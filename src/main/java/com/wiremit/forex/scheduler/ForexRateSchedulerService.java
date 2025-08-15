package com.wiremit.forex.scheduler;

import com.wiremit.forex.model.CurrencyPair;
import com.wiremit.forex.model.FetchStatus;
import com.wiremit.forex.model.ForexRate;
import com.wiremit.forex.model.RawApiRate;
import com.wiremit.forex.repository.CurrencyPairRepository;
import com.wiremit.forex.repository.ForexRateRepository;
import com.wiremit.forex.repository.RawApiRateRepository;
import com.wiremit.forex.service.ExchangeRateApiService;
import com.wiremit.forex.service.FixerApiService;
import com.wiremit.forex.service.OpenExchangeRatesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForexRateSchedulerService {

    private final CurrencyPairRepository currencyPairRepository;
    private final RawApiRateRepository rawApiRateRepository;
    private final ForexRateRepository forexRateRepository;

    private final OpenExchangeRatesService openExchangeRatesService;
    private final ExchangeRateApiService exchangeRateApiService;
    private final FixerApiService fixerApiService;

    private static final BigDecimal DEFAULT_MARKUP = BigDecimal.valueOf(0.10);

    /**
     * Scheduled task that runs every hour to fetch and aggregate forex rates
     * Cron: "0 0 * * * ?" means every hour at minute 0
     */
     @Scheduled(cron = "0 0 * * * ?") // Every hour
//    @Scheduled(fixedRate = 60000) // For testing: every minute
    @Transactional
    public void fetchAndAggregateRates() {
        log.info("Starting scheduled forex rate aggregation at {}", LocalDateTime.now());

        try {
            List<CurrencyPair> activePairs = currencyPairRepository.findByIsActiveTrue();
            if (activePairs.isEmpty()) {
                log.warn("No active currency pairs found. Skipping rate aggregation.");
                return;
            }

            log.info("Found {} active currency pairs: {}", activePairs.size(),
                    activePairs.stream().map(CurrencyPair::getPairCode).collect(Collectors.toList()));

            List<RawApiRate> allRawRates = fetchRatesFromAllApis(activePairs);

            if (!allRawRates.isEmpty()) {
                rawApiRateRepository.saveAll(allRawRates);
                log.info("Saved {} raw API rates to database", allRawRates.size());
            }

            List<ForexRate> aggregatedRates = calculateAggregatedRates(activePairs, allRawRates);
            if (!aggregatedRates.isEmpty()) {
                forexRateRepository.saveAll(aggregatedRates);
                log.info("Saved {} aggregated forex rates to database", aggregatedRates.size());
            }

            log.info("Successfully completed forex rate aggregation");

        } catch (Exception e) {
            log.error("Error during scheduled forex rate aggregation: {}", e.getMessage(), e);
        }
    }

    /**
     * Fetch rates from all three APIs
     */
    private List<RawApiRate> fetchRatesFromAllApis(List<CurrencyPair> currencyPairs) {
        List<RawApiRate> allRawRates = new ArrayList<>();

        // Fetch from OpenExchangeRates
        try {
            List<RawApiRate> openExchangeRates = openExchangeRatesService.fetchRatesForPairs(currencyPairs);
            allRawRates.addAll(openExchangeRates);
            log.info("Fetched {} rates from OpenExchangeRates", openExchangeRates.size());
        } catch (Exception e) {
            log.error("Error fetching from OpenExchangeRates: {}", e.getMessage());
        }

        // Fetch from ExchangeRate-API
        try {
            List<RawApiRate> exchangeRateApiRates = exchangeRateApiService.fetchRatesForPairs(currencyPairs);
            allRawRates.addAll(exchangeRateApiRates);
            log.info("Fetched {} rates from ExchangeRate-API", exchangeRateApiRates.size());
        } catch (Exception e) {
            log.error("Error fetching from ExchangeRate-API: {}", e.getMessage());
        }

        // Fetch from Fixer.io
        try {
            List<RawApiRate> fixerRates = fixerApiService.fetchRatesForPairs(currencyPairs);
            allRawRates.addAll(fixerRates);
            log.info("Fetched {} rates from Fixer.io", fixerRates.size());
        } catch (Exception e) {
            log.error("Error fetching from Fixer.io: {}", e.getMessage());
        }

        return allRawRates;
    }

    /**
     * Calculate aggregated rates for each currency pair
     */
    private List<ForexRate> calculateAggregatedRates(List<CurrencyPair> currencyPairs, List<RawApiRate> rawRates) {
        List<ForexRate> aggregatedRates = new ArrayList<>();

        // Group raw rates by currency pair
        Map<Long, List<RawApiRate>> ratesByPair = rawRates.stream()
                .filter(rate -> rate.getStatus() == FetchStatus.SUCCESS)
                .filter(rate -> rate.getRate().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.groupingBy(rate -> rate.getCurrencyPair().getId()));

        // Calculate average for each pair
        for (CurrencyPair pair : currencyPairs) {
            List<RawApiRate> pairRates = ratesByPair.get(pair.getId());

            if (pairRates == null || pairRates.isEmpty()) {
                log.warn("No successful rates found for pair: {}", pair.getPairCode());
                continue;
            }

            ForexRate aggregatedRate = calculateAverageRate(pair, pairRates);
            if (aggregatedRate != null) {
                aggregatedRates.add(aggregatedRate);
                log.info("Calculated aggregated rate for {}: {} (from {} sources)",
                        pair.getPairCode(), aggregatedRate.getFinalRate(), pairRates.size());
            }
        }

        return aggregatedRates;
    }

    /**
     * Calculate average rate and apply markup for a currency pair
     */
    private ForexRate calculateAverageRate(CurrencyPair pair, List<RawApiRate> rates) {
        try {
            if (rates.isEmpty()) {
                return null;
            }

            // Calculate average rate
            BigDecimal sum = rates.stream()
                    .map(RawApiRate::getRate)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal averageRate = sum.divide(BigDecimal.valueOf(rates.size()), 8, RoundingMode.HALF_UP);

            BigDecimal markup = pair.getCustomMarkup() != null ? pair.getCustomMarkup() : DEFAULT_MARKUP;

            // Apply percentage markup: finalRate = averageRate * (1 + markupPercentage/100)
            BigDecimal markupMultiplier = BigDecimal.ONE.add(markup.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP));
            BigDecimal finalRate = averageRate.multiply(markupMultiplier).setScale(8, RoundingMode.HALF_UP);

            return ForexRate.builder()
                    .currencyPair(pair)
                    .averageRate(averageRate)
                    .finalRate(finalRate)
                    .markupApplied(markup)
                    .sourcesCount(rates.size())
                    .build();

        } catch (Exception e) {
            log.error("Error calculating average rate for {}: {}", pair.getPairCode(), e.getMessage());
            return null;
        }
    }
}

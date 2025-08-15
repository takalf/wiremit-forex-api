package com.wiremit.forex.service;

import com.wiremit.forex.dto.response.ForexApiResponse;
import com.wiremit.forex.model.CurrencyPair;
import com.wiremit.forex.model.FetchStatus;
import com.wiremit.forex.model.RawApiRate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public abstract class BaseForexApiService {

    protected final RestTemplate restTemplate;

    protected abstract String getApiSource();
    protected abstract String buildApiUrl(List<CurrencyPair> currencyPairs);
    protected abstract Class<? extends ForexApiResponse> getResponseClass();

    /**
     * Common method to fetch rates for all currency pairs
     */
    public List<RawApiRate> fetchRatesForPairs(List<CurrencyPair> currencyPairs) {
        List<RawApiRate> rawRates = new ArrayList<>();

        try {
            String url = buildApiUrl(currencyPairs);

            ForexApiResponse response = restTemplate.getForObject(url, getResponseClass());

            if (response != null && response.isSuccess()) {
                log.info("Successfully fetched {} rates from {}", response.getRates().size(), getApiSource());

                // Process each currency pair
                for (CurrencyPair pair : currencyPairs) {
                    RawApiRate rawRate = calculateRateForPair(pair, response);
                    if (rawRate != null) {
                        rawRates.add(rawRate);
                    }
                }
            } else {
                String errorMsg = response != null ? response.getErrorMessage() : "Null response from API";
                log.error("{} API returned error: {}", getApiSource(), errorMsg);

                // Create failed entries for all pairs
                for (CurrencyPair pair : currencyPairs) {
                    rawRates.add(createFailedRate(pair, errorMsg));
                }
            }

        } catch (RestClientException e) {
            log.error("Failed to fetch rates from {}: {}", getApiSource(), e.getMessage());
            // Create failed entries for all pairs
            for (CurrencyPair pair : currencyPairs) {
                rawRates.add(createFailedRate(pair, e.getMessage()));
            }
        }

        return rawRates;
    }

    /**
     * Common rate calculation logic for currency pairs
     */
    protected RawApiRate calculateRateForPair(CurrencyPair pair, ForexApiResponse response) {
        try {
            String baseCurrency = pair.getBaseCurrency();
            String targetCurrency = pair.getTargetCurrency();
            Map<String, BigDecimal> rates = response.getRates();

            BigDecimal rate = calculateCurrencyRate(baseCurrency, targetCurrency, rates);

            if (rate == null) {
                return createFailedRate(pair, "Rate calculation failed for " + pair.getPairCode());
            }

            return RawApiRate.builder()
                    .currencyPair(pair)
                    .rate(rate)
                    .apiSource(getApiSource())
                    .status(FetchStatus.SUCCESS)
                    .build();

        } catch (Exception e) {
            log.error("Error calculating rate for {}: {}", pair.getPairCode(), e.getMessage());
            return createFailedRate(pair, e.getMessage());
        }
    }

    /**
     * Common currency rate calculation logic
     * Handles USD base scenarios and cross-currency calculations
     */
    protected BigDecimal calculateCurrencyRate(String baseCurrency, String targetCurrency, Map<String, BigDecimal> rates) {
        try {
            if ("USD".equals(baseCurrency)) {
                // Direct rate: USD to target currency
                return rates.get(targetCurrency);
            } else if ("USD".equals(targetCurrency)) {
                // Inverse rate: non-USD to USD
                BigDecimal baseRate = rates.get(baseCurrency);
                return baseRate != null ? BigDecimal.ONE.divide(baseRate, 8, RoundingMode.HALF_UP) : null;
            } else {
                // Cross rate: non-USD to non-USD
                BigDecimal baseRate = rates.get(baseCurrency);
                BigDecimal targetRate = rates.get(targetCurrency);

                if (baseRate != null && targetRate != null) {
                    return targetRate.divide(baseRate, 8, RoundingMode.HALF_UP);
                }
                return null;
            }
        } catch (ArithmeticException e) {
            log.error("Rate calculation error for {}-{}: {}", baseCurrency, targetCurrency, e.getMessage());
            return null;
        }
    }

    /**
     * Common method to create failed rate entries
     */
    protected RawApiRate createFailedRate(CurrencyPair pair, String errorMessage) {
        return RawApiRate.builder()
                .currencyPair(pair)
                .rate(BigDecimal.ZERO)
                .apiSource(getApiSource())
                .status(FetchStatus.FAILED)
                .build();
    }
}

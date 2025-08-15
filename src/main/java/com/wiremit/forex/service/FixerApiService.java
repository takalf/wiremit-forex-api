package com.wiremit.forex.service;

import com.wiremit.forex.dto.response.FixerApiResponse;
import com.wiremit.forex.dto.response.ForexApiResponse;
import com.wiremit.forex.model.CurrencyPair;
import com.wiremit.forex.model.FetchStatus;
import com.wiremit.forex.model.RawApiRate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FixerApiService extends BaseForexApiService {

    @Value("${fixer.api.key}")
    private String apiKey;

    @Value("${fixer.api.base-url:https://data.fixer.io/api}")
    private String baseUrl;

    public FixerApiService(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    protected String getApiSource() {
        return "fixer-io";
    }

    @Override
    protected String buildApiUrl(List<CurrencyPair> currencyPairs) {
        Set<String> currencies = currencyPairs.stream()
                .flatMap(pair -> Set.of(pair.getBaseCurrency(), pair.getTargetCurrency()).stream())
                .filter(currency -> !"EUR".equals(currency))
                .collect(Collectors.toSet());

        currencies.add("USD");

        String symbols = String.join(",", currencies);
        return String.format("%s/latest?access_key=%s&symbols=%s", baseUrl, apiKey, symbols);
    }

    @Override
    protected Class<? extends ForexApiResponse> getResponseClass() {
        return FixerApiResponse.class;
    }

    /**
     * Override the rate calculation to convert EUR-based rates to USD-based rates
     * This allows us to use the existing USD-based logic in the parent class
     */
    @Override
    protected RawApiRate calculateRateForPair(CurrencyPair pair, ForexApiResponse response) {
        try {
            String baseCurrency = pair.getBaseCurrency();
            String targetCurrency = pair.getTargetCurrency();
            Map<String, BigDecimal> eurRates = response.getRates();

            // Convert EUR-based rates to USD-based rates
            Map<String, BigDecimal> usdRates = convertEurRatesToUsdRates(eurRates);

            BigDecimal rate = calculateCurrencyRate(baseCurrency, targetCurrency, usdRates);

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
     * Convert EUR-based rates to USD-based rates
     * EUR/USD rate from API gives us how many USD per 1 EUR
     * For USD-based rates, we need rates as "1 USD = X units of other currency"
     */
    private Map<String, BigDecimal> convertEurRatesToUsdRates(Map<String, BigDecimal> eurRates) {
        Map<String, BigDecimal> usdRates = new HashMap<>();

        BigDecimal eurToUsdRate = eurRates.get("USD");
        if (eurToUsdRate == null || eurToUsdRate.compareTo(BigDecimal.ZERO) == 0) {
            log.error("USD rate not found or zero in Fixer.io response");
            return usdRates;
        }

        // For each currency, convert EUR-based rate to USD-based rate
        for (Map.Entry<String, BigDecimal> entry : eurRates.entrySet()) {
            String currency = entry.getKey();
            BigDecimal eurToXRate = entry.getValue();

            if (!"USD".equals(currency) && eurToXRate != null && eurToXRate.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal usdToXRate = eurToXRate.divide(eurToUsdRate, 8, RoundingMode.HALF_UP);
                usdRates.put(currency, usdToXRate);
            }
        }

        return usdRates;
    }
}
package com.wiremit.forex.service;

import com.wiremit.forex.dto.response.ExchangeRateApiResponse;
import com.wiremit.forex.dto.response.ForexApiResponse;
import com.wiremit.forex.model.CurrencyPair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ExchangeRateApiService extends BaseForexApiService {

    @Value("${exchangerate-api.api.key}")
    private String apiKey;

    @Value("${exchangerate-api.api.base-url:https://v6.exchangerate-api.com/v6}")
    private String baseUrl;

    public ExchangeRateApiService(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    protected String getApiSource() {
        return "exchangerate-api";
    }

    @Override
    protected String buildApiUrl(List<CurrencyPair> currencyPairs) {
        return String.format("%s/%s/latest/USD", baseUrl, apiKey);
    }

    @Override
    protected Class<? extends ForexApiResponse> getResponseClass() {
        return ExchangeRateApiResponse.class;
    }
}

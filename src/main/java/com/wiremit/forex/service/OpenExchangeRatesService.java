package com.wiremit.forex.service;

import com.wiremit.forex.dto.response.ForexApiResponse;
import com.wiremit.forex.dto.response.OpenExchangeRatesResponse;
import com.wiremit.forex.model.CurrencyPair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class OpenExchangeRatesService extends BaseForexApiService {

    @Value("${openexchangerates.api.key}")
    private String apiKey;

    @Value("${openexchangerates.api.base-url:https://openexchangerates.org/api}")
    private String baseUrl;

    public OpenExchangeRatesService(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    protected String getApiSource() {
        return "openexchangerates";
    }

    @Override
    protected String buildApiUrl(List<CurrencyPair> currencyPairs) {
        return String.format("%s/latest.json?app_id=%s", baseUrl, apiKey);
    }

    @Override
    protected Class<? extends ForexApiResponse> getResponseClass() {
        return OpenExchangeRatesResponse.class;
    }
}

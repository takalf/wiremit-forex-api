package com.wiremit.forex.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenExchangeRatesResponse implements ForexApiResponse {
    private String disclaimer;
    private String license;
    private Long timestamp;
    private String base;
    private Map<String, BigDecimal> rates;

    @Override
    public boolean isSuccess() {
        return rates != null && !rates.isEmpty();
    }

    @Override
    public Map<String, BigDecimal> getRates() {
        return rates;
    }

    @Override
    public String getErrorMessage() {
        return rates == null ? "No rates data received" : null;
    }

    @Override
    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getBaseCurrency() {
        return base;
    }
}
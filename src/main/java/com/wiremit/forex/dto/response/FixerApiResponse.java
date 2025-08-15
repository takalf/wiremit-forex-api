package com.wiremit.forex.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FixerApiResponse implements ForexApiResponse {
    private Boolean success;
    private Long timestamp;
    private String base;
    private String date;
    private Map<String, BigDecimal> rates;
    private ErrorInfo error;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorInfo {
        private Integer code;
        private String info;
    }

    @Override
    public boolean isSuccess() {
        return Boolean.TRUE.equals(success) && rates != null;
    }

    @Override
    public Map<String, BigDecimal> getRates() {
        return rates;
    }

    @Override
    public String getErrorMessage() {
        return error != null ? error.getInfo() : (!Boolean.TRUE.equals(success) ? "API returned unsuccessful response" : null);
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

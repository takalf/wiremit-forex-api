package com.wiremit.forex.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public interface ForexApiResponse {
    boolean isSuccess();
    Map<String, BigDecimal> getRates();
    String getErrorMessage();
    Long getTimestamp();
    String getBaseCurrency();
}

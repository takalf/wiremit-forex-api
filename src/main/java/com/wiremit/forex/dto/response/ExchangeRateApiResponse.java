package com.wiremit.forex.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateApiResponse implements ForexApiResponse {
    private String result;
    private String documentation;

    @JsonProperty("terms_of_use")
    private String termsOfUse;

    @JsonProperty("time_last_update_unix")
    private Long timeLastUpdateUnix;

    @JsonProperty("base_code")
    private String baseCode;

    @JsonProperty("conversion_rates")
    private Map<String, BigDecimal> conversionRates;

    @JsonProperty("error-type")
    private String errorType;

    @Override
    public boolean isSuccess() {
        return "success".equals(result) && conversionRates != null;
    }

    @Override
    public Map<String, BigDecimal> getRates() {
        return conversionRates;
    }

    @Override
    public String getErrorMessage() {
        return errorType != null ? errorType : ("success".equals(result) ? null : "API returned unsuccessful result");
    }

    @Override
    public Long getTimestamp() {
        return timeLastUpdateUnix;
    }

    @Override
    public String getBaseCurrency() {
        return baseCode;
    }
}

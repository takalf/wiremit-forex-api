package com.wiremit.forex.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForexRateDTO {

    private BigDecimal rate;

    private String baseCurrency;
    private String targetCurrency;
    private String pairCode;
    private String displayName;
}

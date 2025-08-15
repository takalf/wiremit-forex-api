package com.wiremit.forex.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "raw_api_rates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RawApiRate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_pair_id", nullable = false)
    private CurrencyPair currencyPair;

    @Column(nullable = false, precision = 12, scale = 6)
    private BigDecimal rate;

    @Column(name = "api_source", nullable = false, length = 50)
    private String apiSource; // "exchangerate-api", "fixer-io", "currencylayer"


    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private FetchStatus status = FetchStatus.SUCCESS;

}

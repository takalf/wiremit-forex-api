package com.wiremit.forex.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "forex_rates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ForexRate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_pair_id", nullable = false)
    private CurrencyPair currencyPair;

    @Column(name = "average_rate", nullable = false, precision = 12, scale = 6)
    private BigDecimal averageRate; // Average from APIs

    @Column(name = "final_rate", nullable = false, precision = 12, scale = 6)
    private BigDecimal finalRate; // Average + markup

    @Column(name = "markup_applied", nullable = false, precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal markupApplied = BigDecimal.valueOf(0.10);

    @Column(name = "sources_count")
    private Integer sourcesCount; // How many APIs provided data
}

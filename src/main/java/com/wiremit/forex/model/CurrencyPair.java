package com.wiremit.forex.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "currency_pairs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CurrencyPair extends BaseEntity {

    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency;

    @Column(name = "target_currency", nullable = false, length = 3)
    private String targetCurrency;

    @Column(name = "pair_code", nullable = false, unique = true, length = 7)
    private String pairCode;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "custom_markup", precision = 5, scale = 4)
    private BigDecimal customMarkup;

    @PrePersist
    protected void onCreate() {
        if (pairCode == null) {
            pairCode = baseCurrency + targetCurrency;
        }
    }

    public String getDisplayName() {
        return baseCurrency + "-" + targetCurrency;
    }
}

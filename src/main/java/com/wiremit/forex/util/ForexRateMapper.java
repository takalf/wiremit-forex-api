package com.wiremit.forex.util;

import com.wiremit.forex.dto.ForexRateDTO;
import com.wiremit.forex.model.ForexRate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ForexRateMapper {

    public ForexRateDTO toDTO(ForexRate forexRate) {
        if (forexRate == null) {
            return null;
        }

        return ForexRateDTO.builder()
                .rate(forexRate.getFinalRate())
                .baseCurrency(forexRate.getCurrencyPair() != null ? forexRate.getCurrencyPair().getBaseCurrency() : null)
                .targetCurrency(forexRate.getCurrencyPair() != null ? forexRate.getCurrencyPair().getTargetCurrency() : null)
                .pairCode(forexRate.getCurrencyPair() != null ? forexRate.getCurrencyPair().getPairCode() : null)
                .displayName(forexRate.getCurrencyPair() != null ? forexRate.getCurrencyPair().getDisplayName() : null)
                .build();
    }

    /**
     * Convert list of ForexRate entities to list of ForexRateDTO
     */
    public List<ForexRateDTO> toDTOList(List<ForexRate> forexRates) {
        if (forexRates == null) {
            return null;
        }

        return forexRates.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}

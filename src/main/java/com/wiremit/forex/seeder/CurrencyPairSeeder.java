package com.wiremit.forex.seeder;

import com.wiremit.forex.model.CurrencyPair;
import com.wiremit.forex.repository.CurrencyPairRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurrencyPairSeeder implements CommandLineRunner {

    private final CurrencyPairRepository currencyPairRepository;

    @Override
    public void run(String... args) throws Exception {
        seedCurrencyPairs();
    }

    private void seedCurrencyPairs() {
        // Check if data already exists
        if (currencyPairRepository.count() > 0) {
            log.info("Currency pairs already exist. Skipping seeding.");
            return;
        }

        log.info("Seeding currency pairs...");

        List<CurrencyPair> currencyPairs = List.of(
                // USD to other currencies
                CurrencyPair.builder()
                        .baseCurrency("USD")
                        .targetCurrency("GBP")
                        .pairCode("USDGBP")
                        .isActive(true)
                        .customMarkup(null)
                        .build(),

                CurrencyPair.builder()
                        .baseCurrency("USD")
                        .targetCurrency("ZAR")
                        .pairCode("USDZAR")
                        .isActive(true)
                        .customMarkup(null)
                        .build(),

                CurrencyPair.builder()
                        .baseCurrency("ZAR")
                        .targetCurrency("GBP")
                        .pairCode("ZARGBP")
                        .isActive(true)
                        .customMarkup(null)
                        .build(),

                CurrencyPair.builder()
                        .baseCurrency("USD")
                        .targetCurrency("EUR")
                        .pairCode("USDEUR")
                        .isActive(false)
                        .customMarkup(null)
                        .build(),

                CurrencyPair.builder()
                        .baseCurrency("GBP")
                        .targetCurrency("USD")
                        .pairCode("GBPUSD")
                        .isActive(true)
                        .customMarkup(null)
                        .build(),

                CurrencyPair.builder()
                        .baseCurrency("EUR")
                        .targetCurrency("GBP")
                        .pairCode("EURGBP")
                        .isActive(false)
                        .customMarkup(null)
                        .build()
        );

        // Save currency pairs
        List<CurrencyPair> savedPairs = currencyPairRepository.saveAll(currencyPairs);

        log.info("Successfully seeded {} currency pairs:", savedPairs.size());
        savedPairs.forEach(pair ->
                log.info("- {}: {} (Active: {}, Custom Markup: {})",
                        pair.getPairCode(),
                        pair.getDisplayName(),
                        pair.getIsActive(),
                        pair.getCustomMarkup() != null ? pair.getCustomMarkup() : "Default (0.10)")
        );

        // Log active pairs that will be processed
        List<CurrencyPair> activePairs = savedPairs.stream()
                .filter(CurrencyPair::getIsActive)
                .toList();

        log.info("Active currency pairs that will be processed by scheduler: {}",
                activePairs.stream()
                        .map(CurrencyPair::getPairCode)
                        .toList()
        );
    }
}

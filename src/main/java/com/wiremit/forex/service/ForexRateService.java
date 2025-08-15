package com.wiremit.forex.service;

import com.wiremit.forex.dto.ForexRateDTO;
import com.wiremit.forex.exception.CurrencyPairNotFoundException;
import com.wiremit.forex.exception.ForexRateNotFoundException;
import com.wiremit.forex.exception.ForexServiceException;
import com.wiremit.forex.util.ForexRateMapper;
import com.wiremit.forex.model.ForexRate;
import com.wiremit.forex.repository.ForexRateRepository;
import com.wiremit.forex.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ForexRateService {

    private final ForexRateRepository forexRateRepository;
    private final ForexRateMapper forexRateMapper;

    /**
     * Get all latest forex rates for all currency pairs
     */
    public ResponseEntity<ApiResponse<List<ForexRateDTO>>> getAllLatestRates(HttpServletRequest request) {
        log.debug("Fetching all latest forex rates");
        List<ForexRate> rates = forexRateRepository.findLatestRateForAllPairs();
        List<ForexRateDTO> rateDTOs = forexRateMapper.toDTOList(rates);
        String message = rateDTOs.isEmpty() ? "No rates available" : "Latest rates fetched successfully";

        return ResponseEntity.ok(
                ApiResponse.success(message, rateDTOs)
                        .path(request.getRequestURI())
        );
    }

    /**
     * Get the latest rate for a specific currency pair-by-pair code
     */
    public ResponseEntity<ApiResponse<ForexRateDTO>> getLatestRateByPairCode(String pairCode, HttpServletRequest request) {
        log.debug("Fetching latest rate for currency pair: {}", pairCode);

        validatePairCode(pairCode);

        Optional<ForexRate> rate = forexRateRepository.findLatestRateByPairCode(pairCode);
        ForexRate forexRate = rate.orElseThrow(() ->
                new ForexRateNotFoundException("No rate found for currency pair: " + pairCode));

        ForexRateDTO forexRateDTO = forexRateMapper.toDTO(forexRate);

        return ResponseEntity.ok(
                ApiResponse.success("Rate fetched successfully", forexRateDTO)
                        .path(request.getRequestURI())
        );
    }

    /**
     * Get the latest rate for conversion from base to target currency
     */
    public ResponseEntity<ApiResponse<ForexRateDTO>> getLatestRate(String baseCurrency, String targetCurrency, HttpServletRequest request) {
        log.debug("Fetching latest rate for {} to {}", baseCurrency, targetCurrency);

        validateCurrencyCode(baseCurrency);
        validateCurrencyCode(targetCurrency);

        String pairCode = baseCurrency.toUpperCase() + targetCurrency.toUpperCase();
        return getLatestRateByPairCode(pairCode, request);
    }

    /**
     * Get latest rates for multiple currency pairs
     */
    public ResponseEntity<ApiResponse<List<ForexRateDTO>>> getLatestRatesForPairs(List<String> pairCodes, HttpServletRequest request) {
        log.debug("Fetching latest rates for {} currency pairs", pairCodes.size());

        validatePairCodes(pairCodes);

        List<ForexRate> rates = forexRateRepository.findLatestRatesByPairCodes(pairCodes);
        List<ForexRateDTO> rateDTOs = forexRateMapper.toDTOList(rates);
        String message = String.format("Fetched %d rates out of %d requested pairs",
                rateDTOs.size(), pairCodes.size());

        return ResponseEntity.ok(
                ApiResponse.success(message, rateDTOs)
                        .path(request.getRequestURI())
        );
    }

    /**
     * Get rate history for a specific currency pair
     */
    public ResponseEntity<ApiResponse<List<ForexRateDTO>>> getRateHistory(String pairCode, int limit, HttpServletRequest request) {
        log.debug("Fetching rate history for currency pair: {} (limit: {})", pairCode, limit);

        validatePairCode(pairCode);
        validateLimit(limit);

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<ForexRate> rateHistory = forexRateRepository.findRateHistoryByPairCode(pairCode, pageable);
        List<ForexRateDTO> rateHistoryDTOs = forexRateMapper.toDTOList(rateHistory);

        String message = String.format("Fetched %d historical rates for %s", rateHistoryDTOs.size(), pairCode);

        return ResponseEntity.ok(
                ApiResponse.success(message, rateHistoryDTOs)
                        .path(request.getRequestURI())
        );
    }

    // Validation methods

    private void validatePairCode(String pairCode) {
        if (isValidPairCode(pairCode)) {
            throw new CurrencyPairNotFoundException("Invalid currency pair format: " + pairCode);
        }
    }

    private void validateCurrencyCode(String currencyCode) {
        if (isValidCurrencyCode(currencyCode)) {
            throw new CurrencyPairNotFoundException("Invalid currency code format: " + currencyCode);
        }
    }

    private void validatePairCodes(List<String> pairCodes) {
        List<String> invalidPairs = pairCodes.stream()
                .filter(this::isValidPairCode)
                .collect(Collectors.toList());

        if (!invalidPairs.isEmpty()) {
            throw new CurrencyPairNotFoundException("Invalid currency pair formats: " + String.join(", ", invalidPairs));
        }
    }

    private void validateLimit(int limit) {
        if (limit <= 0 || limit > 100) {
            throw new ForexServiceException("Limit must be between 1 and 100");
        }
    }

    /**
     * Validate currency pair code format (e.g., USDEUR)
     */
    private boolean isValidPairCode(String pairCode) {
        if (pairCode == null || pairCode.trim().isEmpty()) {
            return true;
        }

        String cleanPairCode = pairCode.trim().toUpperCase();

        if (cleanPairCode.length() != 6) {
            return true;
        }

        String baseCurrency = cleanPairCode.substring(0, 3);
        String targetCurrency = cleanPairCode.substring(3, 6);

        return isValidCurrencyCode(baseCurrency) ||
                isValidCurrencyCode(targetCurrency) ||
                baseCurrency.equals(targetCurrency);
    }

    /**
     * Validate currency code format (3-letter ISO code)
     */
    private boolean isValidCurrencyCode(String currencyCode) {
        return currencyCode == null ||
                currencyCode.trim().length() != 3 ||
                !currencyCode.trim().matches("[A-Z]{3}");
    }
}
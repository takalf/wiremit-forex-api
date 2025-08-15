package com.wiremit.forex.service;

import com.wiremit.forex.exception.CurrencyPairNotFoundException;
import com.wiremit.forex.exception.ForexServiceException;
import com.wiremit.forex.model.CurrencyPair;
import com.wiremit.forex.repository.CurrencyPairRepository;
import com.wiremit.forex.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CurrencyPairService {

    private final CurrencyPairRepository currencyPairRepository;

    /**
     * Get all currency pairs
     */
    public ResponseEntity<ApiResponse<List<CurrencyPair>>> getAllCurrencyPairs(HttpServletRequest request) {
        log.debug("Fetching all currency pairs");
        List<CurrencyPair> pairs = currencyPairRepository.findAll();

        String message = pairs.isEmpty() ? "No currency pairs found" : "Currency pairs fetched successfully";

        return ResponseEntity.ok(
                ApiResponse.success(message, pairs)
                        .path(request.getRequestURI())
        );
    }

    /**
     * Get all active currency pairs
     */
    public ResponseEntity<ApiResponse<List<CurrencyPair>>> getActiveCurrencyPairs(HttpServletRequest request) {
        log.debug("Fetching active currency pairs");
        List<CurrencyPair> activePairs = currencyPairRepository.findByIsActiveTrue();

        String message = activePairs.isEmpty() ? "No active currency pairs found" : "Active currency pairs fetched successfully";

        return ResponseEntity.ok(
                ApiResponse.success(message, activePairs)
                        .path(request.getRequestURI())
        );
    }

    /**
     * Get a currency pair by ID
     */
    public ResponseEntity<ApiResponse<CurrencyPair>> getCurrencyPairById(Long id, HttpServletRequest request) {
        log.debug("Fetching currency pair with ID: {}", id);

        validateId(id);

        Optional<CurrencyPair> pair = currencyPairRepository.findById(id);
        CurrencyPair currencyPair = pair.orElseThrow(() ->
                new CurrencyPairNotFoundException("Currency pair not found with ID: " + id));

        return ResponseEntity.ok(
                ApiResponse.success("Currency pair fetched successfully", currencyPair)
                        .path(request.getRequestURI())
        );
    }

    /**
     * Create a new currency pair
     */
    @Transactional
    public ResponseEntity<ApiResponse<CurrencyPair>> createCurrencyPair(CurrencyPair currencyPair, HttpServletRequest request) {
        log.debug("Creating new currency pair: {}-{}", currencyPair.getBaseCurrency(), currencyPair.getTargetCurrency());

        validateCurrencyPairForCreation(currencyPair);

        // Ensure pair code is properly set
        if (currencyPair.getPairCode() == null) {
            currencyPair.setPairCode(currencyPair.getBaseCurrency() + currencyPair.getTargetCurrency());
        }

        CurrencyPair savedPair = currencyPairRepository.save(currencyPair);

        return ResponseEntity.ok(
                ApiResponse.success("Currency pair created successfully", savedPair)
                        .path(request.getRequestURI())
        );
    }

    /**
     * Update an existing currency pair
     */
    @Transactional
    public ResponseEntity<ApiResponse<CurrencyPair>> updateCurrencyPair(Long id, CurrencyPair currencyPair, HttpServletRequest request) {
        log.debug("Updating currency pair with ID: {}", id);

        validateId(id);
        validateCurrencyPairForUpdate(currencyPair);

        CurrencyPair existingPair = currencyPairRepository.findById(id)
                .orElseThrow(() -> new CurrencyPairNotFoundException("Currency pair not found with ID: " + id));

        // Update fields
        if (currencyPair.getBaseCurrency() != null) {
            existingPair.setBaseCurrency(currencyPair.getBaseCurrency().toUpperCase());
        }
        if (currencyPair.getTargetCurrency() != null) {
            existingPair.setTargetCurrency(currencyPair.getTargetCurrency().toUpperCase());
        }
        if (currencyPair.getIsActive() != null) {
            existingPair.setIsActive(currencyPair.getIsActive());
        }
        if (currencyPair.getCustomMarkup() != null) {
            existingPair.setCustomMarkup(currencyPair.getCustomMarkup());
        }

        // Update pair code if currencies changed
        if (currencyPair.getBaseCurrency() != null || currencyPair.getTargetCurrency() != null) {
            existingPair.setPairCode(existingPair.getBaseCurrency() + existingPair.getTargetCurrency());
        }

        CurrencyPair updatedPair = currencyPairRepository.save(existingPair);

        return ResponseEntity.ok(
                ApiResponse.success("Currency pair updated successfully", updatedPair)
                        .path(request.getRequestURI())
        );
    }

    /**
     * Activate a currency pair
     */
    @Transactional
    public ResponseEntity<ApiResponse<CurrencyPair>> activateCurrencyPair(Long id, HttpServletRequest request) {
        log.debug("Activating currency pair with ID: {}", id);

        validateId(id);

        CurrencyPair pair = currencyPairRepository.findById(id)
                .orElseThrow(() -> new CurrencyPairNotFoundException("Currency pair not found with ID: " + id));

        pair.setIsActive(true);
        CurrencyPair updatedPair = currencyPairRepository.save(pair);

        return ResponseEntity.ok(
                ApiResponse.success("Currency pair activated successfully", updatedPair)
                        .path(request.getRequestURI())
        );
    }

    /**
     * Deactivate a currency pair
     */
    @Transactional
    public ResponseEntity<ApiResponse<CurrencyPair>> deactivateCurrencyPair(Long id, HttpServletRequest request) {
        log.debug("Deactivating currency pair with ID: {}", id);

        validateId(id);

        CurrencyPair pair = currencyPairRepository.findById(id)
                .orElseThrow(() -> new CurrencyPairNotFoundException("Currency pair not found with ID: " + id));

        pair.setIsActive(false);
        CurrencyPair updatedPair = currencyPairRepository.save(pair);

        return ResponseEntity.ok(
                ApiResponse.success("Currency pair deactivated successfully", updatedPair)
                        .path(request.getRequestURI())
        );
    }

    /**
     * Delete a currency pair
     */
    @Transactional
    public ResponseEntity<ApiResponse<String>> deleteCurrencyPair(Long id, HttpServletRequest request) {
        log.debug("Deleting currency pair with ID: {}", id);

        validateId(id);

        if (!currencyPairRepository.existsById(id)) {
            throw new CurrencyPairNotFoundException("Currency pair not found with ID: " + id);
        }

        currencyPairRepository.deleteById(id);

        return ResponseEntity.ok(
                ApiResponse.<String>success("Currency pair deleted successfully")
                        .path(request.getRequestURI())
        );
    }

    // Validation methods

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new ForexServiceException("Invalid currency pair ID");
        }
    }

    private void validateCurrencyPairForCreation(CurrencyPair currencyPair) {
        if (currencyPair == null) {
            throw new ForexServiceException("Currency pair data is required");
        }

        validateCurrencyCode(currencyPair.getBaseCurrency(), "Base currency");
        validateCurrencyCode(currencyPair.getTargetCurrency(), "Target currency");

        if (currencyPair.getBaseCurrency().equalsIgnoreCase(currencyPair.getTargetCurrency())) {
            throw new ForexServiceException("Base and target currencies cannot be the same");
        }

        if (currencyPair.getCustomMarkup() != null) {
            validateMarkup(currencyPair.getCustomMarkup());
        }

        // Normalize to uppercase
        currencyPair.setBaseCurrency(currencyPair.getBaseCurrency().toUpperCase());
        currencyPair.setTargetCurrency(currencyPair.getTargetCurrency().toUpperCase());
    }

    private void validateCurrencyPairForUpdate(CurrencyPair currencyPair) {
        if (currencyPair == null) {
            throw new ForexServiceException("Currency pair data is required");
        }

        if (currencyPair.getBaseCurrency() != null) {
            validateCurrencyCode(currencyPair.getBaseCurrency(), "Base currency");
        }

        if (currencyPair.getTargetCurrency() != null) {
            validateCurrencyCode(currencyPair.getTargetCurrency(), "Target currency");
        }

        if (currencyPair.getBaseCurrency() != null && currencyPair.getTargetCurrency() != null
                && currencyPair.getBaseCurrency().equalsIgnoreCase(currencyPair.getTargetCurrency())) {
            throw new ForexServiceException("Base and target currencies cannot be the same");
        }

        if (currencyPair.getCustomMarkup() != null) {
            validateMarkup(currencyPair.getCustomMarkup());
        }
    }

    private void validateCurrencyCode(String currencyCode, String fieldName) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            throw new ForexServiceException(fieldName + " is required");
        }

        if (currencyCode.trim().length() != 3) {
            throw new ForexServiceException(fieldName + " must be exactly 3 characters");
        }

        if (!currencyCode.trim().matches("[A-Za-z]{3}")) {
            throw new ForexServiceException(fieldName + " must contain only alphabetic characters");
        }
    }

    private void validateMarkup(BigDecimal markup) {
        if (markup.compareTo(BigDecimal.ZERO) < 0) {
            throw new ForexServiceException("Custom markup cannot be negative");
        }

        if (markup.compareTo(BigDecimal.valueOf(1.0)) > 0) {
            throw new ForexServiceException("Custom markup cannot exceed 100%");
        }
    }
}
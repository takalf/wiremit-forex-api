package com.wiremit.forex.repository;

import com.wiremit.forex.model.ForexRate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ForexRateRepository extends JpaRepository<ForexRate, Long> {

    @Query(value = """
        SELECT fr.*
        FROM forex_rates fr
        INNER JOIN (
            SELECT currency_pair_id, MAX(created_at) as latest_created_at
            FROM forex_rates
            GROUP BY currency_pair_id
        ) latest ON fr.currency_pair_id = latest.currency_pair_id 
                 AND fr.created_at = latest.latest_created_at
        INNER JOIN currency_pairs cp ON fr.currency_pair_id = cp.id
        WHERE cp.is_active = true
        ORDER BY fr.created_at DESC
        """, nativeQuery = true)
    List<ForexRate> findLatestRateForAllPairs();

    /**
     * Find the latest rate for a specific currency pair by pair code
     */
    @Query("""
        SELECT fr FROM ForexRate fr
        JOIN FETCH fr.currencyPair cp
        WHERE cp.pairCode = :pairCode
        AND cp.isActive = true
        ORDER BY fr.createdAt DESC
        LIMIT 1
        """)
    Optional<ForexRate> findLatestRateByPairCode(@Param("pairCode") String pairCode);

    /**
     * Find the latest rates for multiple currency pairs by their pair codes
     */
    @Query(value = """
            SELECT fr.*
            FROM forex_rates fr
            INNER JOIN (
                SELECT currency_pair_id, MAX(created_at) as latest_created_at
                FROM forex_rates fr2
                INNER JOIN currency_pairs cp2 ON fr2.currency_pair_id = cp2.id
                WHERE cp2.pair_code IN :pairCodes
                AND cp2.is_active = true
                GROUP BY currency_pair_id
            ) latest ON fr.currency_pair_id = latest.currency_pair_id 
                     AND fr.created_at = latest.latest_created_at
            INNER JOIN currency_pairs cp ON fr.currency_pair_id = cp.id
            WHERE cp.pair_code IN :pairCodes
            ORDER BY fr.created_at DESC
            """, nativeQuery = true)
    List<ForexRate> findLatestRatesByPairCodes(@Param("pairCodes") List<String> pairCodes);

    /**
     * Find rate history for a specific currency pair with pagination
     */
    @Query("""
        SELECT fr FROM ForexRate fr
        JOIN FETCH fr.currencyPair cp
        WHERE cp.pairCode = :pairCode
        AND cp.isActive = true
        ORDER BY fr.createdAt DESC
        """)
    List<ForexRate> findRateHistoryByPairCode(@Param("pairCode") String pairCode, Pageable pageable);
    /**
     * Find the latest rate for a specific currency pair ID
     */
    @Query("""
        SELECT fr FROM ForexRate fr
        WHERE fr.currencyPair.id = :currencyPairId
        ORDER BY fr.createdAt DESC
        LIMIT 1
        """)
    Optional<ForexRate> findLatestRateByCurrencyPairId(@Param("currencyPairId") Long currencyPairId);


    /**
     * Find rates within a date range for a specific pair
     */
    @Query("""
        SELECT fr FROM ForexRate fr
        JOIN FETCH fr.currencyPair cp
        WHERE cp.pairCode = :pairCode
        AND fr.createdAt BETWEEN :startDate AND :endDate
        ORDER BY fr.createdAt DESC
        """)
    List<ForexRate> findByPairCodeAndDateRange(
            @Param("pairCode") String pairCode,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);
}
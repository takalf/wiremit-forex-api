package com.wiremit.forex.repository;

import com.wiremit.forex.model.CurrencyPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CurrencyPairRepository extends JpaRepository<CurrencyPair, Long> {

    List<CurrencyPair> findByIsActiveTrue();
}

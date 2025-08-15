package com.wiremit.forex.repository;

import com.wiremit.forex.model.RawApiRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RawApiRateRepository extends JpaRepository<RawApiRate, Long> {
}

package com.rms.funds.hodings.analyser.repository;

import com.rms.funds.hodings.analyser.entity.MutualFundHoldingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface MutualFundHoldingRepository extends JpaRepository<MutualFundHoldingEntity, Long> {

    boolean existsByMutualFundIdAndAtDate(Long mutualFundId, LocalDate atDate);
}

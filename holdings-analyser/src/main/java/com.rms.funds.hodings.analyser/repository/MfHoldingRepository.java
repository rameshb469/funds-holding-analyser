package com.rms.funds.hodings.analyser.repository;

import com.rms.funds.hodings.analyser.entity.MfHoldingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MfHoldingRepository extends JpaRepository<MfHoldingEntity, Long> {

    List<MfHoldingEntity> findByStockId(Long stockId);
}

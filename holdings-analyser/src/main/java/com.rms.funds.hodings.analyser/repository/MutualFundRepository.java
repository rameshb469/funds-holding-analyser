package com.rms.funds.hodings.analyser.repository;

import com.rms.funds.hodings.analyser.entity.MutualFundEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MutualFundRepository extends JpaRepository<MutualFundEntity, Long> {
}

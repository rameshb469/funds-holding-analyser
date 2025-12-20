package com.rms.funds.holdings.analyser.repository;

import com.rms.funds.holdings.analyser.entity.MutualFundConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MutualFundConfigRepository extends JpaRepository<MutualFundConfigEntity, Long> {
}

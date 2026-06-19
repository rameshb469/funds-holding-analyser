package com.rms.funds.holdings.analyser.repository;

import com.rms.funds.holdings.analyser.entity.KiteSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KiteSessionRepository extends JpaRepository<KiteSessionEntity, Long> {
}

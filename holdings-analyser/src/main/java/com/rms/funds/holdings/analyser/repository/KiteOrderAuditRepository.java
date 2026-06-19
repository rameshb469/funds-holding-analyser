package com.rms.funds.holdings.analyser.repository;

import com.rms.funds.holdings.analyser.entity.KiteOrderAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KiteOrderAuditRepository extends JpaRepository<KiteOrderAuditEntity, Long> {

    List<KiteOrderAuditEntity> findTop50ByOrderByCreatedAtDesc();
}

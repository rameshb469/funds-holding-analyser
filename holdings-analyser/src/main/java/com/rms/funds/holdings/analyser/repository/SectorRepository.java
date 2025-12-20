package com.rms.funds.holdings.analyser.repository;

import com.rms.funds.holdings.analyser.entity.SectorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SectorRepository extends JpaRepository<SectorEntity, Long> {
}

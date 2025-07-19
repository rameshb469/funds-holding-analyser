package com.rms.funds.hodings.analyser.repository;

import com.rms.funds.hodings.analyser.entity.SectorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SectorRepository extends JpaRepository<SectorEntity, Long> {
}

package com.rms.funds.holdings.analyser.repository;

import com.rms.funds.holdings.analyser.entity.IndustryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndustryRepository extends JpaRepository<IndustryEntity, Long> {
}

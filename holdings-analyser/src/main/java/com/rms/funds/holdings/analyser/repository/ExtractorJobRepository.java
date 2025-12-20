package com.rms.funds.holdings.analyser.repository;

import com.rms.funds.holdings.analyser.entity.ExtractorJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExtractorJobRepository extends JpaRepository<ExtractorJobEntity, Long> {

    List<ExtractorJobEntity> findByMutualFundConfigId(Long mutualFundConfigId);

    List<ExtractorJobEntity> findByConfigMutualFundIdAndAtDate(Long mutualFundConfigId, LocalDate atDate);


}

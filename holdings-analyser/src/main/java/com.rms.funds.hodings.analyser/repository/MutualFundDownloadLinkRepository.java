package com.rms.funds.hodings.analyser.repository;

import com.rms.funds.hodings.analyser.entity.MutualFundDownloadLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface MutualFundDownloadLinkRepository extends JpaRepository<MutualFundDownloadLinkEntity, Long> {

    Optional<MutualFundDownloadLinkEntity> findByMutualFundIdAndAtDate(Long mutualFundId, LocalDate atDate);
}

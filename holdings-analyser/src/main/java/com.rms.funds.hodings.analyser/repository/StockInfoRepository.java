package com.rms.funds.hodings.analyser.repository;

import com.rms.funds.hodings.analyser.entity.StockInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockInfoRepository extends JpaRepository<StockInfoEntity, Long> {

}

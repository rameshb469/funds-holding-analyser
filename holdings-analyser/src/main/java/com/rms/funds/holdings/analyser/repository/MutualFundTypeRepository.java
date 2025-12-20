package com.rms.funds.holdings.analyser.repository;

import com.rms.funds.holdings.analyser.entity.MutualFundTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MutualFundTypeRepository extends JpaRepository<MutualFundTypeEntity,Long > {
}

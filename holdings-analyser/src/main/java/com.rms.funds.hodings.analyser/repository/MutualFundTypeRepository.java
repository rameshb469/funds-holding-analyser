package com.rms.funds.hodings.analyser.repository;

import com.rms.funds.hodings.analyser.entity.MutualFundTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MutualFundTypeRepository extends JpaRepository<MutualFundTypeEntity,Long > {
}

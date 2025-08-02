package com.rms.funds.hodings.analyser.repository;

import com.rms.funds.hodings.analyser.entity.ExtractorJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExtractorJobRepository extends JpaRepository<ExtractorJobEntity, Long> {

}

package com.rms.funds.holdings.analyser.repository;

import com.rms.funds.holdings.analyser.entity.OrderAgentCycleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderAgentCycleRepository extends JpaRepository<OrderAgentCycleEntity, Long> {

    Optional<OrderAgentCycleEntity> findByCycleAnchorDate(LocalDate cycleAnchorDate);

    List<OrderAgentCycleEntity> findTop20ByOrderByCreatedAtDesc();

    List<OrderAgentCycleEntity> findByStatusInOrderByCreatedAtDesc(List<String> statuses);
}

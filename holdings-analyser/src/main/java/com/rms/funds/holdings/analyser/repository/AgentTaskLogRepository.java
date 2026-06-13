package com.rms.funds.holdings.analyser.repository;

import com.rms.funds.holdings.analyser.entity.AgentTaskLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgentTaskLogRepository extends JpaRepository<AgentTaskLogEntity, Long> {

    List<AgentTaskLogEntity> findByTaskName(String taskName);

    List<AgentTaskLogEntity> findByStatus(String status);

    List<AgentTaskLogEntity> findByTaskNameAndStatus(String taskName, String status);

    List<AgentTaskLogEntity> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query(value = """
            SELECT * FROM agent_task_logs 
            WHERE task_name = :taskName
            ORDER BY task_embedding <-> :embedding::vector 
            LIMIT :limit
            """, nativeQuery = true)
    List<AgentTaskLogEntity> findSimilarTasks(
            @Param("taskName") String taskName,
            @Param("embedding") String embedding,
            @Param("limit") int limit);

    @Query("SELECT a FROM AgentTaskLogEntity a WHERE a.taskName = :taskName ORDER BY a.createdAt DESC")
    List<AgentTaskLogEntity> findLatestTasksByName(@Param("taskName") String taskName);

    @Query("SELECT a FROM AgentTaskLogEntity a WHERE a.status = 'SUCCESS' ORDER BY a.executionTimeMs ASC")
    List<AgentTaskLogEntity> findFastestTasks();
}


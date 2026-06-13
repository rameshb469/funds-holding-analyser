package com.rms.funds.holdings.analyser.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "agent_task_logs")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AgentTaskLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_name", nullable = false, length = 100)
    private String taskName;

    @Column(name = "task_description", columnDefinition = "TEXT")
    private String taskDescription;

    @Column(name = "task_embedding", columnDefinition = "vector(1536)")
    private float[] taskEmbedding;

    @Column(name = "input_data", columnDefinition = "jsonb")
    private String inputData; // JSON string

    @Column(name = "output_data", columnDefinition = "jsonb")
    private String outputData; // JSON string

    @Column(name = "status", length = 20)
    private String status; // SUCCESS, FAILED, PENDING

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}


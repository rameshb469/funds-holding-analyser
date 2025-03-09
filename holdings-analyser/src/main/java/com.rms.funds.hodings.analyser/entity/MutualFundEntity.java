package com.rms.funds.hodings.analyser.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "MUTUAL_FUND")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MutualFundEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @Column(name = "MUTUAL_FUND_HOUSE_ID")
    private Long mutualFundHouseId;

    @OneToOne
    //@PrimaryKeyJoinColumn
    @JoinColumn(name = "MUTUAL_FUND_HOUSE_ID", insertable = false, updatable = false)
    private MutualFundHouseEntity houseEntity;

    @Column(name = "FUND_TYPE_ID")
    private Long fundTypeId;

    @OneToOne
    //@PrimaryKeyJoinColumn
    @JoinColumn(name = "FUND_TYPE_ID", insertable = false, updatable = false)
    private MutualFundTypeEntity typeEntity;

    @Column(name = "is_international_fund")
    private boolean isInternationalFund;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}

package com.rms.funds.hodings.analyser.controller.dto;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MutualFundConfigDto {

    private Long id;
    private Long mfHouseId;
    private String mfHouseName;
    private Long mutualFundId;
    private String mutualFundName;
    private Long mfTypeId;
    private String mfTypeName;
    private String configUrl;
    private String sheetName;
    private String dateMapper1;
    private String dateMapper2;
    private String dateMapper3;
    private String dateMapper4;
    private Long successfulCount;
    private Long failedCount;
}

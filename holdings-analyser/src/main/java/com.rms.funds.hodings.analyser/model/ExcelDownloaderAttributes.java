package com.rms.funds.hodings.analyser.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder(toBuilder = true)
@Getter
@ToString
public class ExcelDownloaderAttributes {

    private String url;

    private String mutualFundHouse;
    private String mutualFundName;
    private String fundTypeName;
    private Long configId;
    private String contentType;
    private String extension;
    private String fileName;
    private boolean isZipped;
    private String sheetName;
    private SheetColumnMapper sheetColumnMapper;
    private boolean isPickupBySystem;

}

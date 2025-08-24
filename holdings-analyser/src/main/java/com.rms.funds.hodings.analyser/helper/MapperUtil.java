package com.rms.funds.hodings.analyser.helper;

import com.rms.funds.hodings.analyser.entity.MutualFundConfigEntity;
import com.rms.funds.hodings.analyser.model.ExcelDownloaderAttributes;
import com.rms.funds.hodings.analyser.model.SheetColumnMapper;

public class MapperUtil {

    public static ExcelDownloaderAttributes getAttributes(String link, final MutualFundConfigEntity config){

        return ExcelDownloaderAttributes.builder()
                .url(link)
                .mutualFundName(config.getMutualFund().getName())
                .mutualFundHouse(config.getMutualFund().getHouseEntity().getName())
                .fundTypeName(config.getMutualFund().getTypeEntity().getName())
                .sheetName(config.getSheetName())
                .contentType(config.getContentType())
                .extension(config.getExtension())
                .isPickupBySystem(config.isPickValuesBySystem())
                .sheetColumnMapper(SheetColumnMapper.builder()
                        .isin(config.getIsinCodeColNumber())
                        .stockName(config.getStockNameColNumber())
                        .industry(config.getIndustryCodeColumnNumber())
                        .quantity(config.getQuantityColNumber())
                        .netAssetPerc(config.getNetAssetPercColNumber())
                        .marketValue(config.getMarketValueColNumber())
                        .build())
                .build();
    }
}

package com.rms.funds.hodings.analyser.utility;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;

@Slf4j
public class ValueUtil {

    public static double getNetAssetPerc(Row row, int cellNumber) {
        try {
            return row.getCell(cellNumber).getNumericCellValue();
        } catch (Exception e){
            return 0.01;
        }
    }

    public static double getMarketValue(Row row, int cellNumber) {
        try {
            return row.getCell(cellNumber).getNumericCellValue();
        } catch (Exception e){
            return 1;
        }
    }

    public static Long getQuantity(Row row, int cellNumber) {
        try {
            if (row.getLastCellNum() >= cellNumber-1) {
                return (long) row.getCell(cellNumber).getNumericCellValue();
            }

        } catch (Exception e) {
            log.error("invalid row is given and cell number {}", cellNumber);
        }
        return 0L;
    }

    public static String getStringValue(Row row, int cellNumber) {
        try {
            if (row.getLastCellNum() >= cellNumber) {
                return row.getCell(cellNumber).getStringCellValue();
            }

        } catch (Exception e) {
            log.error("invalid row is given and cell number {}", cellNumber);
        }
        return "INVALID";
    }
}

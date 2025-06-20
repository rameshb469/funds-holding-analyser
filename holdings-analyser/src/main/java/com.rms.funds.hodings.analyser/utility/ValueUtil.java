package com.rms.funds.hodings.analyser.utility;

import org.apache.poi.ss.usermodel.Row;

public class ValueUtil {

    public static double getNetAssetPerc(Row row, int cellNumber) {
        try {
            return row.getCell(cellNumber).getNumericCellValue();
        } catch (Exception e){
            return 0.01;
        }
    }
}

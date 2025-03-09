package com.rms.funds.hodings.analyser.utility;

public enum DateMonthEnum {
    M, MM, MMM, YY, YYYY;

    public DateMonthEnum from(String value){
        for (DateMonthEnum de : DateMonthEnum.values()){
            if (de.name().equalsIgnoreCase(value)){
                return de;
            }
        }

        throw new RuntimeException("Invalid Value");
    }
}

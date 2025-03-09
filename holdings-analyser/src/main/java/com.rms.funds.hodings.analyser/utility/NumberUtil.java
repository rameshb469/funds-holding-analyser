package com.rms.funds.hodings.analyser.utility;

import java.util.Optional;

public class NumberUtil {

    public static Optional<Integer> safeInt(String number){
        try{
            return Optional.of(Integer.parseInt(number));
        }catch (Exception e){
            return Optional.empty();
        }
    }
}

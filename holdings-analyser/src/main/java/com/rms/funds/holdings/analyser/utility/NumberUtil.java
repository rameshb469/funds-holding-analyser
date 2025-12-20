package com.rms.funds.holdings.analyser.utility;

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

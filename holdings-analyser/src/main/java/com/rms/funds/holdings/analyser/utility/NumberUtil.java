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

    public static Optional<Long> safeLong(String number){
        try{
            return Optional.of(Long.parseLong(number));
        }catch (Exception e){
            return Optional.empty();
        }
    }
}

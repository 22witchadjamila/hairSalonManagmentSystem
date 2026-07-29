package za.ac.cput.util;

import java.math.BigDecimal;

public class Helper {

    public static boolean isNullOrEmpty(String str){
        return str == null || str.trim().isEmpty();
    }

    public static boolean isValidAmount(BigDecimal amount){
        return amount != null &&
                amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isPositiveDouble(double value){
        return value > 0;
    }

    public static boolean isPositiveInteger(int value){
        return value > 0;
    }

}

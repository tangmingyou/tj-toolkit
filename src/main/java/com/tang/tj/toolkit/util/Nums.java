package com.tang.tj.toolkit.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Nums {

    /**
     * 可能为数值的数据转换成 double，不能转换返回 defaultV
     */
    public static double number2Double(Object number, double defaultV) {
        if (number == null) {
            return defaultV;
        }
        if (number instanceof Number) {
            return ((Number) number).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(number));
        } catch (Exception ignored) {
            return defaultV;
        }
    }

    /**
     * 可能为数值的数据转换成 int，不能转换返回 defaultV
     * @param defaultV 转换失败的默认值
     */
    public static int number2Int(Object number, int defaultV) {
        if (number == null) {
            return defaultV;
        }
        if (number instanceof Number) {
            return ((Number)number).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(number));
        } catch (Exception ignored) {
            return defaultV;
        }
    }
    
    /**
     * double 转成字符串，最多n位小数，不含小数位时不带小数点
     * 11.00d -> "11"
     * 11.1d -> "11.1"
     */
    public static String double2Str(double num, int scale) {
        if (num == 0d) {
            return "0";
        }
        return BigDecimal.valueOf(num).setScale(scale, RoundingMode.DOWN).stripTrailingZeros().toPlainString();
    }

//    public static String double2Str(String num, int scale) {
//        return double2Str(number2Double(num, 0d), scale);
//    }
//
//    public static String double2Str(Object num, int scale) {
//        return double2Str(number2Double(num, 0d), scale);
//    }

    public static BigDecimal num2Decimal(Object num, BigDecimal defaultV) {
        if (num instanceof BigDecimal) {
            return (BigDecimal) num;
        }
        if (num instanceof Number) {
            return BigDecimal.valueOf(((Number)num).doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(num));
        }catch (Exception e) {
            e.printStackTrace();
            return defaultV;
        }
    }
    
    public static boolean isBetween(Integer num, int begin, int end) {
        return num != null && num >= begin && num <= end;
    }


    /* start ================================================================================== */
    /* copy from org.apache.commons.lang3.math.NumberUtils */
    public static boolean isParsable(final String str) {
        if (Strings.isEmpty(str)) {
            return false;
        }
        if (str.charAt(str.length() - 1) == '.') {
            return false;
        }
        if (str.charAt(0) == '-') {
            if (str.length() == 1) {
                return false;
            }
            return withDecimalsParsing(str, 1);
        }
        return withDecimalsParsing(str, 0);
    }

    private static boolean withDecimalsParsing(final String str, final int beginIdx) {
        int decimalPoints = 0;
        for (int i = beginIdx; i < str.length(); i++) {
            final boolean isDecimalPoint = str.charAt(i) == '.';
            if (isDecimalPoint) {
                decimalPoints++;
            }
            if (decimalPoints > 1) {
                return false;
            }
            if (!isDecimalPoint && !Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static Double createDouble(final String str) {
        if (str == null) {
            return null;
        }
        return Double.valueOf(str);
    }

    /* end ================================================================================== */
    
}

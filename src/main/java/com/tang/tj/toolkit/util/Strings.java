package com.tang.tj.toolkit.util;

public class Strings {

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
    
    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }
    
}

package com.tang.tj.toolkit.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Objs {

    public static <T> T ifNull(T data, T other) {
        return data != null ? data : other;
    }

    /**
     * 判断 Object 对象是否为空，Optional 是否为空，CharSequence、Collection、Map、Array 长度是否为 0
     *
     * @param obj 对象
     * @return 是否为空
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        } else if (obj instanceof Optional) {
            return !((Optional<?>) obj).isPresent();
        } else if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length() == 0;
        } else if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        } else if (obj instanceof Collection) {
            return ((Collection<?>) obj).isEmpty();
        } else {
            return obj instanceof Map && ((Map<?, ?>) obj).isEmpty();
        }
    }

    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    @SafeVarargs
    public static <T> boolean isEqualsAny(T item, T...eqs) {
        if (eqs == null || eqs.length == 0) {
            return false;
        }
        for (Object eq : eqs) {
            if (Objects.equals(item, eq)) {
                return true;
            }
        }
        return false;
    }
    
}

package org.mvc.util;

import java.util.UUID;

public class StringUtil {

    public static boolean isEmpty(String str) {
        return str == null || str.equals("");
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().equals("");
    }

    public static String randomString() {
        return UUID.randomUUID().toString();
    }
}

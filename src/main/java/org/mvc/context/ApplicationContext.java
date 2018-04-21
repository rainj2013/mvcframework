package org.mvc.context;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yangyujian
 * Time: 2018/4/21 17:40
 */
public class ApplicationContext {
    private static final Map<String,Object> map = new HashMap<>();

    public static void set(String key, Object obj) {
        map.put(key, obj);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getAs(Class<T> clazz, String key) {
        return (T)map.get(key);
    }
}

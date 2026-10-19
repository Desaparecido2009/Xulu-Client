package com.elementars.eclient.util;

import java.util.HashMap;

public class DataManager
{
    private static HashMap<String, Object> data = new HashMap<String, Object>();

    public static void put(final String key, final Object value) {
        data.put(key, value);
    }

    public static Object get(final String key) {
        return data.get(key);
    }
}

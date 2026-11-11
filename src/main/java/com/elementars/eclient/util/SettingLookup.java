package com.elementars.eclient.util;

import com.elementars.eclient.Xulu;
import dev.xulu.settings.Value;

public class SettingLookup
{
    public static <T> Value<T> get(final String name) {
        return Xulu.VALUE_MANAGER.getValueByName(name);
    }
}

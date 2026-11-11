package com.elementars.eclient.util;

public class NumberUtils
{
    public static boolean isInteger(final String str) {
        if (str == null) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDouble(final String str) {
        if (str == null) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNumber(final String str) {
        return isInteger(str) || isDouble(str);
    }
}

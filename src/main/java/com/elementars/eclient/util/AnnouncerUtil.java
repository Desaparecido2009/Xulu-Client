package com.elementars.eclient.util;

public class AnnouncerUtil
{
    public static int currentIndex;

    public static String getRandomMessage(final String[] messages) {
        if (messages.length == 0) {
            return "";
        }
        currentIndex = (currentIndex + 1) % messages.length;
        return messages[currentIndex];
    }
}

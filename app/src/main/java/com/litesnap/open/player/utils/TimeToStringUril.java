package com.litesnap.open.player.utils;

import java.util.Formatter;
import java.util.Locale;

/**
 * Created by ZTMIDGO on 2018/12/6.
 */

public class TimeToStringUril {
    public static String stringForTime(int timeMs) {
        StringBuilder formatBuilder = new StringBuilder();
        Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        formatBuilder.setLength(0);
        if (hours > 0) {
            return formatter.format("%d:%2d:%2d", hours, minutes, seconds).toString();
        } else {
            return formatter.format("%2d:%2d", minutes, seconds).toString();
        }
    }
}

package com.alexandra.nokia.util;

public class TimeUtil {
    public static int parseHhMmSsToSeconds(String hhmmss) {
        if (hhmmss == null || !hhmmss.matches("\\d{2}:\\d{2}:\\d{2}")) {
            throw new IllegalArgumentException("Bad input format (hh:mm:ss)");
        }
        String[] parts = hhmmss.split(":");
        int hh = Integer.parseInt(parts[0]);
        int mm = Integer.parseInt(parts[1]);
        int ss = Integer.parseInt(parts[2]);

        if (hh < 0 || mm < 0 || mm > 59 || ss < 0 || ss > 59) {
            throw new IllegalArgumentException("Bad input format (hh:mm:ss)");
        }
        return hh * 3600 + mm * 60 + ss;
    }

    public static String formatSecondsToHhMmSs(int totalSeconds) {
        if (totalSeconds < 0) totalSeconds = 0;
        int hh = totalSeconds / 3600;
        int rem = totalSeconds % 3600;
        int mm = rem / 60;
        int ss = rem % 60;
        return String.format("%02d:%02d:%02d", hh, mm, ss);
    }
}

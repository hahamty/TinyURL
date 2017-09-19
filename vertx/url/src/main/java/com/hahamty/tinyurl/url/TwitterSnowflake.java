package com.hahamty.tinyurl.url;

public class TwitterSnowflake {
    private static long lastTimestamp = 0L;
    private static int count = 0;

    public static long getNextLongId(int machineId) {
        long timestamp = System.currentTimeMillis();
        // Start from UTC Jan, 1st, 2017
        timestamp -= 1483228800L;
        int num = -1;
        if (timestamp == lastTimestamp) {
            synchronized (TwitterSnowflake.class) {
                if (count != (1 << 12) - 1) {
                    num = count++;
                }
            }
        } else {
            synchronized (TwitterSnowflake.class) {
                lastTimestamp = timestamp;
                count = 1;
                num = 0;
            }
        }

        if (num == -1) {
            return -1;
        }

        return (timestamp << 22) | (machineId << 12) | num;
    }
}

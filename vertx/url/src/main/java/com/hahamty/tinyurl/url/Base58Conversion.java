package com.hahamty.tinyurl.url;

public class Base58Conversion {
    private final static char[] base58Chars = "123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();

    public static String encode(long number) {
        if (number < 0) {
            throw new IllegalArgumentException();
        }
        StringBuilder stringBuilder = new StringBuilder(12);
        while (number >= 58) {
            int remains = (int) (number % 58);
            stringBuilder.append(base58Chars[remains]);
            number /= 58;
        }
        stringBuilder.append(base58Chars[(int) number]);
        return stringBuilder.toString();
    }
}

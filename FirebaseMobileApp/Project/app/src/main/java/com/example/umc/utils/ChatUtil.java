package com.example.umc.utils;
public class ChatUtil {
    public static String buildChatId(String a, String b) {
        return a.compareTo(b) < 0 ? a + "_" + b : b + "_" + a;
    }
}

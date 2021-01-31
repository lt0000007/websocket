package com.example.websocket.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lantian
 */
public class EncryptionUtils {


    public static List<Integer> MsgEncryption(List<Integer> msgList) {
        List<Integer> integers = new ArrayList<>();
        for (int i = 0; i < msgList.size(); i++) {
            integers.add(msgList.get(i) * 2 + 1889);
        }
        return integers;
    }
}

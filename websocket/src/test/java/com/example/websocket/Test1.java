package com.example.websocket;

import com.example.websocket.utils.RandomName;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;

public class Test1 {

    @Test
    public void test2() {

        byte[] srtbyte = {-45,98,98};
        try {
            String res = new String(srtbyte,"GBK");
            System.out.println(res);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}

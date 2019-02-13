package com.cyt.utils;

import org.apache.commons.net.util.Base64;

import java.nio.charset.Charset;

/**
 * Created by cyt on 2017/12/20.
 */
public class Base64Utils {

    public static String encode(String raw, Charset charset) {
        if (raw == null) {
            return null;
        }
        return encode(raw.getBytes(charset));
    }

    public static String encode(byte[] raw) {
        return new String(Base64.encodeBase64(raw));
    }

    public static String decode(String raw, Charset charset) {
        return new String(decode(raw), charset);
    }

    public static byte[] decode(String raw) {
        return Base64.decodeBase64(raw.getBytes());
    }

}

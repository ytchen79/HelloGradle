package com.cyt.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by cyt on 2017/12/20.
 */
public class HashUtils {

    public static String MD5Encode(byte[] rawBytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(rawBytes);
            byte[] rawMD5Key = md.digest();
            return StringUtils.encodeHex(rawMD5Key).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String SHA1Encode(byte[] rawBytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.update(rawBytes);
            byte[] rawSHA1Key = md.digest();
            return StringUtils.encodeHex(rawSHA1Key).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String SHA256Encode(byte[] rawBytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(rawBytes);
            byte[] rawSHA256Key = md.digest();
            return StringUtils.encodeHex(rawSHA256Key).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}

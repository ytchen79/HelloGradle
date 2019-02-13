package com.cyt.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {

    private static final String AES_ALG = "AES";

    private static final String AES_CBC_PCK_ALG = "AES/CBC/PKCS5Padding";

    private static final byte[] AES_IV = initIv(AES_CBC_PCK_ALG);

    public static String encryptContent(String content, String encryptKey, String charset) {
        return aesEncrypt(content, encryptKey, charset);
    }

    public static String decryptContent(String content, String encryptKey, String charset) {
        return aesDecrypt(content, encryptKey, charset);
    }

    private static String aesEncrypt(String content, String aesKey, String charset) {
        try {
            Cipher cipher = Cipher.getInstance(AES_CBC_PCK_ALG);
            IvParameterSpec iv = new IvParameterSpec(AES_IV);
            cipher.init(1, new SecretKeySpec(Base64Utils.decode(aesKey), AES_ALG), iv);
            byte[] encryptBytes = cipher.doFinal(content.getBytes(charset));
            return new String(Base64Utils.encode(encryptBytes));
        } catch (Exception e) {
            throw new RuntimeException("AES加密失败：Aescontent = " + content + "; charset = " + charset, e);
        }
    }

    private static String aesDecrypt(String content, String key, String charset) {
        try {
            Cipher cipher = Cipher.getInstance(AES_CBC_PCK_ALG);
            IvParameterSpec iv = new IvParameterSpec(initIv(AES_CBC_PCK_ALG));
            cipher.init(2, new SecretKeySpec(Base64Utils.decode(key), AES_ALG), iv);
            byte[] cleanBytes = cipher.doFinal(Base64Utils.decode(content));
            return new String(cleanBytes, charset);
        } catch (Exception e) {
            throw new RuntimeException("AES解密失败：Aescontent = " + content + "; charset = " + charset, e);
        }
    }

    private static byte[] initIv(String fullAlg) {
        try {
            Cipher cipher = Cipher.getInstance(fullAlg);
            int blockSize = cipher.getBlockSize();
            byte[] iv = new byte[blockSize];
            for (int i = 0; i < blockSize; i++) {
                iv[i] = 0;
            }
            return iv;
        } catch (Exception e) {
            int blockSize = 16;
            byte[] iv = new byte[blockSize];
            for (int i = 0; i < blockSize; i++) {
                iv[i] = 0;
            }
            return iv;
        }
    }
}

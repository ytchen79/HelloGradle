package com.cyt.utils;

/**
 * Created by cyt on 2017/12/20.
 */
public class ArrayUtils {

    /**
     * 按照字节长度将截断字符串
     * @param src	原字符
     * @param pos	起始位置
     * @param length	截取长度
     */
    public static byte[] splitByteArray(byte[] src, int pos, int length) {
        if (pos + length > src.length) {
            throw new ArrayIndexOutOfBoundsException(pos + length);
        }
        byte[] tmpBuff = new byte[length];
        System.arraycopy(src, pos, tmpBuff, 0, length);
        return tmpBuff;
    }

    public static byte[] removeIndex(byte[] src, int index) {
        if (index < 0 || index > src.length - 1) {
            throw new ArrayIndexOutOfBoundsException(StringUtils.formatString("索引值%s超出数组长度为%s", index, src.length));
        }
        byte[] newByte = new byte[src.length - 1];
        System.arraycopy(src, 0, newByte, 0, index);
        System.arraycopy(src, index, newByte, index + 1, src.length - index -1);
        return newByte;
    }

}

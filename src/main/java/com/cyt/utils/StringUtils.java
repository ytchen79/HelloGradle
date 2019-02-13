package com.cyt.utils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by cyt on 2017/12/20.
 */
public class StringUtils {

    public static final byte ZERO_BYTE = "0".getBytes()[0];

    public static final byte SPACE_BYTE = " ".getBytes()[0];

    public static final char ZER0_CHAR = '0';

    public static final char SPACE_CHAR = ' ';

    public static final String SPACE_STRING = " ";

    public static final String ZERO_STRING = "0";

    // 判断手机号正则
    private static final String MPH_REGEX = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";

    private static final String NO_SPECIAL_SYMBOL = "^([0-9]|[a-z]|[A-Z])+$";

    private static final String IPV4_REGEX = "^(\\d){1,3}\\.(\\d){1,3}\\.(\\d){1,3}\\.(\\d){1,3}$";

    public static enum Fixdirection {
        LEFT("L"),
        RIGHT("R");
        private String value;
        Fixdirection(String value) {
            this.value = value;
        }
        public String value() {
            return value;
        }
    }

    /**
     * 如果字符串为null或者String.trim()后为空，则返回null。
     *
     * 可用此判断字符串是否为空并且得到trim后的值。
     */
    public static String trim(Object raw) {
        if (raw == null) {
            return null;
        }
        Assert.isTrue((raw instanceof String), "传入非String类型值");
        String str = ((String) raw).trim();
        if (str.length() == 0) {
            return null;
        }
        return str;
    }

    public static String getText(String rawString, String defaultString) {
        return hasText(rawString) ? trim(rawString) : trim(defaultString);
    }

    /**
     * 判断字符串是否存在非空格字符
     */
    public static boolean hasText(String... strs) {
        for (String str : strs) {
            if (trim(str) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是否为空
     *
     * @return 如果为null或长度为0，返回true，否则false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean hasLength(String str) {
        return str != null && str.length() != 0;
    }

    public static boolean isTrimEquals(String input1, String input2) {
        String str1 = trim(input1);
        String str2 = trim(input2);
        if (null == str1 || null == str2) {
            return false;
        }
        return str1.equals(str2);
    }

    /**
     * 右补字符串至某一固定长度
     * @param fillByte 补齐字节
     * @param length 补齐长度
     * @param charsetName 字符编码
     */
    public static String fixRightStringLength(String src, String charsetName, byte fillByte, int length) {
        return new String(fixBytesLength(src, charsetName, fillByte, length, Fixdirection.RIGHT));
    }

    /**
     * 右补字符串至某一固定长度
     * @param fillByte 补齐字节
     * @param length 补齐长度
     * @param charsetName 字符编码
     */
    public static String fixRightStringLength(byte[] srcBytes, byte fillByte, int length) {
        return new String(fixBytesLength(srcBytes, fillByte, length, Fixdirection.RIGHT));
    }

    /**
     * 左补字符串至某一固定长度
     * @param fillByte 补齐字节
     * @param length 补齐长度
     * @param charsetName 字符编码
     */
    public static String fixLeftStringLength(byte[] srcBytes, byte fillByte, int length) {
        return new String(fixBytesLength(srcBytes, fillByte, length, Fixdirection.LEFT));
    }

    /**
     * 左补字符串至某一固定长度
     * @param fillByte 补齐字节
     * @param length 补齐长度
     * @param charsetName 字符编码
     */
    public static String fixLeftStringLength(String src, String charsetName, byte fillByte, int length) {
        return new String(fixBytesLength(src, charsetName, fillByte, length, Fixdirection.LEFT));
    }

    public static byte[] fixBytesLength(String src, String charsetName, byte fillByte, int length, Fixdirection fixDirectory) {
        return fixBytesLength(src.getBytes(Charset.forName(charsetName)), fillByte, length, fixDirectory);
    }

    /**
     * 对字节数组进行补位
     * @param srcBytes	原字节数组
     * @param fillByte	填充字节
     * @param length	填充长度
     * @param fixDirectory	填充方向
     */
    public static byte[] fixBytesLength(byte[] srcBytes, byte fillByte, int length, Fixdirection fixDirectory) {
        int strLen = srcBytes.length;
        if (strLen == length) {
            return srcBytes;
        }
        byte[] newStrBytes = new byte[length];
        if (strLen < length) {
            switch (fixDirectory) {
                case LEFT:
                    Arrays.fill(newStrBytes, 0, length - strLen, fillByte);
                    System.arraycopy(srcBytes, 0, newStrBytes, length - strLen, strLen);
                    break;
                case RIGHT:
                    System.arraycopy(srcBytes, 0, newStrBytes, 0, strLen);
                    Arrays.fill(newStrBytes, strLen, length, fillByte);
                    break;
            }
        } else {
            // 超过截掉后面部分的字节
            System.arraycopy(srcBytes, 0, newStrBytes, 0, length);
        }
        return newStrBytes;
    }

    public static byte[] decodeHex(String source) {
        if (source == null) {
            return new byte[0];
        }
        if (source.length() % 2 != 0) {
            throw new IllegalArgumentException("输入字符串非法：字符数为奇数");
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
            for (int i = 0; i < source.length(); i += 2) {
                baos.write(Integer.valueOf(source.substring(i, i + 2), 16).intValue());
            }
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalArgumentException("输入字符串非法：含有非法字符", e);
        }
    }

    /**
     * 将字节转换为16进制的字符串
     */
    public static String encodeHex(byte[] source) {
        if (source == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : source) {
            String s = Integer.toHexString(b & 0xFF);
            if (s.length() < 2) {
                sb.append("0");
            }
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * 按ASCII排序输入的字符数组，每个字符串之间使用&连接
     */
    public static String sortString(String... strings) {
        if (strings.length == 0) {
            return null;
        }
        List<String> sortList = Arrays.asList(strings);
        Collections.sort(sortList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = sortList.size(); i < len; ++i) {
            sb.append(sortList.get(i));
            if (i + 1 < len) {
                sb.append("&");
            }
        }
        return sb.toString();
    }

    /**
     * 转换字符编码
     */
    public static byte[] changeCharset(byte[] src, String srcCharsetName, String targetCharsetName) {
        String srcStr = new String(src, Charset.forName(srcCharsetName));
        return srcStr.getBytes(Charset.forName(targetCharsetName));
    }

    /**
     * 检测字符串是否为手机号码
     */
    public static boolean isValidateMobliPhoneNumber(String text) {
        Assert.hasLength(text, "传入的字段为空");
        return RegexUtils.isMatch(trim(text), MPH_REGEX);
    }

    public static boolean isValiateIpv4Addr(String ip) {
        Assert.hasLength(ip, "传入的字段为空");
        ip = trim(ip);
        if (!RegexUtils.isMatch(ip, IPV4_REGEX)) {
            return false;
        }
        String[] vals = ip.split("\\.");
        if (Integer.valueOf(vals[0]) <= 0 || Integer.valueOf(vals[0]) >= 255 ||
                Integer.valueOf(vals[1]) < 0 || Integer.valueOf(vals[1]) > 255 ||
                Integer.valueOf(vals[1]) < 0 || Integer.valueOf(vals[1]) > 255 ||
                Integer.valueOf(vals[1]) < 1 || Integer.valueOf(vals[1]) > 255) {
            return false;
        }
        return true;
    }

    public static String formatString(String template, Object... strings) {
        return String.format(template, strings);
    }

    /**
     * 检测字符串是否含有特殊字符
     */
    public static boolean isNotSepcialSymbol(String text) {
        Assert.hasLength(text, "传入的字段为空");
        return RegexUtils.isMatch(trim(text), NO_SPECIAL_SYMBOL);
    }

    /**
     * 判断传入的字符串数组的值是否为非空
     */
    public static boolean areNotEmpty(String... values) {
        boolean result = true;
        if (values == null || values.length == 0) {
            result = false;
        } else {
            for (String value : values) {
                result &= !isEmpty(value);
            }
        }
        return result;
    }

    /**
     * 去除UTF-8的BOM
     */
    public static byte[] replaceUTF8BOM(byte[] srcBytes) {
        byte[] flagBytes = new byte[3];
        System.arraycopy(srcBytes, 0, flagBytes, 0, 3);
        if ("EFBBBF".equalsIgnoreCase(encodeHex(flagBytes))) {
            return ArrayUtils.splitByteArray(srcBytes, 3, srcBytes.length - 3);
        }
        return srcBytes;
    }

    public static String replace(String srcStr, char replaceChar, int offset, int replaceLength) {
        Assert.isTrue(!(offset < 0 || replaceLength < 0), formatString("参数错误"));
        if (!hasLength(srcStr) || srcStr.length() < offset) {
            return srcStr;
        }
        int actualReplaceLength = srcStr.length() > (offset + replaceLength) ? replaceLength : srcStr.length() - offset;
        StringBuilder stringBuilder = new StringBuilder(srcStr.length());
        stringBuilder.append(srcStr.substring(0, offset));
        for (int i=0; i<actualReplaceLength; ++i) {
            stringBuilder.append(replaceChar);
        }
        stringBuilder.append(srcStr.substring(offset + actualReplaceLength));
        return stringBuilder.toString();
    }

}

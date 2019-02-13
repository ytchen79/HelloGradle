package com.cyt.utils;

import com.cyt.utils.consts.CharsetNameEnum;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by cyt on 2017/12/20.
 */
public class NumberUtils {

    public final static BigDecimal MINUS_ONE = new BigDecimal("-1");

    public  final static BigDecimal ZERO = new BigDecimal(0);

    // 检测是是否为纯数字正则
    private static final String INTEGER_REGEX = "[0-9]+";

    // 检测是否为小数正则
    private static final String FLOAT_REGEX = "[0-9]+(\\.[0-9]+)*";

    /**
     * 检测字符串是否为浮点型
     */
    public static boolean isFloat(String text) {
        Assert.hasLength(text, "传入的字段为空");
        return RegexUtils.isMatch(text, INTEGER_REGEX);
    }

    /**
     * 检测字符串是否为纯数字
     */
    public static boolean isNumber(String text) {
        Assert.hasLength(text, "传入的字段为空");
        return RegexUtils.isMatch(text, INTEGER_REGEX);
    }

    /**
     * 设置BigDecimal精度(四舍五入)
     * @param bd 需要设置的BigDecimal类型
     * @param scale 精度
     * @return
     */
    public static BigDecimal setScale(BigDecimal bd, int scale) {
        return bd.setScale(scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 将整数装换为BigDecimal,并设置精度
     */
    public static BigDecimal setScale(int val, int scale) {
        BigDecimal bd = new BigDecimal(val);
        return bd.setScale(scale, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal setScale(double val, int scale) {
        BigDecimal bd = new BigDecimal(val);
        return bd.setScale(scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 判断两个BigDecimal是否相等
     */
    public static boolean isEq(BigDecimal bd1, BigDecimal bd2) {
        if (null == bd1 || null == bd2) {
            return false;
        }
        return bd1.compareTo(bd2) == 0;
    }

    /**
     * 判断bd1是否小于bd2
     */
    public static boolean isLt(BigDecimal bd1, BigDecimal bd2) {
        if (null == bd1 || null == bd2) {
            return false;
        }
        return bd1.compareTo(bd2) == -1;
    }

    /**
     * 判断bd1是否小于等于bd2
     */
    public static boolean isLe(BigDecimal bd1, BigDecimal bd2) {
        if (null == bd1 || null == bd2) {
            return false;
        }
        return bd1.compareTo(bd2) < 1;
    }

    /**
     * 将数字格式为AS400可处理的格式字符串
     */
    public static String formatAS400Number(BigDecimal bd, int scale, int length) {
        BigDecimal tmp = setScale(bd.multiply(new BigDecimal(Math.pow(10, scale))), 0);
        if (tmp.toBigInteger().compareTo(new BigInteger("0")) < 0) {
            return "-" + StringUtils.fixLeftStringLength(tmp.multiply(MINUS_ONE).toPlainString(), CharsetNameEnum.GBK.value(), StringUtils.ZERO_BYTE, length - 1);
        } else {
            return StringUtils.fixLeftStringLength(tmp.toPlainString(), CharsetNameEnum.GBK.value(), StringUtils.ZERO_BYTE, length);
        }
    }

    /**
     * 将AS400可处理的格式字符串转换为数字
     */
    public static BigDecimal parseAS400Number(String dataStr, int scale) {
        BigDecimal key = new BigDecimal(1);
        if (dataStr.startsWith("-")) {
            dataStr = dataStr.substring(1);
            key = MINUS_ONE;
        } else if(RegexUtils.isMatch(dataStr, "(\\d+)(\\D)$")) {
            dataStr = dataStr.substring(0, dataStr.length() - 1);
            key = MINUS_ONE;
        }
        BigDecimal bd = new BigDecimal(dataStr).multiply(key);
        return bd.divide(new BigDecimal(10).pow(scale));
    }

    public static String fenToYuan(long num) {
        return new BigDecimal(num).divide(new BigDecimal(100)).toPlainString();
    }
}

package com.cyt.utils;

import com.cyt.utils.json.JSONUtils;

/**
 * Created by cyt on 2018/3/8.
 */
public class SystemUtils {

    public static void sleep(long sleepMills) {
        try {
            Thread.sleep(sleepMills);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getEnv(String envName, String defaultValue) {
        return StringUtils.hasText(System.getenv(envName)) ?
                StringUtils.trim(System.getenv(envName)) : defaultValue;
    }

    public static <T> T getEnv(String envName, Class<? extends T> clazz) {
        String envString = getEnv(envName, "");
        Assert.isTrue(StringUtils.hasText(envString) && JSONUtils.isValidateJson(envString),
            StringUtils.formatString("环境变量%s参数格式异常，必须为json格式", envName));
        return JSONUtils.jsonToObject(envString, clazz);
    }

}

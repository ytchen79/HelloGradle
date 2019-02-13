package com.cyt.utils.json;

import com.cyt.utils.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Created by cyt on 2017/12/20.
 */
public class JSONUtils {

    public static boolean isValidateJson(String jsonStr) {
        try {
            ObjectMapper jsonMapper = new ObjectMapper();
            jsonMapper.readValue(jsonStr, Object.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String objectToJSON(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static <T> T jsonToObject(String jsonStr, Class<T> clazz) {
        if (StringUtils.trim(jsonStr) == null) {
            return null;
        }
        try {
            return new ObjectMapper().readValue(jsonStr, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getRawJsonContent(String jsonString, String key) {
        Map<String, Object> jsonMap = (Map<String, Object>) new JSONReader().read(jsonString);
        return jsonMap.get(key);
    }

}

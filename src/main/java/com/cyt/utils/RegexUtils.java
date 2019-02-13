package com.cyt.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cyt on 2017/12/20.
 */
public class RegexUtils {

    public static boolean isMatch(String text, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(text);
        if (!matcher.matches()) {
            return false;
        }
        return true;
    }
}

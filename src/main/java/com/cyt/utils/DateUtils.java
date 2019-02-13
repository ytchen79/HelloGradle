package com.cyt.utils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 日期操作类
 *
 * @author cyt
 */
public class DateUtils {

    public static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";
    public static final String HHmmss = "HHmmss";
    public static final String yyyyMMdd = "yyyyMMdd";
    public static final String ISO_8601_DATE_PATTERN = "yyyy-MM-dd";
    public static final String ISO_8601_TIME_PATTERN = "HH:mm:ss";
    public static final String ISO_8601_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String ISO_8601_DATE_TIME_PATTERN_V2 = "yyyy-MM-dd HH:mm:ss";
    public static final String HHmmss_CRON_PATTERN = "ss mm HH * * ? *";

    /**
     * 一天的毫秒数
     */
    public static final long DAY_MILLS = 24 * 3600 * 1000;
    /**
     * 一分钟的毫秒数
     */
    public static final long MIN_MILLS = 60 * 1000;
    /**
     * 一个月的毫秒数
     */
    public static final long ONE_MONTH_MILLS = 30 * DAY_MILLS;
    /**
     * 7天的毫秒数
     */
    public final static long _7DAYS_MILLS = 7 * 24 * 60 * 60 * 1000;

    public static String format(Date date, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    public static Date getDateByBetweenDay(Date baseDate, int between) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(baseDate);
        cal.add(Calendar.DATE, between);
        return new Date(cal.getTimeInMillis());
    }

    public static Date getDateByBetweenYear(Date baseDate, int between) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(baseDate);
        cal.add(Calendar.YEAR, between);
        return new Date(cal.getTimeInMillis());
    }

    /**
     * 获取传入日期的所在季度
     */
    public static int getQuarter(Date date) {
        if (null == date) {
            throw new RuntimeException("传入的日期不能为空");
        }
        int m = getMount(date);
        int quarter = (int) Math.ceil(m / 3.0);
        return quarter;
    }

    /**
     * 获取传入日期的所在年
     */
    public static int getYear(Date date) {
        if (null == date) {
            throw new RuntimeException("传入的日期不能为空");
        }
        return Integer.valueOf(format(date, "YYYY"));
    }

    /**
     * 获取传入日期的所在月
     */
    public static int getMount(Date date) {
        if (null == date) {
            throw new RuntimeException("传入的日期不能为空");
        }
        return Integer.valueOf(format(date, "MM"));
    }

    /**
     * 获取两个日期相差的天数
     */
    public static int getBetweendays(Date date1, Date date2) {
        return getBetween(date1, date2, yyyyMMdd, DAY_MILLS);
    }

    /**
     * 获取两个时间相差的分钟数
     */
    public static int getBetweenMins(Date date1, Date date2) {
        return getBetween(date1, date2, yyyyMMddHHmmss, MIN_MILLS);
    }

    private static int getBetween(Date date1, Date date2, String formatPattern, long unit) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(formatPattern);
            long date1Mills = sdf.parse(sdf.format(date1)).getTime();
            long date2Mills = sdf.parse(sdf.format(date2)).getTime();
            double betweenDays = Math.abs(date1Mills - date2Mills);
            return new BigDecimal(Math.ceil(betweenDays / unit)).intValue();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String formatYyyyMMddHHmmss() {
        return new SimpleDateFormat(yyyyMMddHHmmss).format(new Date());
    }

    public static String formatYyyyMMdd() {
        return new SimpleDateFormat(yyyyMMdd).format(new Date());
    }

    public static String formatHHmmss() {
        return new SimpleDateFormat(HHmmss).format(new Date());
    }

    public static String formatIS08601Date() {
        return new SimpleDateFormat(ISO_8601_DATE_PATTERN).format(new Date());
    }

    public static String formatIS08601Time() {
        return new SimpleDateFormat(ISO_8601_TIME_PATTERN).format(new Date());
    }

    public static String formatIS08601DateTime() {
        return new SimpleDateFormat(ISO_8601_DATE_TIME_PATTERN).format(new Date());
    }

    public static String formatIS08601DateTimeV2() {
        return new SimpleDateFormat(ISO_8601_DATE_TIME_PATTERN_V2).format(new Date());
    }

    public static Date parseYyyyMMddHHmmss(String date) {
        if (!(NumberUtils.isNumber(date) || StringUtils.trim(date).length() != 14)) {
            throw new RuntimeException(date + "为非指定日期格式！");
        }
        try {
            return new SimpleDateFormat(yyyyMMddHHmmss).parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static Date parseYyyyMMdd(String date) {
        if (!(NumberUtils.isNumber(date) || StringUtils.trim(date).length() != 8)) {
            throw new RuntimeException(date + "为非指定日期格式！");
        }
        return parse(date, yyyyMMdd);
    }

    public static Date parseHHmmss(String date) {
        if (!NumberUtils.isNumber(date) || StringUtils.trim(date).length() != 6) {
            throw new RuntimeException(date + "为非指定日期格式！");
        }
        return parse(date, HHmmss);
    }

    public static Date parse(String date, String formatPattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(formatPattern);
            sdf.setLenient(false);
            return sdf.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isFormatHHmmss(String date) {
        try {
            parseHHmmss(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isFormatDate(String date, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            sdf.setLenient(false);
            sdf.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static String parseHHmmssCronExpression(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(HHmmss_CRON_PATTERN);
        sdf.setLenient(false);
        return sdf.format(date);
    }

    public static String parseSsCronExpression(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("ss * * * * ? *");
        sdf.setLenient(false);
        return sdf.format(date);
    }

    public static int getSecondsBetweenNowAndNextDay(Date nowDate) {
        Date nextDay = parseYyyyMMddHHmmss(format(nowDate, yyyyMMdd) + "235800");
        return (int) ((nextDay.getTime() - nowDate.getTime()) / 1000);
    }

    public static int getSecondsBetweenSomeDateAndNextMonth(Date date) {
        return (int) ((date.getTime() + ONE_MONTH_MILLS - new Date().getTime()) / 1000);
    }

    public static String changeDateFormatter(String dateStr, String srcPattern, String targetPattern) {
        Date date = parse(dateStr, srcPattern);
        return format(date, targetPattern);
    }

    public static String getTransactionDay(int modify) {
        return format(new Date(System.currentTimeMillis() + modify * DAY_MILLS), yyyyMMdd);
    }
    public static void main(String[] args) {
        Set<String> workSet = new HashSet<>();
        Set<String> unWorkSet = new HashSet<>();
        workSet.add("20190202");
        workSet.add("20190203");
        unWorkSet.add("20190204");
        unWorkSet.add("20190205");
        unWorkSet.add("20190206");
        unWorkSet.add("20190207");
        unWorkSet.add("20190208");
        unWorkSet.add("20190209");
        unWorkSet.add("20190210");
        Date date = parse("20190211", "yyyyMMdd");
        if(DateUtils.isTDay(date, workSet, unWorkSet)){
            System.out.println("工作日");
        }else{
            System.out.println("不是工作日");
        }
        List<String> list = DateUtils.getT_1Days(date, workSet, unWorkSet);
        System.out.println("交易列表："+list);

    }
    private static String[] Week = {"星期一","星期二","星期三","星期四","星期五","星期六","星期日",};

    /**
     * 输入一个日期，周末补休假set，周一-周五休假set，返回输入日期是不是工作日（T日）
     * @param date 输入日期Date yyyyMMDD
     * @param workSet 周末补休假workSet
     * @param unWorkSet 周一-周五休假unWorkSet
     * @return
     */
    public static boolean isTDay(Date date,Set<String> workSet,Set<String> unWorkSet ){
        try{
            SimpleDateFormat dateFm = new SimpleDateFormat("EEEE");
            String week = dateFm.format(date);
            String dateStr = format(date,yyyyMMdd);
            System.out.println("输入日期："+dateStr);
            System.out.println("转换为："+week);
            if((Week[5].equals(week) ||  Week[6].equals(week))){//周六 周日
                return workSet.contains(dateStr) ? true : false;
            }else{//周一到周五
                return unWorkSet.contains(dateStr) ? false : true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 输入一个日期，周末补休假set，周一-周五休假set，返回输入日期需要处理的T-1日期列表
     * @param date
     * @param workSet
     * @param unWorkSet
     * @return
     */
    public static List<String> getT_1Days(Date date,Set<String> workSet,Set<String> unWorkSet){
        List<String> list = new ArrayList<>();
        if(isTDay(date,workSet,unWorkSet)){
            includeUnhandleDay(date,workSet, unWorkSet, list);
        }
        return list;
    }

    /**
     * 输入一个日期，周末补休假set，周一-周五休假set，处理列表 将需要要处理的T-1日期加入列表中
     * 注意：这里输入的日期必须是T日，即工作日。非工作日getT_1Days中已经判断
     * 检查前一天是不是T日，如果前一天是T日说明往前的日期已经被处理。加入T-1日，返回。
     * 如果前一天不是T日，说明T-2日需要处理。
     * @param date
     * @param workSet
     * @param unWorkSet
     * @param list
     */
    private static void includeUnhandleDay(Date date,Set<String> workSet,Set<String> unWorkSet, List<String> list){
        Date lastDate = getDateByBetweenDay(date, -1);
        if(isTDay(lastDate,workSet,unWorkSet)){//前一日如果是交易日，则处理前一日的账单即可，再往前的日期不用再处理。
            list.add(format(lastDate,yyyyMMdd));
            return;
        }else{//前一日如果不是T日，加入T-1 日之前未处理日期
            list.add(format(lastDate,yyyyMMdd));
            includeUnhandleDay(lastDate, workSet, unWorkSet, list);
        }
    }


}

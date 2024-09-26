package com.blackfire.aicloud.common.utils;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 日期处理
 */
public class DateUtils {
    public static final long ONE_DAY_MILLS = 3600000 * 24;
    /**
     * 时间格式(yyyy-MM-dd)
     */
    public final static String DATE_PATTERN = "yyyy-MM-dd";
    /**
     * 时间格式(yyyy-MM-dd HH:mm:ss)
     */
    public final static String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /**
     * 时间格式(yyyy-MM-dd'T'HH:mm:ss.SSSXXX)
     */
    public final static String DATE_TIME_PATTERN_XXX = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    /**
     * 时间格式(yyyy-MM-dd 00:00:00)
     */
    public final static String DATE_TIME_FORMAT_ZERO = "yyyy-MM-dd 00:00:00";
    /**
     * 时间格式(yyyy-MM-dd 23:59:59)
     */
    public final static String DATE_TIME_FORMAT_END = "yyyy-MM-dd 23:59:59";

    public static void main(String[] args) {
        System.out.println(getDaysGapOfDates(parse("2022-08-21", "yyyy-MM-dd"), new Date()));
    }

    public static String dateFormat(Date date) {
        return format(date, DATE_PATTERN);
    }

    public static String dateTimeformat(Date date) {
        return format(date, DATE_TIME_PATTERN);
    }

    public static String format(Date date, String pattern) {
        if (date != null) {
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            return df.format(date);
        }
        return null;
    }

    /**
     * 获取昨天的时间
     *
     * @return
     */
    public static Date getYesterdayTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, -24);
        return calendar.getTime();
    }

    /**
     * 获取昨天特殊的日期格式
     *
     * @param pattern
     * @return
     */
    public static Date getYesterdayTime(String pattern) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, -24);
        Date time = calendar.getTime();
        if (StringUtils.isEmpty(pattern)) {
            pattern = DATE_TIME_PATTERN;
        }
        String format = format(time, pattern);
        return parse(format, null);
    }

    /**
     * 获取前几天的开始时间
     *
     * @param pattern
     * @return
     */
    public static Date getLastDayTime(Integer days, String pattern) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -days);
        Date time = calendar.getTime();
        if (StringUtils.isEmpty(pattern)) {
            pattern = DATE_TIME_PATTERN;
        }
        String format = format(time, pattern);
        return parse(format, null);
    }

    /**
     * 获取前几周的开始时间
     *
     * @param pattern
     * @return
     */
    public static Date getLastWeekTime(Integer weeks, String pattern) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_MONTH, -weeks);
        Date time = calendar.getTime();
        if (StringUtils.isEmpty(pattern)) {
            pattern = DATE_TIME_PATTERN;
        }
        String format = format(time, pattern);
        return parse(format, null);
    }

    /**
     * 获取前几月的开始时间
     *
     * @param pattern
     * @return
     */
    public static Date getLastMonthTime(Integer months, String pattern) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -months);
        Date time = calendar.getTime();
        if (StringUtils.isEmpty(pattern)) {
            pattern = DATE_TIME_PATTERN;
        }
        String format = format(time, pattern);
        return parse(format, null);
    }

    /**
     * 获取前几年的开始时间
     *
     * @param pattern
     * @return
     */
    public static Date getLastYearTime(Integer years, String pattern) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -years);
        Date time = calendar.getTime();
        if (StringUtils.isEmpty(pattern)) {
            pattern = DATE_TIME_PATTERN;
        }
        String format = format(time, pattern);
        return parse(format, null);
    }

    /**
     * 将日期字符串转换为指定的时间
     *
     * @param date
     * @param pattern
     * @return
     */
    public static Date parse(String date, String pattern) {
        SimpleDateFormat df;
        if (date != null) {
            if (StringUtils.isEmpty(pattern)) {
                df = new SimpleDateFormat(DATE_TIME_PATTERN);
            } else {
                df = new SimpleDateFormat(pattern);
            }
            try {
                return df.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * java8(别的版本获取2月有bug) 获取某月最后一天的23:59:59
     *
     * @return
     */
    public static String getLastDayOfMonth(String datestr) {
        if (StringUtils.isBlank(datestr)) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = strToDateNotDD(datestr);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
        ;
        LocalDateTime endOfDay = localDateTime.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);
        Date dates = Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
        return sdf.format(dates);
    }

    /**
     * 将短时间格式字符串转换为时间 yyyy-MM( 2017-02)
     *
     * @param strDate
     * @return
     */
    public static Date strToDateNotDD(String strDate) {
        if (StringUtils.isBlank(strDate)) return null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;

    }

    public static Map<String, Date> getDate(Date startDate, Date endDate) {
        Map dateMap = new HashMap();
        String startParse = DateUtils.format(startDate, DateUtils.DATE_TIME_FORMAT_ZERO);
        String endParse = DateUtils.format(endDate, DateUtils.DATE_TIME_FORMAT_END);
        dateMap.put("startDate", DateUtils.parse(startParse, DateUtils.DATE_TIME_PATTERN));
        dateMap.put("endDate", DateUtils.parse(endParse, DateUtils.DATE_TIME_PATTERN));
        return dateMap;
    }

    private static int getDaysBetween(Date startDate, Date endDate) {
        return (int) ((endDate.getTime() - startDate.getTime()) / ONE_DAY_MILLS);
    }

    /**
     * 获取时间间隔
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static String getTimeIntervalString(Date startDate, Date endDate) {
        LocalDateTime fromDateTime = LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault());
        LocalDateTime toDateTime = LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault());
        LocalDateTime tempDateTime = LocalDateTime.from(fromDateTime);

        long days = tempDateTime.until(toDateTime, ChronoUnit.DAYS);
        tempDateTime = tempDateTime.plusDays(days);

        long hours = tempDateTime.until(toDateTime, ChronoUnit.HOURS);
        tempDateTime = tempDateTime.plusHours(hours);

        long minutes = tempDateTime.until(toDateTime, ChronoUnit.MINUTES);

        if (0L == days) {
            if (0L == hours) {
                if (0L == minutes) {
                    return "1分钟";
                }
                return String.format("%s分钟", minutes);
            }
            return String.format("%s小时%s分钟", hours, minutes);
        }
        return String.format("%s天%s小时%s分钟", days, hours, minutes);
    }

    /**
     * 计算两个日期之间的天数差
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static int getDaysGapOfDates(Date startDate, Date endDate) {
        int date = 0;
        if (startDate != null && endDate != null) {
            date = getDaysBetween(startDate, endDate);
        }
        return date;
    }

    public static Date getEndOfDay(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
        ;
        LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
        return Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }

    // 获得某天最小时间 2020-02-17 00:00:00
    public static Date getStartOfDay(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
        LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
        return Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }
}

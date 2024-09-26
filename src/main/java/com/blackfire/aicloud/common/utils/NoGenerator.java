package com.blackfire.aicloud.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @description: 序列号生成器
 * @author: ding ya
 * @create: 2022-06-17 10:23
 **/
public class NoGenerator {
    /**
     * 获取随机整数（4位）
     *
     * @return
     */
    public static String getRandomNo() {
        return ((int) ((Math.random() * 9 + 1) * 1000)) + "";
    }

    /**
     * 生成日期时间戳（到毫秒）
     */
    public static String getTimeStampMilliS() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    /**
     * 生成日期时间戳（到月）
     */
    public static String getTimeStampMonth() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

}
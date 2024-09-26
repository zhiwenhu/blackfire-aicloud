package com.blackfire.aicloud.common.constant;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
 public class DateTimeConstant {
     /**
      * 1900-01-01 00:00
      */
     public static final LocalDateTime DEFAULT_DATE_TIME = LocalDateTime.of(1900, 1, 1, 0, 0);

     /** yyyy-MM-dd HH:mm:ss */
     public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

     /** yyyy-MM-dd */
     public static final DateTimeFormatter DEFAULT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

     /** HH:mm:ss */
     public static final DateTimeFormatter DEFAULT_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

     private DateTimeConstant() {
     }
 }
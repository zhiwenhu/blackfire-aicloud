package com.blackfire.aicloud.common.utils;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhaominglei on 2020/11/9.
 */
public class StringUtil {

    public static void main(String[] args) {
        System.out.println(getRealPassword("xxx6774a40b-45ee-484d-8d86-1caf1b98b2c7yyy"));
    }

    public static String getRealPassword(String password) {
        if (StringUtils.isNotEmpty(password)) {
            String paswordOne = password.substring(3, password.length());
            return paswordOne.substring(0, paswordOne.length() - 3);
        }
        return password;
    }

    public static String stringFilter(String str) {
        // 只允许字母和数字
        String regEx = "[^a-zA-Z0-9]";
        // 清除掉所有特殊字符
//		String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    public static String encodeURI(String str, String charsetName) {
        charsetName = (charsetName == null || charsetName.equals("")) ? "utf-8" : charsetName;
        String s = str;
        if (s == null || s.equals("")) return s;
        try {
            s = URLEncoder.encode(str, charsetName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }
}

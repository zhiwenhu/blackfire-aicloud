package com.blackfire.aicloud.common.utils;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @description: 字符串处理相关
 * @author: ding ya
 * @create: 2022-07-30 13:13
 **/
public class UrlStringUtil {
    /**
     * 处理url列表字符串
     *
     * @param urlListStr
     * @return
     */
    public static List<String> handleUrlListString(String urlListStr) {
        if (StringUtils.isBlank(urlListStr)) {
            return new ArrayList<>();
        }
        return Arrays.asList(urlListStr.split(","));
    }

    /**
     * 处理url列表
     *
     * @param urlList
     * @return
     */
    public static String handleUrlList(List<String> urlList) {
        if (urlList == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < urlList.size(); i++) {
            sb.append(urlList.get(i));
            if (i != urlList.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
}

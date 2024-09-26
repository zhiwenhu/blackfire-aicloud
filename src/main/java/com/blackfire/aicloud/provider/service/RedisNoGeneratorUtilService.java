package com.blackfire.aicloud.provider.service;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Redis序列号生成工具服务实现类
 *
 * @author RX
 */
@Service
public class RedisNoGeneratorUtilService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String NO_KEY = "BAOSHAN_CHENGYUN_CASE_NO";

    /**
     * 获取下一个案件序列号
     *
     * @return
     */
    public String nextNo() {
        String preFix = LocalDateTimeUtil.format(LocalDateTime.now(), DatePattern.PURE_DATE_PATTERN);
        String thisKey = NO_KEY + preFix;
        if (redisTemplate.hasKey(thisKey)) {
            redisTemplate.opsForValue().increment(thisKey, 1);
        } else {
            redisTemplate.opsForValue().set(thisKey, 1);
            redisTemplate.expire(thisKey, 1000 * 60 * 60 * 24, TimeUnit.MILLISECONDS);
        }
        // 整形转字符串(6位数字字符串)
        String postFix = String.format("%06d", Long.parseLong(redisTemplate.opsForValue().get(thisKey).toString()));
        return preFix + postFix;
    }
}

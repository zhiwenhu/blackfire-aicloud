package com.blackfire.aicloud.common.aspect;


import com.blackfire.aicloud.common.annotation.DynamicDataSource;
import com.blackfire.aicloud.common.utils.StringUtils;
import com.blackfire.aicloud.provider.api.config.datasource.DynamicDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Order(-1)
@Component
@Slf4j
public class DynamicDataSourceAspect {


    @Pointcut("@annotation(com.blackfire.aicloud.common.annotation.DynamicDataSource)")
    public void dsPointcut() {

    }

    @Around("dsPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Map<String, String> map = parameterMap(joinPoint);
        DynamicDataSource changeDs = ((MethodSignature)joinPoint.getSignature()).getMethod().getAnnotation(DynamicDataSource.class);
        String dataBaseKey = map.get(changeDs.value());

        if (StringUtils.isNotEmpty(dataBaseKey)) {
            DynamicDataSourceContextHolder.setDataSource(dataBaseKey);
        }

        try {
            return joinPoint.proceed();
        } finally {
            DynamicDataSourceContextHolder.clearDataSource();
        }
    }

    /**
     * 获取参数map
     * @param joinPoint 切点
     * @return 参数名-参数值的map
     */
    private Map<String, String> parameterMap(JoinPoint joinPoint) {
        Object[] parameterValues = joinPoint.getArgs();
        String[] parameterNames = ((CodeSignature) (joinPoint.getSignature())).getParameterNames();
        HashMap<String, String> map = new HashMap<>();
        for (int i = 0; i < parameterNames.length; i++) {
            map.put(parameterNames[i], parameterValues[i].toString());
        }
        return map;
    }
}

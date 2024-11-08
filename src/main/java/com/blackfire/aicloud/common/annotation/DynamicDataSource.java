package com.blackfire.aicloud.common.annotation;


import java.lang.annotation.*;

/**
 * 自定义多数据源切换注解
 *
 * 优先级：先方法，后类，如果方法覆盖了类上的数据源类型，以方法的为准，否则以类上的为准
 *
 * @author smartpark
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface DynamicDataSource
{
    /**
     * 切换数据源名称
     */
    String value() default "datasourceKey";
}

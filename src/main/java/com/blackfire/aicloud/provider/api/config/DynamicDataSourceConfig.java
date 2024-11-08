package com.blackfire.aicloud.provider.api.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.blackfire.aicloud.common.constant.DataSourceType;
import com.blackfire.aicloud.provider.api.config.datasource.DynamicDataSource;
import com.blackfire.aicloud.provider.api.config.properties.DruidProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DynamicDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.druid.master")
    public DataSource masterDataSource(DruidProperties druidProperties)
    {
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        return druidProperties.dataSource(dataSource);
    }
    /**
     * 动态数据源
     */
    @Bean(name = "dynamicDataSource")
    @Primary
    public DynamicDataSource dynamicDataSource(DataSource masterDataSource) {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceType.MASTER.name(), masterDataSource);
        return new DynamicDataSource(masterDataSource, targetDataSources);
    }
}
package com.blackfire.aicloud.provider.api.config.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.blackfire.aicloud.common.constant.DataSourceType;
import com.blackfire.aicloud.common.utils.SpringUtils;
import com.blackfire.aicloud.common.utils.StringUtils;
import com.blackfire.aicloud.provider.api.config.properties.DruidProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

/**
 * 动态数据源
 * 
 * @author smartpark
 */
public class DynamicDataSource extends AbstractRoutingDataSource
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String DEFAULT_DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    private final Map<Object,Object> targetDataSourceMap;

    public DynamicDataSource(DataSource defaultTargetDataSource, Map<Object, Object> targetDataSources)
    {
        super.setDefaultTargetDataSource(defaultTargetDataSource);
        super.setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();
        this.targetDataSourceMap = targetDataSources;
    }

    @Override
    protected Object determineCurrentLookupKey()
    {
        return DynamicDataSourceContextHolder.getDataSource();
    }

    /**
     * 添加数据源信息
     * @param dataSource 数据源实体
     */
    public synchronized void createDataSource(TenantDataSource dataSource){
        try {
            // 数据库名作为key
            String datasourceKey= dataSource.getTenantKey();
            /* 检查目标数据源是否存在 */
            if (targetDataSourceMap.containsKey(datasourceKey))
            {
                /* 获取数据源*/
                DruidDataSource druidDataSource = (DruidDataSource) targetDataSourceMap.get(datasourceKey);
                Connection connection = null;
                try {
                    connection = druidDataSource.getConnection();
                } catch (SQLException e) {
                    log.error("数据库连接失败", e);
                } finally {
                    if(null != connection) {
                        connection.close();
                    }
                }
            } else {
                String driveClass = dataSource.getDriverClass();
                if (StringUtils.hasText(driveClass)) {
                    Class.forName(driveClass);
                } else {
                    driveClass = DEFAULT_DRIVER_CLASS;
                    Class.forName(DEFAULT_DRIVER_CLASS);
                }
                DriverManager.getConnection(dataSource.getDatabaseUrl(), dataSource.getDatabaseUsername(), dataSource.getDatabasePassword());
                //定义数据源
                DruidDataSource druidDataSource  = new DruidDataSource();
                druidDataSource.setName(dataSource.getDatabaseName());
                druidDataSource.setDriverClassName(driveClass);
                druidDataSource.setUrl(dataSource.getDatabaseUrl());
                druidDataSource.setUsername(dataSource.getDatabaseUsername());
                druidDataSource.setPassword(dataSource.getDatabasePassword());
                DruidProperties druidProperties = SpringUtils.getBean(DruidProperties.class);
                druidProperties.dataSource(druidDataSource);
                this.targetDataSourceMap.put(datasourceKey, druidDataSource );
                super.setTargetDataSources(this.targetDataSourceMap);
                // 将TargetDataSources中的连接信息放入resolvedDataSources管理
                super.afterPropertiesSet();
                log.info("数据源[{}]初始化成功", dataSource.getDatabaseName());
            }
        } catch (ClassNotFoundException | SQLException e) {
            log.error("数据源[{}]初始化失败", dataSource.getDatabaseName(), e);
        }
    }

    /**
     * 删除数据源
     * @param datasourceKey 数据源key
     */
    public synchronized void deleteDataSource(String datasourceKey) {
        if (!org.springframework.util.StringUtils.hasText(datasourceKey)) {
            throw new RuntimeException("remove parameter could not be empty");
        } else if (DataSourceType.MASTER.name().equals(datasourceKey)) {
            throw new RuntimeException("could not remove primary datasource");
        } else {
            if (this.targetDataSourceMap.containsKey(datasourceKey)) {
                DataSource dataSource = (DataSource) this.targetDataSourceMap.remove(datasourceKey);
                this.closeDataSource(datasourceKey, dataSource);
                log.info("dynamic-datasource - remove the database named [{}] success", datasourceKey);
            } else {
                log.warn("dynamic-datasource - could not find a database named [{}]", datasourceKey);
            }

        }
    }

    private void closeDataSource(String ds, DataSource dataSource) {
        try {
            if (dataSource instanceof DruidDataSource) {
                ((DruidDataSource)dataSource).close();
            } else {
                Method closeMethod = ReflectionUtils.findMethod(dataSource.getClass(), "close");
                if (closeMethod != null) {
                    closeMethod.invoke(dataSource);
                }
            }
        } catch (Exception e) {
            log.warn("dynamic-datasource close datasource named [{}] failed", ds, e);
        }

    }
    /**
     * 校验数据源是否存在
     * @param key 数据源保存的key
     * @return 返回结果，true：存在，false：不存在
     */
    public boolean existsDataSource(String key){
        return Objects.nonNull(this.targetDataSourceMap.get(key));
    }
}
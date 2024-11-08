package com.blackfire.aicloud.provider.api.config.datasource;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TenantDataSource {
    private String databaseUrl;
    private String databaseName;
    private String databaseUsername;
    private String databasePassword;
    private String tenantKey;
    private String driverClass;
}

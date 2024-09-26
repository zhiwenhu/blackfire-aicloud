package com.blackfire.aicloud.provider.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author hanbaoqiang
 */
@Component
@Data
@ConfigurationProperties(prefix = "ignore")
public class IgnoreUrlAuthConfig {

    private List<String> urls;
}

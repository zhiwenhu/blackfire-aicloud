package com.blackfire.aicloud;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@MapperScan("com.blackfire.aicloud.provider.dao.mapper")
@SpringBootApplication
@EnableDiscoveryClient
public class BlackfireAicloudApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlackfireAicloudApplication.class, args);
    }

}

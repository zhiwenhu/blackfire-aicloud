package com.blackfire.aicloud.provider.api.authority.Listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 防止SQL注入
 * </p>
 *
 * @author bf
 * @since 2020-09-21
 */
@WebListener
public class AntiCrackListener implements ServletContextListener {
    private Logger logger = LoggerFactory.getLogger(AntiCrackListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("liting: contextInitialized");
        logger.info("MyApplicationListener初始化成功");
        ServletContext context = sce.getServletContext();
        // IP存储器
        Map<String, Long[]> ipMap = new HashMap<>();
        context.setAttribute("ipMap", ipMap);
        // 限制IP存储器：存储被限制的IP信息
        Map<String, Long> limitedIpMap = new HashMap<>();
        context.setAttribute("limitedIpMap", limitedIpMap);
        logger.info("ipmap：" + ipMap.toString() + ";limitedIpMap:" + limitedIpMap.toString() + "初始化成功-----");
    }

}

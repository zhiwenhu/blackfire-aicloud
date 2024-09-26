package com.blackfire.aicloud.provider.api.authority.flter;

import com.blackfire.aicloud.common.utils.IPUtils;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 防止爆破
 * </p>
 *
 * @author bf
 * @since 2020-09-21
 */
@WebFilter(urlPatterns = "/*")
public class AntiCrackFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(AntiCrackFilter.class);

    /**
     * 默认限制时间（单位：ms）
     */
    private static final long LIMITED_TIME_MILLIS = 60 * 60 * 1000;
    /**
     * 用户连续访问最高阀值，超过该值则认定为恶意操作的IP，进行限制
     */
    private static final int LIMIT_NUMBER = 50;

    /**
     * 用户访问最小安全时间，在该时间内如果访问次数大于阀值，则记录为恶意IP，否则视为正常访问
     */
    private static final int MIN_SAFE_TIME = 1000;

    /**
     * 出错跳转页面 todo:待跳转页
     */
    public final static String IP_MAX_ERROR_PATH = "/templates/common/error.ftl";

    private FilterConfig config;

    @Override
    public void init(FilterConfig filterConfig) {
        this.config = filterConfig;    //设置属性filterConfig
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String ip = IPUtils.getIpAddr(request);
        ServletContext context = config.getServletContext();
        Map<String, Long> limitedIpMap = (Map<String, Long>) context.getAttribute("limitedIpMap");
        filterLimitedIpMap(limitedIpMap);

        if (isLimitedIP(limitedIpMap, ip)) {
            long limitedTime = limitedIpMap.get(ip) - System.currentTimeMillis();
            request.setAttribute("remainingTime", ((limitedTime / 1000) + (limitedTime % 1000 > 0 ? 1 : 0)));
            logger.error("IP访问过于频繁=>：" + ip);
            request.getRequestDispatcher(IP_MAX_ERROR_PATH).forward(request, response);
            return;
        }
        // 获取IP存储器
        Map<String, Long[]> ipMap = (Map<String, Long[]>) context.getAttribute("ipMap");
        // 判断存储器中是否存在当前IP，如果没有则为初次访问，初始化该ip
        // 如果存在当前ip，则验证当前ip的访问次数
        // 如果大于限制阀值，判断达到阀值的时间，如果不大于[用户访问最小安全时间]则视为恶意访问，跳转到异常页面
        if (ipMap.containsKey(ip)) {
            Long[] ipInfo = ipMap.get(ip);
            ipInfo[0] = ipInfo[0] + 1;
            logger.info("当前IP第[" + (ipInfo[0]) + "]次访问");
            if (ipInfo[0] > LIMIT_NUMBER) {
                Long ipAccessTime = ipInfo[1];
                Long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis - ipAccessTime <= MIN_SAFE_TIME) {
                    limitedIpMap.put(ip, currentTimeMillis + LIMITED_TIME_MILLIS);
                    request.setAttribute("remainingTime", ((LIMITED_TIME_MILLIS / 1000) + (LIMITED_TIME_MILLIS % 1000 > 0 ? 1 : 0)));
                    logger.error("IP访问过于频繁：" + ip);
                    request.getRequestDispatcher("/error/requestLimit").forward(request, response);
                    return;
                } else {
                    initIpVisitsNumber(ipMap, ip);
                }
            }
        } else {
            initIpVisitsNumber(ipMap, ip);
            logger.info("首次访问该网站");
        }
        context.setAttribute("ipMap", ipMap);
        filterChain.doFilter(request, response);
    }

    /**
     * @param limitedIpMap
     * @Description 过滤受限的IP，剔除已经到期的限制IP
     */
    private void filterLimitedIpMap(Map<String, Long> limitedIpMap) {
        if (limitedIpMap == null) {
            return;
        }
        Set<String> keys = limitedIpMap.keySet();
        Iterator<String> keyIt = keys.iterator();
        long currentTimeMillis = System.currentTimeMillis();
        while (keyIt.hasNext()) {
            long expireTimeMillis = limitedIpMap.get(keyIt.next());
            if (expireTimeMillis <= currentTimeMillis) {
                keyIt.remove();
            }
        }
    }

    /**
     * @param limitedIpMap
     * @param ip
     * @return true : 被限制 | false : 正常
     * @Description 是否是被限制的IP
     */
    private boolean isLimitedIP(Map<String, Long> limitedIpMap, String ip) {
        if (limitedIpMap.containsKey(ip)) {
            return true;
        }
        return false;
    }

    /**
     * 初始化用户访问次数和访问时间
     *
     * @param ipMap
     * @param ip
     */
    private void initIpVisitsNumber(Map<String, Long[]> ipMap, String ip) {
        Long[] ipInfo = new Long[2];
        ipInfo[0] = 0L;// 访问次数
        ipInfo[1] = System.currentTimeMillis();// 初次访问时间
        ipMap.put(ip, ipInfo);
    }
}

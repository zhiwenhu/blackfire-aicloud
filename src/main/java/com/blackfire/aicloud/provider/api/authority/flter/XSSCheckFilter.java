package com.blackfire.aicloud.provider.api.authority.flter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.util.Enumeration;

/**
 * <p>
 * xss参数防注入
 * </p>
 *
 * @author bf
 * @since 2020-09-21
 */
@SuppressWarnings("all")
@WebFilter(urlPatterns = "/*", filterName = "XSSCheckFilter", initParams = {@WebInitParam(name = "securityconfig", value = "/*")})
public class XSSCheckFilter implements Filter {

    private static Logger log = LoggerFactory.getLogger(XSSCheckFilter.class);

    private FilterConfig config;
    // 出错跳转的目的地
    private static String errorPath;
    // 不进行拦截的url
    private static String[] excludePaths;
    // 需要拦截的JS字符关键字
    private static String[] safeless;

    /******************** xss攻击防注入参数 begin ************************/
    // 出错跳转的目的地 todo:出错跳转的目的地 待替换
    public final static String XSS_ERROR_PATH = "/templates/common/error.ftl";
    // 不进行拦截的url todo:不进行拦截的url 待替换
    public final static String XSS_EXCLUDE_PATHS = "";
    // 需要拦截的JS字符关键字  todo: 待补充
    public final static String XSS_SAFELESS =
            "<script, </script, <iframe, </iframe, <frame, </frame, set-cookie, %3cscript, %3c/script, %3ciframe, %3c/iframe, %3cframe, %3c/frame, src=\"javascript:, <body, </body, %3cbody, %3c/body, <, >, </, />, %3c, %3e, %3c/, /%3e";

    /******************** xss攻击防注入参数 end ************************/

    public void init(FilterConfig config) throws ServletException {
        this.config = config;
        try {
            errorPath = XSS_ERROR_PATH;
            String excludePath = XSS_EXCLUDE_PATHS;
            if (!"".equals(excludePath) && null != excludePath)
                excludePaths = excludePath.split(",");
            String safeles = XSS_SAFELESS;
            if (!"".equals(safeles) && null != safeles) {
                safeless = safeles.split(",");
                log.debug(safeless.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {
        Enumeration params = req.getParameterNames();
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        boolean isSafe = true;
        String requestUrl = request.getRequestURI();
        if (isSafe(requestUrl)) {
            requestUrl = requestUrl.substring(requestUrl.indexOf("/"));
            if (!excludeUrl(requestUrl)) {
                String cache = "";
                while (params.hasMoreElements()) {
                    cache = req.getParameter((String) params.nextElement());
                    if (!"".equals(cache) && null != cache) {
                        if (!isSafe(cache)) {
                            isSafe = false;
                            break;
                        }
                    }
                }
            }
        } else {
            isSafe = false;
        }
        if (!isSafe) {
            request.setAttribute("err", "您输入的参数有非法字符，请输入正确的参数！");
            request.setAttribute("pageUrl", request.getRequestURI());
            request.getRequestDispatcher(errorPath).forward(request, response);
            return;
        }
        filterChain.doFilter(req, resp);
    }

    private static boolean isSafe(String str) {
        if (!"".equals(str) && null != str) {
            for (String s : safeless) {
                if (str.toLowerCase().contains(s))
                    return false;
            }
        }
        return true;
    }

    private boolean excludeUrl(String url) {
        if (excludePaths != null && excludePaths.length > 0) {
            for (String path : excludePaths) {
                if (url.toLowerCase().equals(path))
                    return true;
            }
        }
        return false;
    }

    public void destroy() {
    }

}

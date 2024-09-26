package com.blackfire.aicloud.provider.api.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.blackfire.aicloud.common.exception.BusinessException;
import com.blackfire.aicloud.common.exception.ErrorMessageEnum;
import com.blackfire.aicloud.provider.domain.resp.GatewayResp;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.ModelAttribute;

public abstract class AbstractController {

    protected HttpServletRequest httpServletRequest;

    protected HttpServletResponse httpServletResponse;

    protected HttpSession httpSession;

    @Autowired
    protected StringRedisTemplate redisTemplate;

    @ModelAttribute
    public void setReqAndResp(HttpServletRequest request, HttpServletResponse response) {
        this.httpServletRequest = request;
        this.httpServletResponse = response;
        this.httpSession = request.getSession();
    }

    /**
     * 根据token获取登录用户信息
     */
    public GatewayResp getLoginUserInfo() {
        String token = httpServletRequest.getHeader("token");
        if (StringUtils.isEmpty(token)) {
            token = httpServletRequest.getParameter("token");
        }
        String accessToken = redisTemplate.opsForValue().get(token);
        if (StringUtils.isEmpty(accessToken)) {
            throw new BusinessException(ErrorMessageEnum.UCENTER_TOKEN_HAS_EXPIRED.getErrCode(), ErrorMessageEnum.UCENTER_TOKEN_HAS_EXPIRED.getErrMsg());
        }
        return JSON.parseObject(accessToken, GatewayResp.class);
    }

    /**
     * 针对图片传值token校验
     */
    public GatewayResp getLoginUserInfo(String token) {
        String accessToken = redisTemplate.opsForValue().get(token);
        return JSON.parseObject(accessToken, GatewayResp.class);
    }
}

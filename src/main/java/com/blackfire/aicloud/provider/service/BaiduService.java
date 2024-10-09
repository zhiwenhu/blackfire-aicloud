package com.blackfire.aicloud.provider.service;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.blackfire.aicloud.common.exception.BusinessException;
import com.blackfire.aicloud.provider.domain.Token;
import com.blackfire.aicloud.provider.domain.req.BaiduChatMessage;
import com.blackfire.aicloud.provider.domain.req.ErnieBotTurboStreamParam;
import com.blackfire.aicloud.provider.domain.resp.ErnieBotTurboResponse;
import com.blackfire.aicloud.provider.listener.BaiduEventSourceListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.net.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Data
@Service
public class BaiduService {
    private static final long TIME_OUT = 30;
    /**
     * ERNIE_BOT_TURBO 发起会话接口
     */
    private static final String ERNIE_BOT_TURBO_INSTANT = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie_speed?access_token=";

    private OkHttpClient okHttpClient;
    @Value("${ai.baidu.api-key}")
    private String appKey;
    @Value("${ai.baidu.secret}")
    private String secretKey;

    /**
     * 获取Token
     * @param appKey
     * @param secretKey
     * @return
     */
    private String getToken(String appKey, String secretKey) {
        String url = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=" + appKey + "&client_secret=" + secretKey;
        String s = HttpUtil.get(url);
        Token bean = JSONUtil.toBean(s, Token.class);
        return bean.getAccess_token();
    }

    public BaiduService() {
        this.okHttpClient(30, 30, 30);
    }

    public BaiduService(@NonNull String appKey, @NonNull String secretKey, long connectTimeout, long writeTimeout, long readTimeout) {
        this.appKey = appKey;
        this.secretKey = secretKey;
        this.okHttpClient(connectTimeout, writeTimeout, readTimeout);
    }


    private void okHttpClient(long connectTimeout, long writeTimeout, long readTimeout) {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.connectTimeout(connectTimeout, TimeUnit.SECONDS);
        client.writeTimeout(writeTimeout, TimeUnit.SECONDS);
        client.readTimeout(readTimeout, TimeUnit.SECONDS);
        this.okHttpClient = client.build();
        okHttpClient.dispatcher().setMaxRequestsPerHost(200);
        okHttpClient.dispatcher().setMaxRequests(200);
    }

    // 该方法是同步请求API，会等大模型将数据完全生成之后，返回响应结果，可能需要等待较长时间，视生成文本长度而定
    public ErnieBotTurboResponse ernieBotTurbo(ErnieBotTurboStreamParam param) {
        if (param == null) {
            log.error("参数异常：param不能为空");
            throw new RuntimeException("参数异常：param不能为空");
        }
        if (param.isStream()) {
            param.setStream(false);
        }
        String post = HttpUtil.post(ERNIE_BOT_TURBO_INSTANT + getToken(appKey, secretKey), JSONUtil.toJsonStr(param));
        return JSONUtil.toBean(post, ErnieBotTurboResponse.class);
    }

    // 该方法是通过流的方式请求API，大模型每生成一些字符，就会通过流的方式相应给客户端，
    // 我们是在 BaiduEventSourceListener.java 的 onEvent 方法中获取大模型响应的数据，其中data就是具体的数据，
    // 我们获取到数据之后，就可以通过 SSE/webscocket 的方式实时相应给前端页面展示
    public void ernieBotTurboStream(String question, BaiduEventSourceListener eventSourceListener) {
        if (Objects.isNull(eventSourceListener)) {
            log.error("参数异常：EventSourceListener不能为空");
            throw new BusinessException("参数异常：EventSourceListener不能为空");
        }
        ErnieBotTurboStreamParam param = new ErnieBotTurboStreamParam();
        BaiduChatMessage userMsg = BaiduChatMessage.builder()
                .role(Role.USER.getValue())
                .content(question)
                .build();
        param.setMessages(Collections.singletonList(userMsg));
        param.setStream(true);
        try {
            EventSource.Factory factory = EventSources.createFactory(this.okHttpClient);
            ObjectMapper mapper = new ObjectMapper();
            String requestBody = mapper.writeValueAsString(param);
            Request request = new Request.Builder()
                    .url(ERNIE_BOT_TURBO_INSTANT + getToken(appKey, secretKey))
                    .post(RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), requestBody))
                    .build();
            //创建事件
            factory.newEventSource(request, eventSourceListener);
            eventSourceListener.getCountDownLatch().await();
        } catch (JsonProcessingException e) {
            log.error("请求参数解析是失败！", e);
            throw new RuntimeException("请求参数解析是失败！", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

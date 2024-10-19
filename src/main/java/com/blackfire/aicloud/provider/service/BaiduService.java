package com.blackfire.aicloud.provider.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.dashscope.common.Role;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.blackfire.aicloud.common.model.HttpResponseInfo;
import com.blackfire.aicloud.common.utils.HttpUtils;
import com.blackfire.aicloud.provider.domain.Token;
import com.blackfire.aicloud.provider.domain.req.BaiduChatMessage;
import com.blackfire.aicloud.provider.domain.req.ChatRequest;
import com.blackfire.aicloud.provider.domain.req.ErnieBotTurboStreamParam;
import com.blackfire.aicloud.provider.domain.resp.ErnieBotTurboResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@Data
@Service
public class BaiduService {
    private static final long TIME_OUT = 30;
    /**
     * ERNIE_BOT_TURBO 发起会话接口
     */
    private static final String AI_CHAT_URL = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/{model}?access_token=";
    private static final String ERNIE_SPEED_INSTANT = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-speed-128k?access_token=";
    private static final String YI34B_INSTANT = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/yi_34b_chat?access_token=";
    private static final String ERNIE_LITE_INSTANT = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-lite-8k?access_token=";

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
    public ErnieBotTurboResponse ernieBotTurbo(ChatRequest question, ErnieBotTurboStreamParam param) {
        if (param == null) {
            log.error("参数异常：param不能为空");
            throw new RuntimeException("参数异常：param不能为空");
        }
        if (param.isStream()) {
            param.setStream(false);
        }
        String requestUrl = ERNIE_SPEED_INSTANT;
        if (StringUtils.isNotBlank(question.getModel())) {
            requestUrl = AI_CHAT_URL.replace("{model}", question.getModel());
        }
        String post = HttpUtil.post(requestUrl + getToken(appKey, secretKey), JSONUtil.toJsonStr(param));
        return JSONUtil.toBean(post, ErnieBotTurboResponse.class);
    }

    // 该方法是通过流的方式请求API，大模型每生成一些字符，就会通过流的方式相应给客户端，
    // 我们是在 BaiduEventSourceListener.java 的 onEvent 方法中获取大模型响应的数据，其中data就是具体的数据，
    // 我们获取到数据之后，就可以通过 SSE/webscocket 的方式实时相应给前端页面展示
    public void ernieBotTurboStream(ChatRequest question, HttpServletResponse rp) {
        ErnieBotTurboStreamParam param = new ErnieBotTurboStreamParam();
        BaiduChatMessage userMsg = BaiduChatMessage.builder()
                .role(Role.USER.getValue())
                .content(question.getQuestion())
                .build();
        param.setMessages(Collections.singletonList(userMsg));
        param.setStream(true);
        try {
            ObjectMapper mapper = new ObjectMapper();
            String requestBody = mapper.writeValueAsString(param);
            String requestUrl = ERNIE_SPEED_INSTANT;
            if (StringUtils.isNotBlank(question.getModel())) {
                requestUrl = AI_CHAT_URL.replace("{model}", question.getModel());
            }
            log.info("请求大数据AI url：{}", requestUrl);
            HttpUtils.setHttpResponse(rp);
            HttpResponseInfo responseInfo = HttpUtils.sendSSLPostStream(requestUrl + getToken(appKey, secretKey), requestBody, null);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseInfo.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    rp.getWriter().write("data:" + JSON.parseObject(line).getString("result"));
                    rp.getWriter().flush();
                }
            } catch (IOException e) {
                log.error("数据解析失败！", e);
                throw new RuntimeException("数据解析失败！", e);
            } finally {
                responseInfo.close();
            }
        } catch (JsonProcessingException e) {
            log.error("请求参数解析是失败！", e);
            throw new RuntimeException("请求参数解析是失败！", e);
        }
    }

    public SseEmitter sseEmitterPostStream(ChatRequest question, HttpServletResponse rp) {
        ErnieBotTurboStreamParam param = new ErnieBotTurboStreamParam();
        BaiduChatMessage userMsg = BaiduChatMessage.builder()
                .role(Role.USER.getValue())
                .content(question.getQuestion())
                .build();
        param.setMessages(Collections.singletonList(userMsg));
        param.setStream(true);
        try {
            ObjectMapper mapper = new ObjectMapper();
            String requestBody = mapper.writeValueAsString(param);
            String requestUrl = ERNIE_SPEED_INSTANT;
            if (StringUtils.isNotBlank(question.getModel())) {
                requestUrl = AI_CHAT_URL.replace("{model}", question.getModel());
            }
            log.info("请求大数据AI url：{}", requestUrl);
            HttpUtils.setHttpResponse(rp);
            return HttpUtils.sseEmitterPostStream(requestUrl + getToken(appKey, secretKey), requestBody, null);
        } catch (JsonProcessingException e) {
            log.error("请求参数解析是失败！", e);
            throw new RuntimeException("请求参数解析是失败！", e);
        }
    }

    public Flux<String> fluxPostStream(ChatRequest question, HttpServletResponse rp) {
        ErnieBotTurboStreamParam param = new ErnieBotTurboStreamParam();
        BaiduChatMessage userMsg = BaiduChatMessage.builder()
                .role(Role.USER.getValue())
                .content(question.getQuestion())
                .build();
        param.setMessages(Collections.singletonList(userMsg));
        param.setStream(true);
        try {
            ObjectMapper mapper = new ObjectMapper();
            String requestBody = mapper.writeValueAsString(param);
            String requestUrl = ERNIE_SPEED_INSTANT;
            if (StringUtils.isNotBlank(question.getModel())) {
                requestUrl = AI_CHAT_URL.replace("{model}", question.getModel());
            }
            log.info("请求大数据AI url：{}", requestUrl);
            HttpUtils.setHttpResponse(rp);
            return HttpUtils.fluxPostStream(requestUrl + getToken(appKey, secretKey), requestBody, null);
        } catch (JsonProcessingException e) {
            log.error("请求参数解析是失败！", e);
            throw new RuntimeException("请求参数解析是失败！", e);
        }
    }

//    public void ernieBotTurboStream(ChatRequest question, BaiduEventSourceListener eventSourceListener) {
//        if (Objects.isNull(eventSourceListener)) {
//            log.error("参数异常：EventSourceListener不能为空");
//            throw new BusinessException("参数异常：EventSourceListener不能为空");
//        }
//        ErnieBotTurboStreamParam param = new ErnieBotTurboStreamParam();
//        BaiduChatMessage userMsg = BaiduChatMessage.builder()
//                .role(Role.USER.getValue())
//                .content(question.getQuestion())
//                .build();
//        param.setMessages(Collections.singletonList(userMsg));
//        param.setStream(true);
//        try {
//            EventSource.Factory factory = EventSources.createFactory(this.okHttpClient);
//            ObjectMapper mapper = new ObjectMapper();
//            String requestBody = mapper.writeValueAsString(param);
//            String requestUrl = ERNIE_SPEED_INSTANT;
//            if (StringUtils.isNotBlank(question.getModel())) {
//                requestUrl = AI_CHAT_URL.replace("{model}", question.getModel());
//            }
//            log.info("请求大数据AI url：{}", requestUrl);
//            Request request = new Request.Builder()
//                    .url(requestUrl + getToken(appKey, secretKey))
//                    .post(RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), requestBody))
//                    .build();
//            //创建事件
//            factory.newEventSource(request, eventSourceListener);
//            eventSourceListener.getCountDownLatch().await();
//        } catch (JsonProcessingException e) {
//            log.error("请求参数解析是失败！", e);
//            throw new RuntimeException("请求参数解析是失败！", e);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    }
}

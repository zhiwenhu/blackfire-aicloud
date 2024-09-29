package com.blackfire.aicloud.provider.service;

import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.DashScopeResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.protocol.ApiServiceOption;
import com.alibaba.dashscope.protocol.HalfDuplexRequest;
import com.alibaba.dashscope.protocol.HttpMethod;
import com.alibaba.dashscope.protocol.okhttp.OkHttpClientFactory;
import com.alibaba.dashscope.protocol.okhttp.OkHttpHttpClient;
import com.blackfire.aicloud.common.exception.BusinessException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

import java.util.Arrays;

@Service
@Slf4j
public class OpenaiService {
    @Value("${ai.openai.api-key}")
    private String apiKeyGpt;
    @Value("${ai.openai.base-url}")
    private String baseUrlGpt;
    @Value("${ai.openai.chat.option.model}")
    private String modelGpt;

    @Value("${ai.xunfei.api-key}")
    private String apiKeyXf;
    @Value("${ai.xunfei.base-url}")
    private String baseUrlXf;
    @Value("${ai.xunfei.chat.option.model}")
    private String modelXf;

    /**
     * OpenGPT
     * @param question
     * @return
     */
    public Flux<String> callOpenGPT(String question) {
        return callOpenAi(baseUrlGpt, apiKeyGpt, modelGpt, question);
    }

    /**
     * 讯飞大模型
     * @param question
     * @return
     */
    public Flux<String> callXunfei(String question) {
        return  callOpenAi(baseUrlXf, apiKeyXf ,modelXf, question);
    }

    /**
     * 共通openAI
     * @return Flux<String>
     */
    public Flux<String> callOpenAi(String url, String apiKey, String model, String question) {
        try {
            OpenAiApi api = new OpenAiApi(url, apiKey);
            OpenAiChatOptions options = new OpenAiChatOptions.Builder()
                    .withModel(model)
                    .withTopP(0.7F)
                    .withMaxTokens(200)
                    .build();
            OpenAiChatClient client = new OpenAiChatClient(api, options);
            return client.stream(question);
        } catch (Exception e) {
            log.error("大模型对接失败", e);
            return new Flux<>() {
                @Override
                public void subscribe(CoreSubscriber<? super String> coreSubscriber) {
                    coreSubscriber.onError(new BusinessException("内部错误，请联系管理员"));
                }
            };
        }
    }


}

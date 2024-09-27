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
    private String apiKey;
    @Value("${ai.openai.base-url}")
    private String baseUrl;
    @Value("${ai.openai.chat.option.model}")
    private String model;

    @Value("${ai.xunfei.api-key}")
    private String apiKeyXf;
    @Value("${ai.xunfei.base-url}")
    private String baseUrlXf;
    @Value("${ai.xunfei.chat.option.model}")
    private String modelXf;
    /**
     * openGPT
     * @param question
     * @return
     */
    public Flux<String> callOpenAi(String question) {
        try {
            OpenAiApi api = new OpenAiApi(baseUrl, apiKey);
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

    /**
     * 讯飞大模型
     * @param question
     * @return
     */
    public Flux<String> callXunfei(String question) {
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("You are a helpful assistant.")
                .build();
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(question)
                .build();
        GenerationParam param = GenerationParam.builder()
                .apiKey(apiKeyXf)
                .model(model)
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .topP(0.7)
                .maxTokens(200)
                .enableSearch(true)
                .incrementalOutput(true)
                .build();
        ApiServiceOption option = ApiServiceOption.builder()
                .baseHttpUrl(baseUrlXf)
                .isSSE(true)
                .httpMethod(HttpMethod.POST).build();

        OkHttpHttpClient client = new OkHttpHttpClient(OkHttpClientFactory.getOkHttpClient());
        HalfDuplexRequest request = new HalfDuplexRequest(param, option);
        return Flux.create(emitter -> {
            try {
                client.streamOut(request, new ResultCallback<DashScopeResult>() {
                    @Override
                    public void onEvent(DashScopeResult generationResult) {
                        String content = generationResult.getOutput().toString();
                        log.info("响应数据：" + content);
                        emitter.next(content);
                    }

                    @Override
                    public void onComplete() {
                        log.info("模型返回数据完毕");
                        emitter.complete();
                    }

                    @Override
                    public void onError(Exception e) {
                        log.error("模型返回未知原因失败", e);
                        emitter.error(new BusinessException(e.getMessage()));
                    }
                });
            } catch (Exception e) {
                log.error("大模型对接失败", e);
                emitter.error(new BusinessException("内部错误，请联系管理员"));
            }
        });
    }
}

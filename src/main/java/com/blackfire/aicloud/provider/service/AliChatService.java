package com.blackfire.aicloud.provider.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.blackfire.aicloud.common.exception.BusinessException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

@Service
@Slf4j
public class AliChatService {

    @Value("${ai.ali-tongyi.api-key}")
    private String aliKey;
    @Value("${ai.ali-tongyi.model}")
    private String model;
    @Value("${ai.ali-tongyi.stream}")
    private String stream;

    /**
     * 一次询问
     * @param question
     * @return
     */
    public String callWithMessage(String question) {
        try {
            Generation gen = new Generation();
            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content("You are a helpful assistant.")
                    .build();
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(question)
                    .build();
            GenerationParam param = GenerationParam.builder()
                    .apiKey(aliKey)
                    .model(model)
                    .messages(Arrays.asList(systemMsg, userMsg))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();
            GenerationResult result = gen.call(param);
            List<GenerationOutput.Choice> array = result.getOutput().getChoices();
            if (array != null && !array.isEmpty()) {
                return array.get(0).getMessage().getContent();
            } else {
                return "";
            }
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * 流式输出
     * @param question
     * @return
     */
    public Flux<String> callWithStream(String question) {
        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("You are a helpful assistant.")
                .build();
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(question)
                .build();
        GenerationParam param = GenerationParam.builder()
                .apiKey(aliKey)
                .model(model)
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .topP(0.3)
                .enableSearch(true)
                .incrementalOutput(true)
                .build();

        return Flux.create(emitter -> {
            try {
                gen.streamCall(param, new ResultCallback<GenerationResult>() {
                    @Override
                    public void onEvent(GenerationResult generationResult) {
                        String content = generationResult.getOutput().getChoices().get(0).getMessage().getContent();
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

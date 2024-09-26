package com.blackfire.aicloud.provider.service;

import com.blackfire.aicloud.common.exception.BusinessException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.stereotype.Service;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class OpenaiService {
    @Resource
    private StreamingChatClient client;

    /**
     * 流式输出
     * @param question
     * @return
     */
    public Flux<String> callWithStream(String question) {
        try {
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

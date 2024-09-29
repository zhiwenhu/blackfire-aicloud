package com.blackfire.aicloud.provider.api.controller;

import com.alibaba.nacos.common.utils.StringUtils;
import com.blackfire.aicloud.common.exception.BusinessException;
import com.blackfire.aicloud.provider.domain.req.ChatBody;
import com.blackfire.aicloud.provider.domain.resp.GatewayResp;
import com.blackfire.aicloud.provider.listener.BaiduEventSourceListener;
import com.blackfire.aicloud.provider.service.AliChatService;
import com.blackfire.aicloud.provider.service.BaiduService;
import com.blackfire.aicloud.provider.service.OpenaiService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat")
public class AiChatController extends AbstractController{

    @Autowired
    private AliChatService aliChatService;
    @Autowired
    private OpenaiService openaiService;
    @Autowired
    private BaiduService baiduService;

    @PostMapping("/ali")
    public GatewayResp chatAliMessage(@RequestBody ChatBody quest){
        if (StringUtils.hasLength(quest.getQuestion())) {
            return GatewayResp.ok(aliChatService.callWithMessage(quest.getQuestion()));
        }
        throw new BusinessException("请输入你的问题。");
    }

    @PostMapping(value = "/ali/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatAliStream(@RequestBody ChatBody quest){
        if (StringUtils.hasLength(quest.getQuestion())) {
            return aliChatService.callWithStream(quest.getQuestion());
        }
        throw new BusinessException("请输入你的问题。");
    }

    @PostMapping(value = "/openai", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatOpenaiStream(@RequestBody ChatBody quest){
        if (StringUtils.hasLength(quest.getQuestion())) {
            return openaiService.callOpenGPT(quest.getQuestion());
        }
        throw new BusinessException("请输入你的问题。");
    }

    @PostMapping(value = "/xunfei", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatXunfeiStream(@RequestBody ChatBody quest){
        if (StringUtils.hasLength(quest.getQuestion())) {
            return openaiService.callXunfei(quest.getQuestion());
        }
        throw new BusinessException("请输入你的问题。");
    }

    @PostMapping(value = "/baidu", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void baiduStream(@RequestBody ChatBody quest, HttpServletResponse rp){
        if (StringUtils.hasLength(quest.getQuestion())) {
            BaiduEventSourceListener listener = new BaiduEventSourceListener(rp);
            baiduService.ernieBotTurboStream(quest.getQuestion(), listener);
        }
        throw new BusinessException("请输入你的问题。");
    }
}

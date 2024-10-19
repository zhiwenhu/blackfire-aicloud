package com.blackfire.aicloud.provider.api.controller;

import com.alibaba.nacos.common.utils.StringUtils;
import com.blackfire.aicloud.common.exception.BusinessException;
import com.blackfire.aicloud.provider.domain.req.ChatRequest;
import com.blackfire.aicloud.provider.domain.resp.GatewayResp;
import com.blackfire.aicloud.provider.listener.BaiduEventSourceListener;
import com.blackfire.aicloud.provider.service.AliChatService;
import com.blackfire.aicloud.provider.service.BaiduService;
import com.blackfire.aicloud.provider.service.OpenaiService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
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
    public GatewayResp chatAliMessage(@RequestBody ChatRequest quest){
        if (StringUtils.hasLength(quest.getQuestion())) {
            return GatewayResp.ok(aliChatService.callWithMessage(quest.getQuestion()));
        }
        throw new BusinessException("请输入你的问题。");
    }

    @PostMapping(value = "/ali/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatAliStream(@RequestBody ChatRequest quest){
        if (StringUtils.hasLength(quest.getQuestion())) {
            return aliChatService.callWithStream(quest.getQuestion());
        }
        throw new BusinessException("请输入你的问题。");
    }

    @PostMapping(value = "/openai", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatOpenaiStream(@RequestBody ChatRequest quest){
        if (StringUtils.hasLength(quest.getQuestion())) {
            return openaiService.callOpenGPT(quest.getQuestion());
        }
        throw new BusinessException("请输入你的问题。");
    }

    @PostMapping(value = "/xunfei", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatXunfeiStream(@RequestBody ChatRequest quest){
        if (StringUtils.hasLength(quest.getQuestion())) {
            return openaiService.callXunfei(quest.getQuestion());
        }
        throw new BusinessException("请输入你的问题。");
    }

    @PostMapping(value = "/baidu", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void baiduStream(@RequestBody @Validated ChatRequest quest, HttpServletResponse rp){
        if (StringUtils.hasLength(quest.getQuestion())) {
//            BaiduEventSourceListener listener = new BaiduEventSourceListener(rp);
            baiduService.ernieBotTurboStream(quest, rp);
        } else {
            throw new BusinessException("请输入你的问题。");
        }
    }

    @PostMapping(value = "/baiduSse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter baiduSseEmitterStream(@RequestBody @Validated ChatRequest quest, HttpServletResponse rp){
        if (StringUtils.hasLength(quest.getQuestion())) {
//            BaiduEventSourceListener listener = new BaiduEventSourceListener(rp);
            return baiduService.sseEmitterPostStream(quest, rp);
        } else {
            throw new BusinessException("请输入你的问题。");
        }
    }

    @PostMapping(value = "/baiduFlux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> baiduFluxStream(@RequestBody @Validated ChatRequest quest, HttpServletResponse rp){
        if (StringUtils.hasLength(quest.getQuestion())) {
//            BaiduEventSourceListener listener = new BaiduEventSourceListener(rp);
            return baiduService.fluxPostStream(quest, rp);
        } else {
            throw new BusinessException("请输入你的问题。");
        }
    }
}

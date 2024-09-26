package com.blackfire.aicloud.provider.api.controller;

import com.blackfire.aicloud.provider.domain.req.ChatBody;
import com.blackfire.aicloud.provider.domain.resp.GatewayResp;
import com.blackfire.aicloud.provider.service.AliChatService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController("/chat")
public class AiChatController extends AbstractController{

    @Autowired
    private AliChatService aliChatService;
    @PostMapping("/ali")
    public GatewayResp chatMessage(@RequestBody ChatBody quest){
        return GatewayResp.ok(aliChatService.callWithMessage(quest.getQuestion()));
    }

    @PostMapping(value = "/ali/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatBody quest, HttpServletResponse response){
        return aliChatService.callWithStream(quest.getQuestion(), response);
    }
}

package com.blackfire.aicloud.provider.api.controller;

import com.blackfire.aicloud.provider.domain.resp.GatewayResp;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController extends AbstractController{

    @GetMapping("/test")
    public GatewayResp testHello(){
        return GatewayResp.ok("Thank you for your visit.");
    }
}

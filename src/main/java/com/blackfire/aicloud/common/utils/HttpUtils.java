package com.blackfire.aicloud.common.utils;

import cn.hutool.http.ContentType;
import com.alibaba.fastjson.JSON;
import com.blackfire.aicloud.common.model.HttpResponseInfo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HttpUtils {
    public static void setHttpResponse(HttpServletResponse rp) {
        rp.setCharacterEncoding("UTF-8");
        rp.setHeader("Cache-Control", "no-cache");
        rp.setHeader("connection", "keep-alive");
        rp.setHeader("Access-Control-Allow-Origin", "*");  // 允许跨域
    }

    public static HttpResponseInfo sendSSLPostStream(String url, String contentString, Map<String, String> headerMap) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(RequestBody.create(contentString, MediaType.parse(ContentType.JSON.getValue())));
        if (headerMap != null) {
            builder.headers(Headers.of(headerMap));
        }

        HttpResponseInfo responseInfo = new HttpResponseInfo();
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(50, TimeUnit.SECONDS).readTimeout(10, TimeUnit.MINUTES).build();
        EventSource.Factory factory = EventSources.createFactory(client);
        factory.newEventSource(builder.build(), responseInfo);
        return responseInfo;
    }

    public static SseEmitter sseEmitterPostStream(String url, String contentString, Map<String, String> headerMap) {
        SseEmitter sseEmitter = new SseEmitter();
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(RequestBody.create(contentString, MediaType.parse(ContentType.JSON.getValue())));
        if (headerMap != null) {
            builder.headers(Headers.of(headerMap));
        }

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(50, TimeUnit.SECONDS).readTimeout(10, TimeUnit.MINUTES).build();
        EventSource.Factory factory = EventSources.createFactory(client);
        factory.newEventSource(builder.build(), new EventSourceListener() {
            @Override
            public void onClosed(@NotNull EventSource eventSource) {
                super.onClosed(eventSource);
                sseEmitter.complete();
                log.info("Connection closed.");
            }

            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                super.onEvent(eventSource, id, type, data);
                try {
                    sseEmitter.send(JSON.parseObject(data).getString("result"));
                } catch (IOException e) {
                    eventSource.cancel();
                    log.error("数据接收失败", e);
                    sseEmitter.completeWithError(e);
                }
            }

            @Override
            public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                super.onFailure(eventSource, t, response);
                sseEmitter.completeWithError(t);
                log.error("Connection failed.", t);
            }

            @Override
            public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
                super.onOpen(eventSource, response);
                log.info("Connection opened.");
            }
        });
        return sseEmitter;
    }

    public static Flux<String> fluxPostStream(String url, String contentString, Map<String, String> headerMap) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(RequestBody.create(contentString, MediaType.parse(ContentType.JSON.getValue())));
        if (headerMap != null) {
            builder.headers(Headers.of(headerMap));
        }

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(50, TimeUnit.SECONDS).readTimeout(10, TimeUnit.MINUTES).build();
        EventSource.Factory factory = EventSources.createFactory(client);
        return  Flux.create(emitter -> {
            factory.newEventSource(builder.build(), new EventSourceListener() {
                @Override
                public void onClosed(@NotNull EventSource eventSource) {
                    super.onClosed(eventSource);
                    emitter.complete();
                    log.info("Connection closed.");
                }

                @Override
                public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                    super.onEvent(eventSource, id, type, data);
                    try {
                        emitter.next(JSON.parseObject(data).getString("result"));
                    } catch (Exception e) {
                        eventSource.cancel();
                        emitter.error(e);
                        log.error("数据接收失败", e);
                    }
                }

                @Override
                public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                    super.onFailure(eventSource, t, response);
                    emitter.error(t);
                }

                @Override
                public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
                    super.onOpen(eventSource, response);
                    log.info("Connection opened.");
                }
            });
        });
    }
}

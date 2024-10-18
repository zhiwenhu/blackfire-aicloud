package com.blackfire.aicloud.common.model;

import cn.hutool.core.io.IoUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class HttpResponseInfo extends EventSourceListener {
    private PipedInputStream inputStream;
    private PipedOutputStream outputStream;
    private Response sseResponse;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public InputStream getInputStream() {
        try {
            countDownLatch.await();
            if (inputStream == null) {
                throw new IllegalStateException("Connection failed");
            }
            return inputStream;
        } catch (InterruptedException e) {
            throw new RuntimeException("Connection failed", e);
        }
    }

    public Response getResponse() {
        return sseResponse;
    }

    @Override
    public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
        this.outputStream = new PipedOutputStream();
        try {
            this.inputStream = new PipedInputStream(outputStream);
            this.sseResponse = response;
            countDownLatch.countDown();
        } catch (IOException e) {
            log.info("Connection failed.", e);
            this.sseResponse.close();
        }
        log.info("Connection opened.");
    }
    @Override
    public void onClosed(@NotNull EventSource eventSource) {
        IoUtil.close(outputStream);
        log.info("Connection closed.");
    }
    @Override
    public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
        try {
            outputStream.write(data.concat("\n").getBytes());
            outputStream.flush();
        } catch (Exception e) {
            eventSource.cancel();
            log.error("data event failed", e);
        }
    }
    @Override
    public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
        eventSource.cancel();
        countDownLatch.countDown();
        log.error("Connection failed.", t);
    }

    public void close() {
        IoUtil.close(outputStream);
        IoUtil.close(inputStream);
    }
}

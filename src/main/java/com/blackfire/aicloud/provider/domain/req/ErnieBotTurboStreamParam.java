package com.blackfire.aicloud.provider.domain.req;

import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class ErnieBotTurboStreamParam {

    private List<BaiduChatMessage> messages;
    private Boolean stream;
    private String user_id;

    public boolean isStream() {
        return Objects.equals(this.stream, true);
    }
}

package com.queueless.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PublicQueueResponse {

    private String shopName;

    // 🔔 Token currently being served
    private int currentToken;

    private Integer yourToken;
    private int peopleAhead;
    private int estimatedWaitMinutes;
    private boolean queueOpen;
}

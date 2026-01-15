package com.queueless.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PublicQueueResponse {

    private String shopName;
    private int currentToken;
    private Integer yourToken;          // nullable
    private int peopleAhead;
    private int estimatedWaitMinutes;
    private boolean queueOpen;
}

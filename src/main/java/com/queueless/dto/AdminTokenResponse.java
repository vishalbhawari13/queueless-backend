package com.queueless.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AdminTokenResponse {

    private UUID tokenId;
    private int tokenNumber;
    private String status;
    private String customerName;
    private String phone;

    private UUID queueId;
    private int currentToken;
}

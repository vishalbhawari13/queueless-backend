package com.queueless.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TokenRequest {
    private UUID shopId;
    private String customerName;
    private String phone;
}

package com.queueless.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteTokenRequest {

    @NotNull
    @Positive(message = "Bill amount must be positive")
    private Integer billAmount;

    private String serviceType;
}

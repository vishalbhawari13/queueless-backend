package com.queueless.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shop {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String phone;

    // 📍 LOCATION (NEW)
    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    // Allowed distance in meters (e.g. 100m)
    @Column(nullable = false)
    private int allowedRadiusMeters;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

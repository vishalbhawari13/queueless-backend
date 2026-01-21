package com.queueless.entity;

import com.queueless.entity.enums.TokenStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "tokens",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"queue_id", "token_number"})
        },
        indexes = {
                @Index(name = "idx_token_status", columnList = "status"),
                @Index(name = "idx_token_created_at", columnList = "createdAt")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {

    @Id
    @GeneratedValue
    private UUID id;

    /* ===============================
       🔗 RELATIONSHIPS
       =============================== */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "queue_id", nullable = false)
    private Queue queue;

    /* ===============================
       🎟️ TOKEN INFO
       =============================== */
    @Column(name = "token_number", nullable = false)
    private int tokenNumber;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false, length = 15)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenStatus status;

    /* ===============================
       💰 BILLING
       =============================== */
    @Column
    private Integer billAmount; // ₹

    @Column
    private String serviceType;

    /* ===============================
       ⏱️ TIMESTAMP
       =============================== */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

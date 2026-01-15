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

    @ManyToOne
    @JoinColumn(name = "queue_id", nullable = false)
    private Queue queue;

    @Column(name = "token_number", nullable = false)
    private int tokenNumber;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenStatus status;

    // 💰 BILLING (NEW)
    @Column
    private Integer billAmount; // ₹

    @Column
    private String serviceType;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

package com.queueless.entity;

import com.queueless.entity.enums.QueueStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "queues",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"shop_id", "queue_date"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Queue {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueueStatus status;

    @Column(nullable = false)
    private int currentToken = 0;

    // ⏱ Average service time per customer (minutes)
    @Column(nullable = false)
    private int avgServiceTimeMinutes;

    @Column(name = "queue_date", nullable = false)
    private LocalDate queueDate;
}

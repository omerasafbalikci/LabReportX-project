package com.lab.backend.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity(name = "tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Token {
    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    private String id;

    @Column(name = "revoked")
    private boolean revoked;

    @Column(name = "expiration_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expirationDate;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;
}

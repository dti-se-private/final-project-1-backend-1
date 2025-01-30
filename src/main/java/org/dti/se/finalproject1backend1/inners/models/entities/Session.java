package org.dti.se.finalproject1backend1.inners.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "session")
public class Session {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    private String accessToken;

    private String refreshToken;

    private OffsetDateTime accessTokenExpiredAt;

    private OffsetDateTime refreshTokenExpiredAt;

}
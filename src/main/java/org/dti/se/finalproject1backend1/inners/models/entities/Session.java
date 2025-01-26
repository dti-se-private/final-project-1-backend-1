package org.dti.se.finalproject1backend1.inners.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "session")
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "access_token", length = Integer.MAX_VALUE)
    private String accessToken;

    @Column(name = "refresh_token", length = Integer.MAX_VALUE)
    private String refreshToken;

    @Column(name = "access_token_expired_at")
    private OffsetDateTime accessTokenExpiredAt;

    @Column(name = "refresh_token_expired_at")
    private OffsetDateTime refreshTokenExpiredAt;

}
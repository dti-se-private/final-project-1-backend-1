package org.dti.se.finalproject1backend1.inners.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "verification")
public class Verification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "email", nullable = false, length = Integer.MAX_VALUE)
    private String email;

    @NotNull
    @Column(name = "type", nullable = false, length = Integer.MAX_VALUE)
    private String type;

    @NotNull
    @Column(name = "code", nullable = false, length = Integer.MAX_VALUE)
    private String code;

    @NotNull
    @Column(name = "init_time", nullable = false)
    private OffsetDateTime initTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private OffsetDateTime endTime;

}
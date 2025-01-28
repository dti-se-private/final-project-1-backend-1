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
    @Column(name = "id", nullable = false)
    private UUID id;

    private String email;

    private String type;

    private String code;

    @NotNull
    @Column(name = "init_time", nullable = false)
    private OffsetDateTime initTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private OffsetDateTime endTime;

}
package org.dti.se.finalproject1backend1.inners.models.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private UUID id;

    private String email;

    private String type;

    private String code;

    private OffsetDateTime initTime;

    private OffsetDateTime endTime;

}
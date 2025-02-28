package org.dti.se.finalproject1backend1.inners.models.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.finalproject1backend1.outers.configurations.serdes.HexStringDeserializer;

import java.time.OffsetDateTime;
import java.util.UUID;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "payment_proof")
public class PaymentProof {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @JsonDeserialize(using = HexStringDeserializer.class)
    private byte[] file;

    private String extension;

    private OffsetDateTime time;

}
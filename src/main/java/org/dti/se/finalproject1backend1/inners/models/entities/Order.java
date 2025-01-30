package org.dti.se.finalproject1backend1.inners.models.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.spatial.dialect.postgis.PGGeographyJdbcType;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "order")
public class Order {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    private Double totalPrice;

    private Double shipmentPrice;

    private Double itemPrice;

    @OneToMany(mappedBy = "order")
    @Builder.Default
    private Set<OrderStatus> orderStatuses = new LinkedHashSet<>();

    @OneToMany(mappedBy = "order")
    @Builder.Default
    private Set<OrderItem> orderItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "order")
    @Builder.Default
    private Set<PaymentProof> paymentProofs = new LinkedHashSet<>();

    private PGGeographyJdbcType shipmentOrigin;

    private PGGeographyJdbcType shipmentDestination;
}
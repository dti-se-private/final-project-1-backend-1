package org.dti.se.finalproject1backend1.inners.models.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

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

    private UUID accountId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    private Double totalPrice;

    private Double shipmentPrice;

    private Double itemPrice;

    @OneToMany(mappedBy = "order")
    private Set<OrderStatus> orderStatuses = new LinkedHashSet<>();

    @OneToMany(mappedBy = "order")
    private Set<OrderItem> orderItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "order")
    private Set<PaymentProof> paymentProofs = new LinkedHashSet<>();

/*
 TODO [Reverse Engineering] create field to map the 'shipment_origin' column
 Available actions: Define target Java type | Uncomment as is | Remove column mapping
    @Column(name = "shipment_origin", columnDefinition = "geography not null")
    private Object shipmentOrigin;
*/
/*
 TODO [Reverse Engineering] create field to map the 'shipment_destination' column
 Available actions: Define target Java type | Uncomment as is | Remove column mapping
    @Column(name = "shipment_destination", columnDefinition = "geography not null")
    private Object shipmentDestination;
*/
}
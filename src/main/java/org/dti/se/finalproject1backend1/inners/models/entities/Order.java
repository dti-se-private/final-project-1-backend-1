package org.dti.se.finalproject1backend1.inners.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "\"order\"")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @NotNull
    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @NotNull
    @Column(name = "shipment_price", nullable = false)
    private BigDecimal shipmentPrice;

    @NotNull
    @Column(name = "item_price", nullable = false)
    private BigDecimal itemPrice;
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
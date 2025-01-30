package org.dti.se.finalproject1backend1.inners.models.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "order_item")
public class OrderItem {
    @Id
    private UUID id;

    private UUID orderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private UUID productId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private Double quantity;

}
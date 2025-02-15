package org.dti.se.finalproject1backend1.inners.models.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.UUID;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "warehouse_ledger")
public class WarehouseLedger {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origin_warehouse_id", nullable = false)
    private Warehouse originWarehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_warehouse_id", nullable = false)
    private Warehouse destinationWarehouse;

    @OneToOne(mappedBy = "warehouseLedger")
    private OrderItem orderItem;

    private Double originPreQuantity;

    private Double originPostQuantity;

    private Double destinationPreQuantity;

    private Double destinationPostQuantity;

    private OffsetDateTime time;

    private String status;

    @Transient
    public WarehouseProduct getWarehouseProduct() {
        WarehouseProduct wp = new WarehouseProduct();
        wp.setProduct(this.product);
        wp.setWarehouse(this.originWarehouse);
        wp.setQuantity(this.preQuantity);
        return wp;
    }

}
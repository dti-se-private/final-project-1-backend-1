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
@Table(name = "warehouse_product")
public class WarehouseProduct {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private Double quantity;

    @OneToMany(mappedBy = "originWarehouseProduct")
    @Builder.Default
    private Set<WarehouseLedger> originWarehouseLedgers = new LinkedHashSet<>();

    @OneToMany(mappedBy = "destinationWarehouseProduct")
    @Builder.Default
    private Set<WarehouseLedger> destinationWarehouseLedgers = new LinkedHashSet<>();

}
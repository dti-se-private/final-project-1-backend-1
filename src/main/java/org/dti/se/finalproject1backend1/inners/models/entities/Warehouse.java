package org.dti.se.finalproject1backend1.inners.models.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "warehouse")
public class Warehouse {
    @Id
    private UUID id;

    private String name;

    private String description;

    @OneToMany(mappedBy = "originWarehouse")
    private Set<WarehouseLedger> warehouseLedgersOrigin = new LinkedHashSet<>();

    @OneToMany(mappedBy = "destinationWarehouse")
    private Set<WarehouseLedger> warehouseLedgersDestination = new LinkedHashSet<>();

    @OneToMany(mappedBy = "warehouse")
    private Set<WarehouseProduct> warehouseProducts = new LinkedHashSet<>();

/*
 TODO [Reverse Engineering] create field to map the 'location' column
 Available actions: Define target Java type | Uncomment as is | Remove column mapping
    @Column(name = "location", columnDefinition = "geography")
    private Object location;
*/
}
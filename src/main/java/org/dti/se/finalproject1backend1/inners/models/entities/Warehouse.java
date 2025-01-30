package org.dti.se.finalproject1backend1.inners.models.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "warehouse")
public class Warehouse {
    @Id
    private UUID id;

    private String name;

    private String description;

    private PGGeographyJdbcType location;

    @OneToMany(mappedBy = "originWarehouse")
    @Builder.Default
    private Set<WarehouseLedger> warehouseLedgersOrigin = new LinkedHashSet<>();

    @OneToMany(mappedBy = "destinationWarehouse")
    @Builder.Default
    private Set<WarehouseLedger> warehouseLedgersDestination = new LinkedHashSet<>();

    @OneToMany(mappedBy = "warehouse")
    @Builder.Default
    private Set<WarehouseProduct> warehouseProducts = new LinkedHashSet<>();

}
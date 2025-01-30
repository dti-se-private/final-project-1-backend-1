package org.dti.se.finalproject1backend1.inners.models.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "category")
public class Category {
    @Id
    private UUID id;

    private String name;

    private String description;

    @OneToMany(mappedBy = "category")
    private Set<Product> products = new LinkedHashSet<>();

}
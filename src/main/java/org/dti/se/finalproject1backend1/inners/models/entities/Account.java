package org.dti.se.finalproject1backend1.inners.models.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.finalproject1backend1.inners.models.Model;
import org.hibernate.annotations.ColumnDefault;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "account")
@Entity
public class Account extends Model {
    @Id
    private UUID id;

    private String name;

    private String email;

    private String password;

    private String phone;

    @Builder.Default
    private Boolean isVerified = false;

    private byte[] image;

    @OneToMany(mappedBy = "account")
    @Builder.Default
    private Set<AccountAddress> accountAddresses = new LinkedHashSet<>();

    @OneToMany(mappedBy = "account")
    @Builder.Default
    private Set<AccountPermission> accountPermissions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "account")
    @Builder.Default
    private Set<CartItem> cartItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "account")
    @Builder.Default
    private Set<Order> orders = new LinkedHashSet<>();

}

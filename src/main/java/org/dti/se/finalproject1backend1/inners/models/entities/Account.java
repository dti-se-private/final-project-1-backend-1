package org.dti.se.finalproject1backend1.inners.models.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.finalproject1backend1.inners.models.Model;
import org.hibernate.annotations.ColumnDefault;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;


@Data
@EqualsAndHashCode(callSuper = true)
@Builder
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

    @ColumnDefault("false")
    private Boolean isVerified;

    private byte[] image;

    @OneToMany(mappedBy = "account")
    private Set<AccountAddress> accountAddresses = new LinkedHashSet<>();

    @OneToMany(mappedBy = "account")
    private Set<AccountPermission> accountPermissions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "account")
    private Set<CartItem> cartItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "account")
    private Set<Order> orders = new LinkedHashSet<>();

    @OneToMany(mappedBy = "account")
    private Set<Session> sessions = new LinkedHashSet<>();

}

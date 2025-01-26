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
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = Integer.MAX_VALUE)
    private String name;

    @Column(name = "email", nullable = false, length = Integer.MAX_VALUE)
    private String email;

    @Column(name = "password", nullable = false, length = Integer.MAX_VALUE)
    private String password;

    @Column(name = "phone", length = Integer.MAX_VALUE)
    private String phone;

    @ColumnDefault("false")
    @Column(name = "is_verified")
    private Boolean isVerified;

    @Column(name = "image")
    private byte[] image;

    @Column(name = "provider", length = Integer.MAX_VALUE)
    private String provider;

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

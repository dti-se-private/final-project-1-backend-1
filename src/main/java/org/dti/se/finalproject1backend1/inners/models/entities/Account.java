package org.dti.se.finalproject1backend1.inners.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.finalproject1backend1.inners.models.Model;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

import java.util.UUID;


@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "account")
@Entity
public class Account extends Model implements Persistable<UUID> {
    @Id
    private UUID id;
    private String name;
    private String email;
    private String password;
    private String phone;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)

    @Transient
    @Builder.Default
    @JsonIgnore
    public Boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }
}

package pl.app.account.model.view;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Getter
@Immutable
@NoArgsConstructor
//@AllArgsConstructor
@EqualsAndHashCode
public class AccountView {
    @Id
    private int id;
    @Column(unique = true)
    private String pesel;
    private String name;
    private String surname;
    private int subAccountCount;
}

package pl.app.account.model.view;

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
@EqualsAndHashCode
public class AccountView {
    @Id
    private int id;
    private String pesel;
    private String name;
    private String surname;
    private String subAccounts;
//    @Enumerated(EnumType.STRING)
//    private CurrencyCode currency;
//    private BigDecimal amount;

//    public AccountView(String pesel, String name, String surname, CurrencyCode currencyCode, BigDecimal amount) {
//        this.pesel = pesel;
//        this.name = name;
//        this.surname = surname;
//        this.currency = currencyCode;
//        this.amount = amount;
//    }
}

package pl.app.account.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pl.app.account.model.enums.CurrencyCode;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "pesel")
@ToString
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(unique = true)
    private String pesel;
    private String name;
    private String surname;

    @Version
    private long version;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private Set<SubAccount> subAccounts = new HashSet<>();

    public Account(String pesel, String name, String surname, BigDecimal balanceUSD) {
        this.pesel = pesel;
        this.name = name;
        this.surname = surname;

        new SubAccount(this, CurrencyCode.USD, new BigDecimal(String.valueOf(balanceUSD)));
        new SubAccount(this, CurrencyCode.BTC, BigDecimal.ZERO);
        new SubAccount(this, CurrencyCode.ETH, BigDecimal.ZERO);
    }

}

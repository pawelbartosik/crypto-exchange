package pl.app.account.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pl.app.account.exception.SubAccountNotFoundException;
import pl.app.currency.model.Currency;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = {"account", "currency"})
@ToString(exclude = "account")
@NoArgsConstructor
public class SubAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pesel", referencedColumnName = "pesel", columnDefinition = "VARCHAR(11)")
    private Account account;
    @ManyToOne
    @JoinColumn(name = "currency", referencedColumnName = "code")
    private Currency currency;
    private BigDecimal amount;

    @Version
    private long version;

    public SubAccount(Account account, Currency currency, BigDecimal amount) {
        this.account = account;
        this.currency = currency;
        this.amount = amount;

        this.account.getSubAccounts().add(this);
    }

    public static SubAccount getSubAccount(Account account, String currency) {
        return account.getSubAccounts()
                .stream()
                .filter(subAccount -> subAccount.getCurrency().getCode().equals(currency))
                .findFirst()
                .orElseThrow(SubAccountNotFoundException::new);
    }

}

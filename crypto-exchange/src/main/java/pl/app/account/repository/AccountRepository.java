package pl.app.account.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import pl.app.account.model.Account;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Integer> {

    void deleteAccountByPesel(String pesel);

    @Query("select a from Account a left join fetch a.subAccounts where a.pesel = :pesel")
    Optional<Account> findByPeselWithSubAccounts(String pesel);

    @Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
    @Query("select a from Account a left join fetch a.subAccounts where a.pesel = :pesel")
    Optional<Account> findByPeselWithSubAccountsForExchange(String pesel);

    @Query("select a from Account a left join fetch a.subAccounts")
    Page<Account> findAllWithSubAccounts(Pageable pageable);

}

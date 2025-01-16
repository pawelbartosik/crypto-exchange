package pl.app.account.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import pl.app.account.model.SubAccount;
import pl.app.account.model.enums.CurrencyCode;

import java.util.Optional;

public interface SubAccountRepository extends JpaRepository<SubAccount, Integer> {
    @Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
    @Query("SELECT s FROM SubAccount s WHERE s.account.pesel = :pesel AND s.currency = :currency")
    Optional<SubAccount> findByPeselAndCurrencyWithLock(String pesel, CurrencyCode currency);

    @Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
    @Query("SELECT s FROM SubAccount s WHERE s.id = :id")
    void findByIdWithLock(int id);
}

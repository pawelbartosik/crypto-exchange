package pl.app.account.repository;

import pl.app.account.model.view.AccountView;

import java.util.Optional;

public interface AccountViewRepository extends ReadOnlyRepository<AccountView, Integer> {
    Optional<AccountView> findByPesel(String pesel);
}

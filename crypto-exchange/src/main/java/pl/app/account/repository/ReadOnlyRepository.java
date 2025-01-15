package pl.app.account.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@NoRepositoryBean
@Transactional(readOnly = true)
public interface ReadOnlyRepository<T, ID> extends PagingAndSortingRepository<T, ID> {
    Optional<T> findById(ID id);
}

package ru.sergdm.ws.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import ru.sergdm.ws.model.Account;
import ru.sergdm.ws.model.MoneyMove;

import java.util.List;

public interface AccountRepository extends CrudRepository<Account, Long>,
		JpaSpecificationExecutor<Account> {
	List<Account> findByUserId(Long userId);
}

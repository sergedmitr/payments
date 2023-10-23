package ru.sergdm.ws.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import ru.sergdm.ws.model.MoneyMove;

import java.util.List;

public interface MoneyMoveRepository extends CrudRepository<MoneyMove, Long>,
		JpaSpecificationExecutor<MoneyMove> {
	List<MoneyMove> findByUserId(Long userId);
	void deleteByUserId(Long userId);
}

package ru.sergdm.ws.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.sergdm.ws.exception.ResourceNotExpectedException;
import ru.sergdm.ws.exception.ResourceNotFoundException;
import ru.sergdm.ws.exception.WrongUserException;
import ru.sergdm.ws.model.MoneyMove;
import ru.sergdm.ws.repository.MoneyMoveRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MoneyService {
	@Autowired
	private MoneyMoveRepository moneyMoveRepository;

	public List<MoneyMove> findMoves(Long accountId) {
		List<MoneyMove> moves = moneyMoveRepository.findByAccountId(accountId);
		System.out.println("moves = " + moves);
		return moves;
	}

	public MoneyMove addMove(MoneyMove move) {
		return moneyMoveRepository.save(move);
	}

	public BigDecimal getRest(Long accountId) {
		List<MoneyMove> moves = moneyMoveRepository.findByAccountId(accountId);
		BigDecimal rest = moves.stream().
				map(m -> m.getAmount().multiply(BigDecimal.valueOf(m.getDirection()))). reduce(BigDecimal.ZERO,
				(subtotal, e) -> subtotal.add(e));
		return rest;
	}

	public void returnMove(Long userId, Long returnMoveId, Long orderId) throws ResourceNotFoundException, WrongUserException,
			ResourceNotExpectedException {
		MoneyMove moveToReturn = moneyMoveRepository.findById(returnMoveId)
				.orElseThrow(() -> new ResourceNotFoundException("Not found move with id = " + returnMoveId));
		if (!moveToReturn.getUserId().equals(userId)) {
			throw new WrongUserException("Returned pay belongs to other user");
		}
		if (!moveToReturn.getOperation().equals("pay")) {
			throw new ResourceNotExpectedException("Only pays can be returned");
		}
		MoneyMove returnedMove = new MoneyMove();
		returnedMove.setMoveDt(new Date());
		returnedMove.setOperation("ReturnPay");
		returnedMove.setDirection(1);
		returnedMove.setAmount(moveToReturn.getAmount());
		returnedMove.setUserId(moveToReturn.getUserId());
		returnedMove.setOrderId(orderId);

		moneyMoveRepository.save(returnedMove);
	}

	public void deleteAll() {
		moneyMoveRepository.deleteAll();
	}
}

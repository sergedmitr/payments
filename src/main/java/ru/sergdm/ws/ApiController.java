package ru.sergdm.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.sergdm.ws.exception.ResourceNotExpectedException;
import ru.sergdm.ws.exception.ResourceNotFoundException;
import ru.sergdm.ws.exception.WrongUserException;
import ru.sergdm.ws.model.MoneyMove;
import ru.sergdm.ws.model.ReturnRequest;
import ru.sergdm.ws.model.SystemName;
import ru.sergdm.ws.service.MoneyService;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
public class ApiController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private MoneyService moneyService;

	@GetMapping("/")
	public ResponseEntity<Object> name() {
		SystemName name = new SystemName();
		return new ResponseEntity<>(name, HttpStatus.OK);
	}

	@GetMapping("/rests/{userId}")
	public ResponseEntity<BigDecimal> rests(@PathVariable Long userId){
		logger.info("rests. userId = {}", userId);
		return new ResponseEntity(moneyService.getRest(userId), HttpStatus.OK);
	}

	@GetMapping("/moves/{userId}")
	public ResponseEntity<List<MoneyMove>> restsList(@PathVariable Long userId){
		logger.info("Moves. userId = {}", userId);
		List<MoneyMove> moves = moneyService.findMoves(userId);
		return new ResponseEntity(moves, HttpStatus.OK);
	}

	@PostMapping(value = "/replenish/{userId}")
	public ResponseEntity<?> replenish(@PathVariable Long userId, @Valid @RequestBody MoneyMove move){
		logger.info("Replenish. userId = {}", userId);
		move.setUserId(userId);
		move.setDirection(1);
		move.setOperation("replenish");
		moneyService.addMove(move);
		return ResponseEntity.ok().body(HttpStatus.OK);
	}

	@PostMapping(value = "/pay/{userId}")
	public ResponseEntity<?> pay(@PathVariable Long userId, @Valid @RequestBody MoneyMove move){
		// Возможно, денежные операции нужно совершать с текущим системным временем
		logger.info("Pay. userId = {}", userId);
		BigDecimal moneyRest = moneyService.getRest(userId);
		if (moneyRest.compareTo(move.getAmount()) >= 0) {
			move.setUserId(userId);
			move.setDirection(-1);
			move.setOperation("pay");
			MoneyMove newMoneyMove = moneyService.addMove(move);
			return ResponseEntity.ok().body(newMoneyMove);
		} else {
			return ResponseEntity.status(409).body("Not enough money");
		}
	}

	@PostMapping(value = "/pay-return/{userId}")
	public ResponseEntity<?> payReturn(@PathVariable Long userId, @Valid @RequestBody ReturnRequest request){
		logger.info("Pay-return. userId = {}", userId);
		try {
			moneyService.returnMove(userId, request.getReturnMoveId(), request.getOrderId());
			return ResponseEntity.ok().body(HttpStatus.OK);
		} catch (WrongUserException | ResourceNotExpectedException ex) {
			logger.error(ex.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
		} catch (ResourceNotFoundException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(request);
		}
	}
}

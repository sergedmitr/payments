package ru.sergdm.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.sergdm.ws.exception.ResourceNotExpectedException;
import ru.sergdm.ws.exception.ResourceNotFoundException;
import ru.sergdm.ws.exception.WrongUserException;
import ru.sergdm.ws.model.Notification;
import ru.sergdm.ws.kafka.NotificationSender;
import ru.sergdm.ws.model.Account;
import ru.sergdm.ws.model.MoneyMove;
import ru.sergdm.ws.model.ReturnRequest;
import ru.sergdm.ws.model.SystemName;
import ru.sergdm.ws.service.AccountService;
import ru.sergdm.ws.service.MoneyService;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
public class ApiController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private MoneyService moneyService;

	@Autowired
	private AccountService accountService;

	@Autowired
	private NotificationSender sender;

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

	@GetMapping("/user/moves/{userId}")
	public ResponseEntity<List<MoneyMove>> restsList(@PathVariable Long userId){
		logger.info("Moves. userId = {}", userId);
		List<MoneyMove> moves = moneyService.findMoves(userId);
		return new ResponseEntity(moves, HttpStatus.OK);
	}

	@DeleteMapping("/moves")
	public ResponseEntity<?> deleteAll(){
		logger.info("Delete all.");
		moneyService.deleteAll();
		return ResponseEntity.ok().body(HttpStatus.OK);
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

	@PostMapping(value = "/enroll/{accountId}")
	public ResponseEntity<?> enroll(@PathVariable Long accountId, @Valid @RequestBody MoneyMove move){
		try {
			logger.info("Enroll. accountId = {}", accountId);
			Account account = accountService.getAccount(accountId);
			move.setAccountId(accountId);
			move.setDirection(1);
			move.setOperation("enroll");
			moneyService.addMove(move);
			return ResponseEntity.ok().body(HttpStatus.OK);
		} catch (ResourceNotFoundException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found. AccountId = " + accountId);
		}
	}

	@GetMapping("/moves/{accountId}")
	public ResponseEntity<List<MoneyMove>> movesByAccount(@PathVariable Long accountId){
		logger.info("Moves. accountId = {}", accountId);
		List<MoneyMove> moves = moneyService.findMoves(accountId);
		return new ResponseEntity(moves, HttpStatus.OK);
	}

	@PostMapping(value = "/pay/{accountId}")
	public ResponseEntity<?> pay(@PathVariable Long accountId, @Valid @RequestBody MoneyMove move){
		// Возможно, денежные операции нужно совершать с текущим системным временем
		logger.info("Pay. accountId = {}", accountId);
		BigDecimal moneyRest = moneyService.getRest(accountId);
		if (moneyRest.compareTo(move.getAmount()) >= 0) {
			move.setAccountId(accountId);
			move.setDirection(-1);
			move.setOperation("pay");
			MoneyMove newMoneyMove = moneyService.addMove(move);

			Notification note = new Notification();
			note.setOrderId(move.getOrderId());
			note.setUserId(move.getUserId());
			note.setMessage("Успешное списание средств: " + newMoneyMove.getAmount());
			sender.send(note);

			return ResponseEntity.ok().body(newMoneyMove);
		} else {
			Notification note = new Notification();
			note.setOrderId(move.getOrderId());
			note.setUserId(move.getUserId());
			note.setMessage("Недостаточно средств для списания: " + move.getAmount());
			sender.send(note);
			return ResponseEntity.status(409).body("Not enough money");
		}
	}

	@PostMapping(value = "/pay-return/{accountId}")
	public ResponseEntity<?> payReturn(@PathVariable Long accountId, @Valid @RequestBody ReturnRequest request){
		logger.info("Pay-return. accountId = {}", accountId);
		try {
			moneyService.returnMove(accountId, request.getReturnMoveId(), request.getOrderId());
			return ResponseEntity.ok().body(HttpStatus.OK);
		} catch (WrongUserException | ResourceNotExpectedException ex) {
			logger.error(ex.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
		} catch (ResourceNotFoundException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(request);
		}
	}

	@GetMapping("/accounts")
	public ResponseEntity<List<Account>> accountsList(){
		logger.info("Accounts");
		List<Account> accounts = accountService.findAllAccounts();
		return new ResponseEntity(accounts, HttpStatus.OK);
	}

	@GetMapping("/accounts/{userId}")
	public ResponseEntity<List<Account>> accountsByUser(@PathVariable Long userId){
		logger.info("Moves. userId = {}", userId);
		List<MoneyMove> moves = moneyService.findMoves(userId);
		return new ResponseEntity(moves, HttpStatus.OK);
	}

	@PostMapping("/accounts")
	public ResponseEntity<?> addAccount(@Valid @RequestBody Account account) {
		logger.info("add account. account = {}", account);
		Account accountNew = accountService.addAccount(account);
		return ResponseEntity.ok().body(accountNew);
	}

	@DeleteMapping("/accounts")
	public ResponseEntity<?> deleteAllAccounts(){
		logger.info("Delete all accounts.");
		accountService.deleteAll();
		return ResponseEntity.ok().body(HttpStatus.OK);
	}

}

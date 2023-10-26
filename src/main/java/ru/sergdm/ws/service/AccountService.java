package ru.sergdm.ws.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.sergdm.ws.exception.ResourceNotFoundException;
import ru.sergdm.ws.model.Account;
import ru.sergdm.ws.repository.AccountRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class AccountService {
	@Autowired
	private AccountRepository accountRepository;

	public List<Account> findAllAccounts() {
		List<Account> accounts = new ArrayList<>();
		accountRepository.findAll().forEach(accounts::add);
		System.out.println("accounts = " + accounts);
		return accounts;
	}

	public List<Account> findAccounts(Long userId) {
		List<Account> accounts = accountRepository.findByUserId(userId);
		System.out.println("accounts = " + accounts);
		return accounts;
	}

	public Account addAccount(Account account) {
		return accountRepository.save(account);
	}

	public Account getAccount(Long accountId) throws ResourceNotFoundException {
		Account account = accountRepository.findById(accountId).orElseThrow(() -> new ResourceNotFoundException("Account not found, id = " + accountId));
		return account;
	}

	public void deleteAll() {
		accountRepository.deleteAll();
	}
}

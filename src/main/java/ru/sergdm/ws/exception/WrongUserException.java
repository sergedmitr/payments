package ru.sergdm.ws.exception;

public class WrongUserException extends Exception {
	public WrongUserException() {
	}

	public WrongUserException(String msg) {
		super(msg);
	}
}

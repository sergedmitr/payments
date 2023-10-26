package ru.sergdm.ws.model;

public class Notification {
	Long orderId;
	Long userId;
	String message;

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "Notification{" +
				" orderId=" + orderId +
				", userId='" + userId + '\'' +
				", message='" + message + '\'' +
				'}';
	}
}

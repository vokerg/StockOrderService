package com.stock.order.model;

public class OperationType {
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getSign() {
		return sign;
	}
	public void setSign(int sign) {
		this.sign = sign;
	}
	public boolean isfTransfer() {
		return fTransfer;
	}
	public void setfTransfer(boolean fTransfer) {
		this.fTransfer = fTransfer;
	}
	private int id;
	private String name;
	private int sign;
	private boolean fTransfer;
}

package com.stock.order.model;

import java.util.Date;
import java.util.List;

public class OrderDoc {
	private int id;
	private Date date;
	private Integer stockId;
	private Integer stockId2;
	private int operationTypeId;
	private String stockName;
	private String stock2Name;
	private Integer statusId;
	private String operationTypeName;
	private String stocksName;
	
	private List<Order> orders;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Integer getStockId() {
		return stockId;
	}
	public void setStockId(Integer stockId) {
		this.stockId = stockId;
	}
	public Integer getStockId2() {
		return stockId2;
	}
	public void setStockId2(Integer stockId2) {
		this.stockId2 = stockId2;
	}
	public int getOperationTypeId() {
		return operationTypeId;
	}
	public void setOperationTypeId(int operationTypeId) {
		this.operationTypeId = operationTypeId;
	}
	public String getStockName() {
		return stockName;
	}
	public void setStockName(String stockName) {
		this.stockName = stockName;
	}
	public String getStock2Name() {
		return stock2Name;
	}
	public void setStock2Name(String stock2Name) {
		this.stock2Name = stock2Name;
	}
	public Integer getStatusId() {
		return statusId;
	}
	public void setStatusId(Integer statusId) {
		this.statusId = statusId;
	}
	public String getOperationTypeName() {
		return operationTypeName;
	}
	public void setOperationTypeName(String operationTypeName) {
		this.operationTypeName = operationTypeName;
	}
	public List<Order> getOrders() {
		return orders;
	}
	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}
	public String getStocksName() {
		return stocksName;
	}
	public void setStocksName(String stocksName) {
		this.stocksName = stocksName;
	}
}

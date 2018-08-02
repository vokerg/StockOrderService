package com.stock.order.dao;

import java.util.List;

import com.stock.order.model.OperationType;

public interface OperationTypeDao {
	public List<OperationType> getAll();
	public String getOperationTypeName(int id);
}

package com.stock.order.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stock.order.dao.OperationTypeDao;
import com.stock.order.model.OperationType;

@RestController
@RequestMapping(value = "/operationTypes")
public class OperationTypeController {
	@Autowired
	OperationTypeDao operationTypeDao;
	
	@GetMapping("")
	public List<OperationType> getOperationTypes() {
		return operationTypeDao.getAll();
	}
}

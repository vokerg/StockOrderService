package com.stock.order.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stock.order.dao.OrderDocDao;
import com.stock.order.model.OrderDoc;
import com.stock.order.model.SharedUser;

@Service
public class DocsService {
	
	@Autowired
	OrderDocDao orderDocDao;
	
	public List<OrderDoc> getOrders(SharedUser sharedUser) throws AccessForbidden {
		if (sharedUser == null) {
			throw new AccessForbidden();
		}
		return orderDocDao.getDocs(sharedUser.isAdmin() ? null : sharedUser.getViewstocks());
	}
}

package com.stock.order.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.stock.order.dao.OperationTypeDao;
import com.stock.order.dao.OrderDao;
import com.stock.order.dao.OrderDocDao;
import com.stock.order.dao.ProductDao;
import com.stock.order.model.Order;
import com.stock.order.model.OrderDoc;
import com.stock.order.model.SharedUser;

@Service
public class OrdersService {
	
	@Autowired
	OrderDao orderDao;
	
	@Autowired
	OrderDocDao orderDocDao;
	
	@Autowired
	OperationTypeDao operationTypeDao;
	
	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	ProductDao productDao;

	public List<Order> getOrders(SharedUser sharedUser, String productId, String stockId, String documentId) throws AccessForbidden {
		if (sharedUser == null) {
			throw new AccessForbidden();
		}
		if (!sharedUser.isAdmin()) {
			if ((stockId != null) && (!sharedUser.getViewstocks().contains(stockId))) {
				throw new AccessForbidden();
			}
			
			if (documentId != null) {
				OrderDoc doc = orderDocDao.getDocumentById(documentId);
				if (!sharedUser.getViewstocks().contains(String.valueOf(doc.getStockId())) && !sharedUser.getViewstocks().contains(String.valueOf(doc.getStockId2()))) {
					throw new AccessForbidden();
				}
			}
		}

		List<Order> orders = orderDao.getFilteredOrders(productId, stockId, documentId, sharedUser.isAdmin() ? null : sharedUser.getViewstocks());
		return orders;
	}

	public String validateDoc(OrderDoc doc) {
		int sign = operationTypeDao.getAll().stream().filter(op -> op.getId() == doc.getOperationTypeId()).findFirst().get().getSign();
		if (sign >= 0) {
			return "";
		}
		Integer stockId = doc.getStockId();
		List<Order> failedOrders = doc.getOrders().stream()
			.filter(order-> (order.getQty() > 0))
			.filter(order -> !isEnoughStockRestsForOperation(stockId, order.getProductId(), order.getQty()))
			.collect(Collectors.toList());
		return failedOrders.size() > 0 ? prepareFailedOrdersResult(failedOrders) : "";
	}

	private String prepareFailedOrdersResult(List<Order> failedOrders) {
		return failedOrders.stream()
				.map(order -> productDao.getProductName(String.valueOf(order.getProductId())))
				.collect(Collectors.joining(","));
	}

	private boolean isEnoughStockRestsForOperation(Integer stockId, Integer productId, Float requestedUnits) {
		Float stockRestQty = restTemplate.getForObject("http://STOCK-API/stocks/" + String.valueOf(stockId) + "/stockrest/" + String.valueOf(productId), Float.class);
		return requestedUnits <= stockRestQty;
	}

}

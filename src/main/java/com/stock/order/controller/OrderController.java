package com.stock.order.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.stock.order.dao.OrderDao;
import com.stock.order.dao.OrderDocDao;
import com.stock.order.model.Order;
import com.stock.order.model.OrderDoc;
import com.stock.order.model.SharedUser;
import com.stock.order.service.AccessForbidden;
import com.stock.order.service.OrdersService;
import com.stock.order.service.UserService;

@RestController
@RequestMapping(value = "/orders")
public class OrderController {

	@Autowired
	OrderDao orderDao;

	@Autowired
	AmqpTemplate template;

	@Autowired
	UserService userService;
	
	@Autowired
	OrderDocDao docDao;
	
	@Autowired
	OrdersService ordersService;

	@GetMapping("/{id}")
	public ResponseEntity<Order> getOrder(@RequestHeader(value = "idUser", required = false) String idUser,
			@PathVariable String id) throws JsonParseException, JsonMappingException, IOException {
		SharedUser sharedUser = (idUser != null) ? userService.getSharedUser(idUser) : null;
		if ((idUser != null) && (sharedUser == null)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		Order order = orderDao.getOrderById(id);
		if (order != null && !userService.isAllowedToSeeStock(sharedUser, order.getStockId())
				&& !userService.isAllowedToSeeStock(sharedUser, order.getStockId2())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		return ResponseEntity.ok(order);
	}

	@GetMapping("")
	public ResponseEntity<List<Order>> getOrders(
			@RequestHeader(value = "idUser", required = false) String idUser,
			@RequestParam(value = "productId", required = false) String productId,
			@RequestParam(value = "stockId", required = false) String stockId,
			@RequestParam(value = "paramUserId", required = false) String paramUserId,
			@RequestParam(value = "documentId", required = false) String documentId
	) throws JsonParseException, JsonMappingException, IOException {
		SharedUser sharedUser = userService.getSharedUser((idUser != null) ? idUser : (paramUserId != null) ? paramUserId : null);
		if (!validateGetOrdersRequest(sharedUser, paramUserId, idUser, documentId)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		try {
			List<Order> orders = ordersService.getOrders(sharedUser, productId, stockId, documentId);
			return ResponseEntity.ok(orders);
		} catch (AccessForbidden e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
	}

	private boolean validateGetOrdersRequest(SharedUser sharedUser, String paramUserId, String idUser,
			String documentId) {
		if ((idUser != null || paramUserId != null) && (sharedUser == null)) {
			return false;
		}
		
		OrderDoc doc = null;
		if (documentId != null) {
			doc = docDao.getDocumentById(documentId);
			if (doc == null) {
				return false;
			}
		}
		return true;
	}

	@PutMapping("")
	public void addOrder(@RequestBody Order order) {
		int orderId = orderDao.addOrder(order);
		template.convertAndSend("orderAddedQueue", orderId);
	}
}

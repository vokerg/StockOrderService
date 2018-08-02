package com.stock.order.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.stock.order.dao.OrderDocDao;
import com.stock.order.model.OrderDoc;
import com.stock.order.model.SharedUser;
import com.stock.order.service.AccessForbidden;
import com.stock.order.service.DocsService;
import com.stock.order.service.OrdersService;
import com.stock.order.service.UserService;

@RestController
@RequestMapping(value = "/docs")
public class OrderDocController {

	@Autowired
	OrderDocDao docDao;
	
	@Autowired
	DocsService docsService;

	@Autowired
	AmqpTemplate template;

	@Autowired
	UserService userService;
	
	@Autowired
	OrdersService ordersService;

	@PutMapping("")
	public ResponseEntity<Integer> addDoc(@RequestHeader(value = "idUser", required = false) String idUser,
			@RequestBody OrderDoc doc) throws JsonParseException, JsonMappingException, IOException {
		SharedUser sharedUser = (idUser != null) ? userService.getSharedUser(idUser) : null;
		if ((idUser != null) && (sharedUser == null)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		if (!userService.isAllowedToSeeStock(sharedUser, doc.getStockId())
				&& !userService.isAllowedToSeeStock(sharedUser, doc.getStockId2())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		int newDocId = docDao.addDoc(doc);
		template.convertAndSend("docAddedQueue", newDocId);
		return ResponseEntity.ok(newDocId);
	}
	
	@PutMapping("/validate")
	public ResponseEntity<String> validateDoc(@RequestHeader(value = "idUser", required = false) String idUser,
			@RequestBody OrderDoc doc) throws JsonParseException, JsonMappingException, IOException {
		String response = ordersService.validateDoc(doc);
		return ResponseEntity.ok(response);
	}
	

	@GetMapping("")
	public ResponseEntity<List<OrderDoc>> getAllDocs(
			@RequestHeader(value = "idUser", required = false) String idUser,
			@RequestParam(value = "paramUserId", required = false) String paramUserId
	) throws JsonParseException, JsonMappingException, IOException {
		SharedUser sharedUser = userService.getSharedUser((idUser != null) ? idUser : (paramUserId != null) ? paramUserId : null);
		if ((idUser != null || paramUserId != null) && (sharedUser == null)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		try {
			return ResponseEntity.ok(docsService.getOrders(sharedUser));
		} catch (AccessForbidden e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
	}
}

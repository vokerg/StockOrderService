package com.stock.order.dao;

import java.util.List;

import com.stock.order.model.OrderDoc;

public interface OrderDocDao {
	public int addDoc(OrderDoc doc);
	public List<OrderDoc> getDocs();
	public OrderDoc getDocumentById(String documentId);
	public List<OrderDoc> getDocs(List<String> viewstocks);
}

package com.stock.order.dao;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.stock.order.model.OrderDoc;
import com.stock.order.service.CommonUtils;

@Component
public class OrderDocDaoImpl implements OrderDocDao{

	private static final int DOC_NEW = 0;

	private static final String SELECT_STOCK_ORDER_DOCS = 
			"SELECT d.*, s1.name as stock1_name, s2.name as stock2_name from stock_order_doc d\n" +
					"left join stock s1 on s1.id = d.stock_id1\n" + 
					"left join stock s2 on s2.id = d.stock_id2\n" +
					"where 1=1";

	@Autowired
	OrderDao orderDao;
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Autowired
	OperationTypeDao operationTypeDao;
	
	@Override
	@Transactional
	public int addDoc(OrderDoc doc) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement statement = connection.prepareStatement("insert into stock_order_doc (date, stock_id1, stock_id2, operation_type_id, status_id) values (?, ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			statement.setDate(1, (Date) doc.getDate());
			statement.setInt(2, doc.getStockId());
			if (doc.getStockId2() != null) {
				statement.setInt(3, doc.getStockId2());
			} else {
				statement.setNull(3, java.sql.Types.INTEGER);
			}
			statement.setInt(4, doc.getOperationTypeId());
			statement.setInt(5, DOC_NEW);
			return statement;
		}, keyHolder);
		int docId = (int) keyHolder.getKeys().get("id");
		
		doc.getOrders().stream()
			.peek(order -> order.setDocumentId(docId))
			.forEach(order -> orderDao.addOrder(order));
		return docId;
	}
	
	public List<OrderDoc> getDocs(String subquery) {
		return jdbcTemplate.query(SELECT_STOCK_ORDER_DOCS + subquery, (ResultSet rs, int rowNum) -> {
			OrderDoc doc = new OrderDoc();
			doc.setDate(rs.getDate("date"));
			doc.setId(rs.getInt("id"));
			doc.setOperationTypeId(rs.getInt("operation_type_id"));
			String operationTypeName = operationTypeDao.getOperationTypeName(doc.getOperationTypeId());
			doc.setOperationTypeName(operationTypeName != null ? operationTypeName : "");
			doc.setStockId(rs.getInt("stock_id1"));
			Integer idStock2 = rs.getInt("stock_id2");
			doc.setStockId2(rs.wasNull() ? null : idStock2);
			doc.setStockName(rs.getString("stock1_name"));
			doc.setStock2Name(rs.getString("stock2_name"));
			doc.setOrders(orderDao.getOrdersByDoc(doc.getId()));
			doc.setStocksName(CommonUtils.getCombinedStocksDescription(doc.getStockName(), doc.getStock2Name()));
			return doc;
		});
	}

	@Override
	public List<OrderDoc> getDocs() {
		return getDocs("");
	}

	@Override
	public OrderDoc getDocumentById(String documentId) {
		List<OrderDoc> docs = getDocs(" and d.id=" + documentId);
		return (docs.size() > 0) ? docs.get(0) : null;
	}

	@Override
	public List<OrderDoc> getDocs(List<String> viewstocks) {
		if (viewstocks == null) {
			return getDocs();
		}
		String viewStocks = (viewstocks.size() != 0) ? viewstocks.stream().collect(Collectors.joining(",")) : "-1";
		return getDocs(" and (stock_id1 in (" + viewStocks + ") or stock_id2 in(" + viewStocks + "))");
	}
}

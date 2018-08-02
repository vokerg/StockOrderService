package com.stock.order.dao;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import com.stock.order.model.Order;
import com.stock.order.service.CommonUtils;

@Component
public class OrderDaoImpl implements OrderDao {

	private static final String SELECT_ALL_ORDERS = "select \n" + 
			"	so.id, so.date, so.stock_id1, so.stock_id2, so.product_id, so.qty, so.operation_type_id, so.status_id,\n" + 
			"	p.name as product_name, s1.name as stock1_name, s2.name as stock2_name, so.document_id\n" + 
			"from stock_order so\n" + 
			"left join product p on p.id = so.product_id\n" + 
			"left join stock s1 on s1.id = so.stock_id1\n" + 
			"left join stock s2 on s2.id = so.stock_id2\n" +
			"where 1=1";
			
	private static final String INSERT_STOCK_ORDER = "insert into stock_order (date, stock_id1, stock_id2, qty, operation_type_id, product_id, status_id, document_id) values (?, ?, ?, ?, ?, ?, ?, ?)";
	private static final int STATUS_NEW = 0;
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Autowired
	OperationTypeDao operationTypeDao;

	@Override
	public int addOrder(Order order) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement statement =  connection.prepareStatement(INSERT_STOCK_ORDER, 
					Statement.RETURN_GENERATED_KEYS);
			statement.setDate(1, (Date) order.getDate());
			statement.setInt(2, order.getStockId());
			if (order.getStockId2() != null) {
				statement.setInt(3, order.getStockId2());	
			} else {
				statement.setNull(3, java.sql.Types.INTEGER);
			}
			statement.setFloat(4, order.getQty());
			statement.setInt(5, order.getOperationTypeId());
			statement.setInt(6, order.getProductId());
			statement.setInt(7, STATUS_NEW);
			if (order.getDocumentId() != null) {
				statement.setInt(8, order.getDocumentId());	
			} else {
				statement.setNull(8, java.sql.Types.INTEGER);
			}
			
			return statement;
		}, keyHolder);
		return (int) keyHolder.getKeys().get("id");
	}
	
	private List<Order> getOrders(String sql) {
		return jdbcTemplate.query (sql, 
				(rs, rownNum) -> {
					Order order = new Order();
					order.setId(rs.getInt("id"));
					order.setDate(rs.getDate("date"));
					order.setStockId(rs.getInt("stock_id1"));
					Integer idStock2 = rs.getInt("stock_id2");
					order.setStockId2(rs.wasNull() ? null : idStock2);
					order.setQty(rs.getInt("qty"));
					order.setProductId(rs.getInt("product_id"));
					order.setOperationTypeId(rs.getInt("operation_type_id"));
					order.setStatusId(rs.getInt("status_id"));
					order.setOperationTypeName(operationTypeDao.getOperationTypeName(rs.getInt("operation_type_id")));
					order.setProductName(rs.getString("product_name"));
					order.setStockName(rs.getString("stock1_name"));
					order.setStock2Name(rs.getString("stock2_name"));
					order.setStocksName(CommonUtils.getCombinedStocksDescription(order.getStockName(), order.getStock2Name()));
					Integer documentId = rs.getInt("document_id");
					order.setDocumentId(rs.wasNull() ? null : documentId);
					return order;
				});
	}
	
	@Override
	public List<Order> getAllOrders() {
		return getOrders(SELECT_ALL_ORDERS);
	}
	
	@Override
	public List<Order> getOrdersByProductIdAndStockId(String productId, String stockId){
		return getOrders(SELECT_ALL_ORDERS + getSubQueryByProductId(productId) + getSubQueryByStockId(stockId));
	}

	@Override
	public List<Order> getOrdersByProductId(String productId) {
		return getOrders(SELECT_ALL_ORDERS + getSubQueryByProductId(productId));
	}

	@Override
	public List<Order> getOrdersByStock(String stockId){
		return getOrders(SELECT_ALL_ORDERS + getSubQueryByStockId(stockId));
	}


	@Override
	public Order getOrderById(String id) {
		List<Order> orders = getOrders(SELECT_ALL_ORDERS + " and so.id=" + id);
		return (orders.size() > 0) ? orders.get(0) : null;
	}

	@Override
	public List<Order> getOrdersByDoc(int docId) {
		return getOrders(SELECT_ALL_ORDERS + getSubQueryByDocumentId(String.valueOf(docId)));
	}
	
	@Override
	public List<Order> getFilteredOrders(String productId, String stockId, String documentId, List<String> viewstocks) {
		String sql = SELECT_ALL_ORDERS;
		if (productId != null) {
			sql += getSubQueryByProductId(productId);
		}
		if (stockId != null) {
			sql += getSubQueryByStockId(stockId);
		}
		if (documentId != null) {
			sql += getSubQueryByDocumentId(documentId);
		}
		if (viewstocks != null) {
			sql += getSubQueryByStockList(viewstocks);
		}
		return getOrders(sql);
	}

	private String getSubQueryByProductId(String productId) {
		return " and product_id=" + productId;
	}

	private String getSubQueryByStockList(List<String> viewstocks) {
		String viewStocks = (viewstocks.size() != 0) ? viewstocks.stream().collect(Collectors.joining(",")) : "-1";
		return " and (stock_id1 in (" + viewStocks + ") or stock_id2 in(" + viewStocks + "))";
	}
	
	private String getSubQueryByStockId(String stockId) {
		return " and (stock_id1=" + stockId + " or stock_id2=" + stockId + ")";
	}
	
	private String getSubQueryByDocumentId(String documentId) {
		return " and document_id=" + documentId;
	}
}

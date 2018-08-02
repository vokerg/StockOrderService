package com.stock.order.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class ProductDaoImpl implements ProductDao{
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Override
	public String getProductName(String productId) {
		return jdbcTemplate.query("select name from product where id=" + productId, (ResultSetExtractor<String>) rs -> {
			rs.next();
			return rs.getString("name");
		});
	}
}

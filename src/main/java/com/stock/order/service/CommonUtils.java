package com.stock.order.service;

public class CommonUtils {
	public static String getCombinedStocksDescription(String stockName, String stock2Name) {
		return (stock2Name != null && !stock2Name.isEmpty()) ? stockName + "->" + stock2Name : stockName;
	}
}

package com.sesnu.fireball.service;

import java.util.List;

import com.sesnu.fireball.model.Ticker;

public interface DaoService {
	

	public List<Ticker> getAllTickers();
	
	public Ticker getTicker(String tickerName);
	
	void save(Object acctHist);

	void update(int orderId, double avgFillPrice);

}
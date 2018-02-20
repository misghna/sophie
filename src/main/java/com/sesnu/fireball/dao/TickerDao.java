package com.sesnu.fireball.dao;

import java.util.List;

import com.sesnu.fireball.model.Ticker;


public interface TickerDao {

	List<Ticker> findAll();
	
	Ticker findByName(String name);

	public Ticker findLastOne();
	
	public void updateTicker(Ticker ticker);
	
	public void updateTickerVolPerc(String tickerName,double volPerc);
	
}

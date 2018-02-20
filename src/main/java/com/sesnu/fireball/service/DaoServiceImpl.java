package com.sesnu.fireball.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sesnu.fireball.dao.TickerDao;
import com.sesnu.fireball.model.Ticker;


@Service("daoService")
@Transactional
public class DaoServiceImpl implements DaoService{

	
	@Autowired TickerDao tickerDao;

	public List<Ticker> getAllTickers(){
		return tickerDao.findAll();
	}
	
	public Ticker getTicker(String tickerName){
		return tickerDao.findByName(tickerName);
	}
	
	@Override
	public void save(Object acctHist) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(int orderId, double avgFillPrice) {
		// TODO Auto-generated method stub
		
	}
	
}

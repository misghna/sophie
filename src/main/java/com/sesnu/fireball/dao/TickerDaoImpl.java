package com.sesnu.fireball.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sesnu.fireball.model.Ticker;



@Repository("tickerDao")
@Transactional
public class TickerDaoImpl extends AbstractDao<Integer, Ticker>implements TickerDao{

	public Ticker findById(int id) {
		return getByKey(id);
	}

	public Ticker findByName(String name) {
		Criteria crit = createEntityCriteria();
		crit.add(Restrictions.eq("ticker", name));
		return (Ticker) crit.uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public List<Ticker> findAll(){
		Criteria crit = createEntityCriteria();
		crit.addOrder(Order.asc("ticker"));
		return (List<Ticker>)crit.list();
	}
	

	public Ticker findLastOne(){
		Criteria crit = createEntityCriteria();
		crit.setMaxResults(1);
		crit.addOrder(Order.desc("updatedOn"));
		return (Ticker)crit.list().get(0);
	}
	
	public void updateTicker(Ticker ticker) {
		update(ticker);
	}
	
	public void updateTickerVolPerc(String tickerName,double volPerc) {
		Ticker ticker = findByName(tickerName);
		ticker.setPercent(volPerc);
		ticker.setUpdatedOn(System.currentTimeMillis());
		update(ticker);
	}
}

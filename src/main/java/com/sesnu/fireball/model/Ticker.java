package com.sesnu.fireball.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="ticker")
public class Ticker implements Serializable{
	
	@Id
	@SequenceGenerator(name="ticker_seq",sequenceName="ticker_seq")
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="ticker_seq")
	private Integer id;	

	@Column(name="ticker",unique=true, nullable=false)
	private String ticker = UserProfileType.USER.getUserProfileType();

	@Column(name="vol_percent")
	private double percent;
	
	@Column(name="updated_on")
	private long updatedOn;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public double getPercent() {
		return percent;
	}

	public void setPercent(double percent) {
		this.percent = percent;
	}

	public long getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(long updatedOn) {
		this.updatedOn = updatedOn;
	}
	
	
	
	
	


}

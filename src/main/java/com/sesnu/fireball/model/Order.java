package com.sesnu.fireball.model;

public class Order {
	
	private long id;
	private String ticker;
	private String action;
	private double inPrice;
	private double stopLoss;
	private double maxRisk;
	private double riskRewardRatio;
	private String reqBy;
	private String status;
	
	
	
	
	
	public Order(long id, String ticker, String action,double inPrice, double stopLoss, 
					double maxRisk, double riskRewardRatio,String reqBy, String status) {
		this.id = id;
		this.ticker = ticker;
		this.action=action;
		this.inPrice = inPrice;
		this.stopLoss = stopLoss;
		this.maxRisk = maxRisk;
		this.riskRewardRatio = riskRewardRatio;
		this.reqBy=reqBy;
		this.status=status;
	}
	
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getTicker() {
		return ticker;
	}
	public void setTicker(String ticker) {
		this.ticker = ticker;
	}
	public double getInPrice() {
		return inPrice;
	}
	public void setInPrice(double inPrice) {
		this.inPrice = inPrice;
	}
	public double getStopLoss() {
		return stopLoss;
	}
	public void setStopLoss(double stopLoss) {
		this.stopLoss = stopLoss;
	}
	public double getMaxRisk() {
		return maxRisk;
	}
	public void setMaxRisk(double maxRisk) {
		this.maxRisk = maxRisk;
	}
	public double getRiskRewardRatio() {
		return riskRewardRatio;
	}
	public void setRiskRewardRatio(double riskRewardRatio) {
		this.riskRewardRatio = riskRewardRatio;
	}


	public String getAction() {
		return action;
	}


	public void setAction(String action) {
		this.action = action;
	}


	public String getReqBy() {
		return reqBy;
	}


	public void setReqBy(String reqBy) {
		this.reqBy = reqBy;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}
	
	
	
	

}

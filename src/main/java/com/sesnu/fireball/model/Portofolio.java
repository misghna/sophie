package com.sesnu.fireball.model;

import com.ib.controller.Position;

public class Portofolio {
	
	private double avgPrice;
	private int shares;
	private double realizedPnl;
	private double unrealizedPnl;
	private double maxUnreal;
	
	public Portofolio(Position pos,double prevMax) {
		if(pos!=null){
			this.avgPrice = pos.averageCost();
			this.shares = (int) pos.position();
			this.realizedPnl= pos.realPnl();
			this.unrealizedPnl = pos.unrealPnl();
			if(this.shares!=0){
				this.maxUnreal=prevMax<unrealizedPnl?unrealizedPnl:prevMax;
			}else{
				this.maxUnreal=0;
			}
		}
	}

	public double getAvgPrice() {
		return avgPrice;
	}
	
	public void setAvgPrice(double avgPrice) {
		this.avgPrice = avgPrice;
	}
	
	public int getPos() {
		return shares;
	}

	public double getRealizedPnl() {
		return realizedPnl;
	}

	public void setRealizedPnl(double realizedPnl) {
		this.realizedPnl = realizedPnl;
	}

	public double getUnrealizedPnl() {
		return unrealizedPnl;
	}

	public void setUnrealizedPnl(double unrealizedPnl) {
		this.unrealizedPnl = unrealizedPnl;
	}

	public void setPos(int pos) {
		this.shares = pos;
	}

	public double getMaxUnreal() {
		return maxUnreal;
	}

	public void setMaxUnreal(double maxUnreal) {
		this.maxUnreal = maxUnreal;
	}
		

}

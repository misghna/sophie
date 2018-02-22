package com.sesnu.fireball.model;

import java.util.List;

public class IdenticalTrade {
	
	
	private double gapPerc;
	private long startTime;
	private List<FBBar> barList;
	private List<FBBar> prevDayBarList;
	private double changePerc;
	private long volume;
	private double rmse;
	
	
	public IdenticalTrade( double gapPerc, double changePerc, long volume) {

		this.gapPerc = gapPerc;
		this.changePerc = changePerc;
		this.volume = volume;
	}
	

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}


	public void setBarList(List<FBBar> barList) {
		this.barList = barList;
	}


	public void setPrevDayBarList(List<FBBar> prevDayBarList) {
		this.prevDayBarList = prevDayBarList;
	}


	public void setRmse(double rmse) {
		this.rmse = rmse;
	}


	public double getGapPerc() {
		return gapPerc;
	}
	public long getStartTime() {
		return startTime;
	}
	public List<FBBar> getBarList() {
		return barList;
	}
	public List<FBBar> getPrevDayBarList() {
		return prevDayBarList;
	}
	public double getChangePerc() {
		return changePerc;
	}
	public long getVolume() {
		return volume;
	}
	public double getRmse() {
		return rmse;
	}
	
	

	
	
	
	

}

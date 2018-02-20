package com.sesnu.fireball.model;

import java.util.List;

public class SimilarTrade {
	
	
	private String ticker;
	private double gapPerc;
	private long startTime;
	private String type;
	private List<FBBar> barList;
	private List<FBBar> prevDayBarList;
	private double maxDayPerc;
	private double minDayPerc;
	private double lMin;
	private double lMax;
	private double preEma;
	private double rmse;
	
	
	public SimilarTrade(String ticker, double gapPerc, long startTime, String type, List<FBBar> barList,List<FBBar> prevDayBarList,
			double preEma,double maxPerc,double minPerc,double lMin,double lMax,double rmse) {
		this.ticker = ticker;
		this.gapPerc = gapPerc;
		this.startTime = startTime;
		this.type = type;
		this.barList = barList;
		this.prevDayBarList=prevDayBarList;
		this.preEma = preEma;
		this.minDayPerc=minPerc;
		this.maxDayPerc=maxPerc;
		this.lMin=lMin;
		this.lMax=lMax;
		this.rmse=rmse;
	}
	
	
	public String getTicker() {
		return ticker;
	}
	public double getGapPerc() {
		return gapPerc;
	}
	public long getStartTime() {
		return startTime;
	}
	public String getType() {
		return type;
	}
	public List<FBBar> getBarList() {
		return barList;
	}
	public double getPreEma() {
		return preEma;
	}


	public double getlMin() {
		return lMin;
	}


	public double getlMax() {
		return lMax;
	}


	public double getMaxDayPerc() {
		return maxDayPerc;
	}


	public double getMinDayPerc() {
		return minDayPerc;
	}


	public List<FBBar> getPrevDayBarList() {
		return prevDayBarList;
	}


	public double getRmse() {
		return rmse;
	}
	
	
	
	
	
	

}

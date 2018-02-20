package com.sesnu.fireball.model;

public class ScTable {

	
	private String ticker;
	private int rank;
	private volatile double gap;
	private volatile double gapPerc;
	private volatile double floatShares;
	private volatile double avgVol;
	private volatile double volume;
	private volatile double maxDayChange;
	private volatile double lastPrice;
	private volatile double marketCap;
	private volatile int totalDays;
	private volatile int gapDate;
	 
	
	public ScTable(String ticker, int rank){
		this.ticker=ticker;
		this.rank=rank;
	}
	
	public int getRank() {
		return rank;
	}

	public  String getTicker() {
		return ticker;
	}

	public synchronized double getGap() {
		return gap;
	}
	public synchronized void setGap(double gap) {
		this.gap = gap;
	}
	public synchronized double getFloatShares() {
		return floatShares;
	}
	public synchronized void setFloatShares(double floatShares) {
		this.floatShares = floatShares;
	}
	public synchronized double getAvgVol() {
		return avgVol;
	}
	public synchronized void setAvgVol(double avgVol) {
		this.avgVol = avgVol;
	}
	public synchronized double getVolume() {
		return volume;
	}
	public synchronized void setVolume(double volume) {
		this.volume = volume;
	}
	public synchronized double getMaxDayChange() {
		return maxDayChange;
	}
	public synchronized void setMaxDayChange(double maxDayChange) {
		this.maxDayChange = maxDayChange;
	}
	public synchronized double getLastPrice() {
		return lastPrice;
	}
	public synchronized void setLastPrice(double lastPrice) {
		this.lastPrice = lastPrice;
	}

	public synchronized double getMarketCap() {
		return marketCap;
	}

	public synchronized void setMarketCap(double marketCap) {
		this.marketCap = marketCap;
	}
	
	public synchronized double getGapPerc() {
		return gapPerc;
	}

	public synchronized void setGapPerc(double gapPerc) {
		this.gapPerc = gapPerc;
	}
	
	
	public synchronized void setTotalDays(int totalDays) {
		this.totalDays = totalDays;
	}

	
	public synchronized int getGapDay() {
		return gapDate;
	}

	public synchronized void setGapDay(int gapDate) {
		this.gapDate = gapDate;
	}

	public String toCsv(){
		return rank + "," + ticker  + "," + gap + "," + gapPerc + "," + floatShares + "," + avgVol + "," + volume + "," + maxDayChange + "," + lastPrice + "," + marketCap + "," + totalDays + "," + gapDate;

	}
	
	
}

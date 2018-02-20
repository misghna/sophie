package com.sesnu.fireball.model;


import java.util.Map;

import com.ib.client.Types.Action;

public class HistResponse {
	
	private Action action;
	private double stp;
	private double entry25;
	private double entry50;
	private double entry75;
	private Map<String,SimilarTrade> matchedTrades;
	private double trendPercent;
	private double reversePercent;
	private boolean goTrade;
	private double idealGapTolerance;
	private int date;
	
	
	
	public HistResponse(Action action, double stp, double entry25, double entry50, double entry75,
						Map<String,SimilarTrade> matchedTrades,double trendPercent,
						double reversePercent,double idealGapTolerance, int date) {
		this.action = action;
		this.stp = stp;
		this.entry25 = entry25;
		this.entry50 = entry50;
		this.entry75 = entry75;
		this.matchedTrades = matchedTrades;
		this.trendPercent = trendPercent;
		this.reversePercent=reversePercent;
		this.goTrade = trendPercent>70 || reversePercent>70;
		this.idealGapTolerance=idealGapTolerance;
		this.date=date;
	}



	public Action getAction() {
		return action;
	}



	public void setAction(Action action) {
		this.action = action;
	}



	public double getStp() {
		return stp;
	}



	public void setStp(double stp) {
		this.stp = stp;
	}



	public double getEntry25() {
		return entry25;
	}



	public void setEntry25(double entry25) {
		this.entry25 = entry25;
	}



	public double getEntry50() {
		return entry50;
	}



	public void setEntry50(double entry50) {
		this.entry50 = entry50;
	}



	public double getEntry75() {
		return entry75;
	}



	public void setEntry75(double entry75) {
		this.entry75 = entry75;
	}



	public Map<String, SimilarTrade> getMatchedTrades() {
		return matchedTrades;
	}



	public double getTrendPercent() {
		return trendPercent;
	}



	public void setTrendPercent(double trendPercent) {
		this.trendPercent = trendPercent;
	}



	public double getReversePercent() {
		return reversePercent;
	}



	public void setReversePercent(double reversePercent) {
		this.reversePercent = reversePercent;
	}



	public boolean isGoTrade() {
		return goTrade;
	}



	public void setGoTrade(boolean goTrade) {
		this.goTrade = goTrade;
	}
	
	
	public int getDate() {
		return date;
	}



	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("Action:" + action + ", ");
		sb.append("STP:" + stp + ", ");
		sb.append("Entry25:" + entry25 + ", ");
		sb.append("Entry50:" + entry50 + ", ");
		sb.append("Entry75:" + entry75 + ", ");
		sb.append("matchedTrades:" + matchedTrades.size() + ", ");
		sb.append("idealGapTolerance:" + idealGapTolerance + ", ");
		sb.append("trendPercent:" + trendPercent + ", ");
		sb.append("reversePercent:" + reversePercent + ", ");
		sb.append("Is_A_Go:" + (trendPercent>70 || reversePercent>70));
		return sb.toString();
	}
	
	
	public String toCsv(){
		StringBuffer sb = new StringBuffer();
		sb.append(action + ", ");
		sb.append(stp + ", ");
		sb.append(entry25 + ", ");
		sb.append(entry50 + ", ");
		sb.append(entry75 + ", ");
		sb.append(matchedTrades.size() + ", ");
		sb.append(idealGapTolerance + ", ");
		sb.append(trendPercent + ", ");
		sb.append(reversePercent + ", ");
		sb.append((trendPercent>70 || reversePercent>70));
		return sb.toString();
	}
	

}

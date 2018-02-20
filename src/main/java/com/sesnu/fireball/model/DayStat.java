package com.sesnu.fireball.model;

import java.util.List;

import com.sesnu.fireball.service.Util;

public class DayStat {
	
	private volatile double min;
	private volatile double max;
	private volatile double gap;
	private volatile double gapPerc;
	
	private volatile int startIndx1;
	private volatile long startVol1;
	private volatile long totalVol1;
	private volatile long totalSVol1; // signed volume total for the day
	private volatile int barSCount1;// signed bar count for the day

	
	private volatile int startIndx5;
	private volatile long startVol5;
	private volatile long totalVol5;
	private volatile long totalSVol5; // signed volume total for the day
	private volatile int barSCount5;// signed bar count for the day
	
	
	//one minute
	public void updateDayStat1(List<FBBar> list){
		if(list.size()<2)return;
		FBBar barA =list.get(list.size()-2);
		FBBar barB =list.get(list.size()-1);
		if(Util.getDate(barA.getStartTime())!=Util.getDate(barB.getStartTime())){
			//reset
			min=5000;max=0;
			startIndx1=list.size()-1;
			startVol1=barB.getVolume();
			gap = barB.open()-barA.getClose();
			gapPerc = Util.roundTo2D(gap/barA.getClose() *100);
			addSignedVolume(barB);
		}else if(totalVol1!=0){
			addSignedVolume(barB);
		}
		
		//set min/max
		min = Math.min(min , barB.low());
		max = Math.max(max ,barB.high());
		
		
	}
	
	private void addSignedVolume(FBBar bar){
		if(bar.getCandleType().equals(CandlePatternType.BULLISH)){
			totalSVol1 += bar.getVolume();
			barSCount1 ++;
		}else if(bar.getCandleType().equals(CandlePatternType.BEARISH)){
			totalSVol1 -= bar.getVolume();
			barSCount1 --;
		}
		totalVol1 += bar.getVolume();
	}

	
	// 5 min
	public void updateDayStat5(List<FBBar> list){
		if(list.size()<2)return;
		FBBar barB =list.get(list.size()-1);
		if(Util.getDate(barB.getStartTime())!=Util.getDate(System.currentTimeMillis()))return;
		FBBar barA =list.get(list.size()-2);
		
		//set gap
		if(Util.getDate(barB.getStartTime())==Util.getDate(System.currentTimeMillis()) && 
				Util.getDate(barA.getStartTime())!=Util.getDate(System.currentTimeMillis())){
			startIndx5=list.size()-1;
			startVol5=barB.getVolume();
			addSignedVolume5(barB);
		}else if(totalVol5!=0){
			addSignedVolume5(barB);
		}
	}
	
	private void addSignedVolume5(FBBar bar){
		if(bar.getCandleType().equals(CandlePatternType.BULLISH)){
			totalSVol5 += bar.getVolume();
			barSCount5 ++;
		}else if(bar.getCandleType().equals(CandlePatternType.BEARISH)){
			totalSVol5 -= bar.getVolume();
			barSCount5 --;
		}
		totalVol5 += bar.getVolume();
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	public double getGap() {
		return gap;
	}

	public double getGapPerc() {
		return gapPerc;
	}

	public int getStartIndx1() {
		return startIndx1;
	}

	public long getStartVol1() {
		return startVol1;
	}

	public long getTotalVol1() {
		return totalVol1;
	}

	public long getTotalSVol1() {
		return totalSVol1;
	}

	public int getBarSCount1() {
		return barSCount1;
	}

	public int getStartIndx5() {
		return startIndx5;
	}

	public long getStartVol5() {
		return startVol5;
	}

	public long getTotalVol5() {
		return totalVol5;
	}

	public long getTotalSVol5() {
		return totalSVol5;
	}

	public int getBarSCount5() {
		return barSCount5;
	}





	
	
}

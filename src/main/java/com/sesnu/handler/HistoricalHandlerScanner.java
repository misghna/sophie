package com.sesnu.handler;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IHistoricalDataHandler;
import com.ib.controller.Bar;
import com.sesnu.fireball.model.FBBar;
import com.sesnu.fireball.model.ScTable;
import com.sesnu.fireball.service.Util;

public class HistoricalHandlerScanner implements IHistoricalDataHandler {

	private static final Logger liveBarLog = LoggerFactory.getLogger("BarLiveLog");

	private boolean done;
	private ScTable scTable;
	private double maxDayChange=0;
	private int i;
	private Map<String,HistoricalHandlerScanner> handleMap ;
	private ApiController api;
	private int totalDays;
	
	public HistoricalHandlerScanner(ScTable scTable, int i, Map<String,HistoricalHandlerScanner> handleMap , ApiController api) {
		this.scTable=scTable;
		this.i=i;
		this.handleMap=handleMap;
		this.api=api;
	}

	private Bar barA;
	private Bar barB;
	@Override
	public void historicalData(Bar bar) {
    	double dayChange = Util.roundTo2D((bar.high()-bar.low())/bar.low()*100);
    	maxDayChange = maxDayChange<dayChange?dayChange:maxDayChange;
    	totalDays ++;
    	barA = barB;
    	barB = bar;
	}

	@Override
	public void historicalDataEnd() {
		scTable.setMaxDayChange(maxDayChange);
		scTable.setTotalDays(totalDays);
		double gap = Util.roundTo2D(barB.open()-barA.close());
		double gapPerc= Util.roundTo2D(gap/barA.close()*100);
		scTable.setGap(gap);
		scTable.setGapPerc(gapPerc);
		scTable.setVolume(Util.roundTo2D((double)barB.volume()/10000));
		scTable.setLastPrice(barB.close());
		scTable.setGapDay(Util.getDate(barB.time()));
		api.cancelHistoricalData(this);
		handleMap.remove(scTable.getTicker());
		this.done=true;
	}

	public boolean isDone(){
		return done;
	}
	


	
}

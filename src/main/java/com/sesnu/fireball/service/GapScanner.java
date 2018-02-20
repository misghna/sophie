package com.sesnu.fireball.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.Contract;
import com.ib.client.ScannerSubscription;
import com.ib.client.Types.BarSize;
import com.ib.client.Types.DurationUnit;
import com.ib.client.Types.WhatToShow;
import com.ib.controller.ApiController;
import com.ib.controller.ScanCode;
import com.sesnu.fireball.model.ScTable;
import com.sesnu.handler.HistoricalHandlerScanner;
import com.sesnu.handler.ScannerHistHandler;

public class GapScanner implements Runnable {

	private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	private ApiController api;
	private Common common;
	private boolean gapUpDone;
	private boolean gapDownDone;
	private String gapType;
	
	public GapScanner(ApiController api,Common common,String gapType){
		this.api=api;
		this.common=common;
		this.gapType=gapType;
	}
	
	@Override
	public void run() {
		try{
			long sTime =System.currentTimeMillis();
			ScannerHistHandler byVolume = new ScannerHistHandler();		
			if(gapType.equals("Up")){
				api.reqScannerSubscription(getSubDetail(ScanCode.HIGH_OPEN_GAP.name(),50), byVolume); 
			}else if(gapType.equals("Down")){
				api.reqScannerSubscription(getSubDetail(ScanCode.LOW_OPEN_GAP.name(),50), byVolume); 
			}
			

			while(!byVolume.isDone() && System.currentTimeMillis()-sTime<15000){
				Thread.sleep(1000);
			}
			
			api.cancelScannerSubscription(byVolume);
			
			List<String> list = byVolume.getScannResult();
			System.out.println(list);
			List<ScTable> scTableList = new ArrayList<ScTable>();
			if(list.size()>0){
				YahooFinanceAPI yf = new YahooFinanceAPI();
				Map<String,HistoricalHandlerScanner> handleMap = new HashMap<String,HistoricalHandlerScanner>();			
				for (int i=0; i<list.size();i++) {			
					String ticker=list.get(i);			
					ScTable scTable = new ScTable(ticker,i);
					yf.analyse(ticker,scTable);
					HistoricalHandlerScanner histHand = new HistoricalHandlerScanner(scTable,i,handleMap,api);
					api.reqHistoricalData(getContract(ticker,null), "",  1, DurationUnit.YEAR,BarSize._1_day, WhatToShow.TRADES, true, false,histHand);
					handleMap.put(ticker,histHand);			
					scTableList.add(scTable);
				}
				
				sTime=System.currentTimeMillis();
				while(handleMap.size()!=0 && System.currentTimeMillis()-sTime<15000){
					Thread.sleep(1000);			
				}
				
				if(gapType.equals("Up")){
					common.setGapUpList(scTableList);
					gapUpDone=true;
					mainL.info("gap up scanner complete");
				}else if(gapType.equals("Down")){
					common.setGapDownList(scTableList);
					gapDownDone=true;
					mainL.info("gap down scanner complete");
				}
				
				
				
			}
		}catch(Exception e){
			mainL.info("error processing scan result for ");
		}

	}

	
	
	public boolean isGapUpDone() {
		return gapUpDone;
	}

	public boolean isGapDownDone() {
		return gapDownDone;
	}
	
	private static ScannerSubscription getSubDetail(String scanCode,int rows){
		ScannerSubscription s = new ScannerSubscription();
		s.instrument("STK");
		s.locationCode("STK.US.MAJOR");
		s.scanCode(scanCode);
//		s.scanCode(ScanCode.HOT_BY_VOLUME.name());
//		s.scanCode(ScanCode.volu);
		s.numberOfRows(rows);
		s.abovePrice(10);
		s.belowPrice(200);
//		s.scanCode(ScanCode.HIGH_OPEN_GAP.name());
//		s.aboveVolume(100000);
		s.marketCapAbove(1000000000);
		return s;
	}
	
	private static Contract getContract(String ticker,String ex){
		   Contract contract = new Contract();
	       contract.symbol(ticker); 
	       contract.secType("STK");
	       contract.currency("USD");	       
	       if(ex==null){
	    	   contract.exchange("SMART"); 
	       }else{
	    	   contract.exchange(ex); 
	       }
	       contract.primaryExch("ISLAND");
	       return contract;
	}

}

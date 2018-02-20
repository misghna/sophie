package com.sesnu.handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.xhtmlrenderer.css.parser.property.PrimitivePropertyBuilders.MarginLeft;

import com.ib.client.Contract;
import com.ib.client.Types.BarSize;
import com.ib.client.Types.DurationUnit;
import com.ib.client.Types.WhatToShow;
import com.ib.controller.ApiController;
import com.sesnu.fireball.service.Common;
import com.sesnu.fireball.service.DaoService;
import com.sesnu.fireball.service.TickerProcessor;
import com.sesnu.fireball.service.WSMessage;


public class TaskHandler {
	
	private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	
	private Map<String,TickerProcessor> activeTickers;
	private Map<String,OneMinHandler> histHandlers;
	private ApiController api;
	private DaoService daoService;
	private AccountHandler acctHandler;
	private PositionHandler posHand;
	private LiveOrdersHandler liveOrdersHand;
	private Common common;
	private Set<String> streamingTickers;
	private boolean guest;
	
	public TaskHandler(ApiController api,DaoService daoService,AccountHandler acctHandler,
			PositionHandler posHand,LiveOrdersHandler liveOrdersHand,Common common){
		this.api=api;
		this.daoService=daoService;
		this.activeTickers=new HashMap<String,TickerProcessor>();
		this.histHandlers=new HashMap<String,OneMinHandler>();
		
		this.acctHandler = acctHandler;		
		this.posHand = posHand;
		this.liveOrdersHand=liveOrdersHand;
		this.common=common;
		streamingTickers = new HashSet<String>();
	}
	
	public void addTicker(String ticker,boolean guest) {
			if(activeTickers.containsKey(ticker))return;
			TickerProcessor tkp = new TickerProcessor(api,ticker,daoService,common,guest);
			posHand.addProcessor(ticker, tkp);
			liveOrdersHand.addProcessor(ticker, tkp);
			OneMinHandler tradesHist =  new OneMinHandler(tkp,ticker);
			api.reqHistoricalData(getContract(ticker,null), "",  5, DurationUnit.DAY,BarSize._1_min, WhatToShow.TRADES, true, true, tradesHist);
			activeTickers.put(ticker, tkp);
			histHandlers.put(ticker, tradesHist);

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

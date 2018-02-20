package com.sesnu.handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.ib.controller.Position;
import com.ib.controller.ApiController.IAccountHandler;
import com.sesnu.fireball.model.AcctHistory;
import com.sesnu.fireball.model.PnL;
import com.sesnu.fireball.model.Portofolio;
import com.sesnu.fireball.service.Common;
import com.sesnu.fireball.service.DaoService;
import com.sesnu.fireball.service.TickerProcessor;
import com.sesnu.fireball.service.Util;
import com.sesnu.fireball.service.WSMessage;

public class AccountHandler implements IAccountHandler {

	
	private Map<String,TickerProcessor> TickerProcessorMap;
	
	private Map<String,Portofolio> positionMap;
	
	private Map<String,Double> realPnlMap;
	private Map<String,Double> unRealPnlMap;
	private double lastTotal=0;
	private DaoService daoService;
	private long lastAcctLog=0;
	private boolean wrapUp;
	private Set<String> activeTickers;
	private Common common;
	private String netLiquid="";
	private Double totalDayChange=0d;
	
	public AccountHandler(DaoService daoService,Common common){
		TickerProcessorMap = new HashMap<String,TickerProcessor>();
		this.positionMap = new HashMap<String,Portofolio>();
		this.realPnlMap = new HashMap<String,Double>();
		this.unRealPnlMap = new HashMap<String,Double>();
		this.activeTickers=new HashSet<String>();
		this.daoService=daoService;
		this.common=common;
	}
	
	public String getNetLiquid(){
		return netLiquid;
	}
	
	@Override
	public void updatePortfolio(Position pos) {
		String ticker = pos.contract().symbol();
		double prevMax = positionMap.containsKey(ticker)?positionMap.get(ticker).getMaxUnreal():0;
		Portofolio portf = new Portofolio(pos,prevMax);
		
		positionMap.put(ticker, portf);
		updatePosition(ticker);
		
		realPnlMap.put(ticker, pos.realPnl());
		unRealPnlMap.put(ticker, pos.unrealPnl());
		activeTickers.add(ticker);
		
		totalDayChange = getTotal(realPnlMap) + getTotal(unRealPnlMap);
		if(Math.abs(totalDayChange-lastTotal)>50){
			lastTotal = totalDayChange;
			PnL pnl = new PnL(System.currentTimeMillis(),getTotal(unRealPnlMap),getTotal(realPnlMap));
	//		daoService.save(pnl);
		}
		sendPortofolio(ticker,portf);
	}
	
//	public void sendReport(){
//		for (Map.Entry<String,Portofolio> enrty : positionMap.entrySet()) {
//			sendPortofolio(enrty.getKey(),enrty.getValue());
//		}
//		common.sendMessage("Total-Day-Change",totalDayChange.toString());
//	}
	
	@SuppressWarnings("unchecked")
	private void sendPortofolio(String ticker,Portofolio portf){
		JSONObject jo = new JSONObject();
		jo.put("Ticker", ticker);
		jo.put("Position", portf.getPos());
		jo.put("Realized", portf.getRealizedPnl());
		jo.put("Unrealized", portf.getUnrealizedPnl());
		jo.put("maxUnreal", portf.getMaxUnreal());
		common.sendMessage("Portfolio_" + ticker +"dd",jo.toJSONString());
		common.sendMessage("realTotal",getTotal(realPnlMap).toString());
		common.sendMessage("unreal_Total",getTotal(unRealPnlMap).toString());
	}
	private Double getTotal(Map<String,Double> pnl){
		double total =0;
		for(Map.Entry<String, Double> entry : pnl.entrySet()){
			total +=entry.getValue();
		}
		return total;
	}
	
	public void addTickerProcessor(String ticker,TickerProcessor proccesor) {
		TickerProcessorMap.put(ticker,proccesor);
		updatePosition(ticker);
	}
	
	private void updatePosition(String ticker){
		if(TickerProcessorMap.containsKey(ticker) && positionMap.containsKey(ticker)){
			TickerProcessorMap.get(ticker).updatePortofolio(positionMap.get(ticker));
		}
	}
		
	
	@Override
	public void accountDownloadEnd(String arg0) {

	}

	@Override
	public void accountTime(String arg0) {


	}

	@Override
	public void accountValue(String acctNo, String type, String amount, String currency) {
		long now = System.currentTimeMillis();
		if(type.equals("NetLiquidation") ){
			if(acctNo.equals(Util.getAcctNo())){
				netLiquid = amount;
				common.sendMessage("Net-Liquidity",amount);
			};
			if(now-lastAcctLog > 1800000 || (!wrapUp && Util.getDoubleTime()>=15.30) ){
				wrapUp = true;
				lastAcctLog = now;
				AcctHistory acctHist = new AcctHistory(now,Double.parseDouble(amount),acctNo);
//				daoService.save(acctHist);
			}
		}
	}



}

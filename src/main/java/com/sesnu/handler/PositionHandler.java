package com.sesnu.handler;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.ib.client.Contract;
import com.ib.controller.ApiController.IPositionHandler;
import com.sesnu.fireball.model.FBPosition;
import com.sesnu.fireball.service.Common;
import com.sesnu.fireball.service.TickerProcessor;
import com.sesnu.fireball.service.WSMessage;

@SuppressWarnings("unchecked")
public class PositionHandler implements IPositionHandler {


	private Map<String,TickerProcessor> processorMap;	
	private volatile Map<String,FBPosition> positionMap;
	private Common common;
	
	public PositionHandler(Common common) {
		this.common=common;
		processorMap = new HashMap<String,TickerProcessor>();
		positionMap = new HashMap<String,FBPosition>();
	}
	
	
	@Override
	public void position(String account, Contract contract, double pos, double avgCost) {
		positionMap.put(contract.symbol(), new FBPosition((int)pos,avgCost));

		updatePosition(contract.symbol());
		updatePosition();
	}



	public void updatePosition(){
		Double totalPositionAmount=0d;
		for(Map.Entry<String, FBPosition> entry : positionMap.entrySet()){
			totalPositionAmount += Math.abs(entry.getValue().getShares()) * entry.getValue().getAvgCost();
		}
		for(Map.Entry<String,TickerProcessor> entry : processorMap.entrySet()){
			entry.getValue().updateTotalUsedAmount(totalPositionAmount);
		}
		common.sendMessage("TotalUsedAmount",totalPositionAmount.toString());
	}
	
	public void addProcessor(String ticker,TickerProcessor proccesor) {
		processorMap.put(ticker,proccesor);
		updatePosition(ticker);
	}
	
	private void updatePosition(String ticker){
		if(processorMap.containsKey(ticker) && positionMap.containsKey(ticker)){
			processorMap.get(ticker).updatePosition(positionMap.get(ticker));
		}
	}

	
	public Map<String,FBPosition> getPositions(){
		return positionMap;
	}
	
	@Override
	public void positionEnd() {}
	

}

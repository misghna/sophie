package com.sesnu.fireball.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.sesnu.fireball.model.ScTable;

@Component
public class Common {
	
	@Autowired SimpMessagingTemplate wsMsgService;
	private  volatile Map<String,String> data;
	private volatile List<ScTable> gapUpList;
	private volatile List<ScTable> gapDownList;
	private volatile Map<String,Long> streamingTickers;
	
	public Common (){
		 System.out.println("************* created");
		 data = new HashMap<String,String>();
		 streamingTickers = new HashMap<String,Long>();
	}
	
	public void addTicker(String ticker){
		streamingTickers.put(ticker, System.currentTimeMillis());
		sendMessage("BrokerGW", "Online, Streaming " + streamingTickers.size() + " Tickers");
	}
	
	public void removeOldTickers(){
		Set<String> removeList = new HashSet<String>();
		for (Map.Entry<String, Long> entry : streamingTickers.entrySet()) {
			if(System.currentTimeMillis()-entry.getValue()>80000){
				removeList.add(entry.getKey());
			}
		}
		for (String ticker : removeList) {
			streamingTickers.remove(ticker);
		}
		if(streamingTickers.size()==0){
			sendMessage("BrokerGW", "Live data feed is not Streaming");
		}
	}
	
	public synchronized void sendMessage(String key,String value){
		if(changed(key,value)){
			
			data.put(key, value);
			if(wsMsgService!=null){
				this.wsMsgService.convertAndSend("/topic/wsMessages",				
						new WSMessage(key,value));
			}
		}
	}

	
	private synchronized boolean changed(String key,String value){
		if(!data.containsKey(key)){
			return true;
		}else if(data.containsKey(key) && !data.get(key).equals(value)){
			return true;
		}else{
			return false;
		}
	}

	public List<String> getInitData() {
		List<String> allData = new ArrayList<String>();
		Map<String,String> data = new HashMap<String,String>(this.data);			
		for (Map.Entry<String, String> entry : data.entrySet()) {
			allData.add(entry.getKey() + "-" +  entry.getValue());
		}
		
		return allData;
	}

	public void setGapUpList(List<ScTable> scTableList) {
		for (ScTable scTable : scTableList) {
			if(scTable.getGapPerc()>1 && scTable.getAvgVol()>=0.5){
				sendMessage("gapUpTicker_" + scTable.getTicker() + "_dd",scTable.toCsv());
			}
		}
		this.gapUpList=scTableList;
		
	}

	public List<ScTable> getGapUpList() {
		return gapUpList;
	}

	public void setGapDownList(List<ScTable> scTableList) {
		for (ScTable scTable : scTableList) {
			if(scTable.getGapPerc()<-3 && scTable.getAvgVol()>=1){
				sendMessage("gapDownTicker_" + scTable.getTicker() + "_dd",scTable.toCsv());
			}
		}
		this.gapDownList=scTableList;
			
	}
	
	public List<ScTable> getGapDownList() {
		return gapDownList;
	}	

}

package com.sesnu.fireball.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sesnu.fireball.dao.TickerDao;
import com.sesnu.fireball.model.CandlePatternType;
import com.sesnu.fireball.model.FBBar;
import com.sesnu.fireball.model.Ticker;


@Component
public class VolumeCalculator {

	private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	
	@Autowired TickerDao tickerDao;
	private Integer lastUpdateDate=0;
	
	public void updateVolumePercent() {
		
		if(lastUpdateDate.equals(Util.getDate(System.currentTimeMillis())))return;
		
		Ticker ticker = tickerDao.findLastOne();
		lastUpdateDate = Util.getDate(ticker.getUpdatedOn());
		Set<String> doneList = new HashSet<String>();
		try{
			if(!lastUpdateDate.equals(Util.getDate(System.currentTimeMillis()))){
				mainL.info("running percent volume update @:{}",Util.getDateTime(System.currentTimeMillis()));
				
				List<Ticker> tickerList = tickerDao.findAll();
				Map<String,List<FBBar>> historicalMapTG = getHistorical(tickerList);
				for (Map.Entry<String, List<FBBar>> entry : historicalMapTG.entrySet()) {
					List<FBBar> list = entry.getValue();
		
					for(Integer v=1;v<=30;v++){
						double volume =0;
						int counter=0; int trueCounter=0; double totalProfit=0;
						List<Double> avgMoveList = new ArrayList<Double>();
						Set<Integer> dates = new HashSet<Integer>();
						for (int i=1;i<list.size();i++) {					
							FBBar bar = list.get(i);
							double heavyVolAvg = avgHeavyVolume(list,i);					
							double time = Util.getDoubleTime(bar.getStartTime());
							volume = heavyVolAvg * v * 0.25;
							double hRatio= Util.roundTo3D(bar.getBodyHeight()/bar.getMah());
							double relHRatio= Util.roundTo3D(bar.getBodyHeight()/bar.getHeight());
							
							if(bar.getAvgVol()>25000 && (bar.getVolume()/bar.getAvgVol())>3 && hRatio>1.5 && relHRatio>0.75 &&
									!isStatic(list,i) && bar.getVolume()>volume && time>10 && time<14.3){
								
								double proft = isProfitable(list,i);
								avgMoveList.add(Math.abs(proft));
								totalProfit += proft;
								boolean profitable=proft>0;
		
								if(profitable){
									trueCounter++;
									dates.add(Util.getDate(bar.getStartTime()));
								}
								counter ++;
		
							}		
						}
						double avgMove = Util.mean(avgMoveList);
						int noStocks = (int) (200/avgMove);
						double invst= avgMoveList.size()>0?noStocks*list.get(0).close():0;
						double perProfit = Math.round((double)trueCounter/(double)counter *100);
						if(counter>=2 && perProfit>70 && invst<50000 && !doneList.contains(entry.getKey())){
							tickerDao.updateTickerVolPerc(entry.getKey(),v);
							doneList.add(entry.getKey());
							mainL.info("{} ~  volume: {} , % winners: {}, total: {}, Profit: {}, % #Stocks:{}, invst$: {}, unique-Dates: ",
							entry.getKey(),(v * 0.25 * 100),trueCounter,counter,perProfit,noStocks,Math.round(invst),dates.size());
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		mainL.info("total volume update count : {} , @: {} ",doneList.size(),Util.getDateTime(System.currentTimeMillis()));
		
	}
	
	private boolean isStatic(List<FBBar> list,int indx){
		Set<Double> hts= new HashSet<Double>();
		Integer noTail= 0;
		if(list.size()<=10 || indx<10)return true;
		for (int i = indx; i>= indx-10; i--) {
			FBBar bar = list.get(i);
			hts.add(bar.getHeight());
			if(bar.getBodyHeight()==bar.getHeight())noTail++;
		}
		return hts.size() < 4 || noTail>4 ;
	}
	
	
	private Map<String,List<FBBar>> getHistorical(List<Ticker> tickers) 
			throws ClientProtocolException, IOException{	
		mainL.info("Fetching historical data from google finance api for {} tickers ",tickers.size());
		Map<String,List<FBBar>> historicalMap = new HashMap<String,List<FBBar>>();
		DataCollector dc = new DataCollector();
		if(tickers!=null && tickers.size()>0){
			for (int i=0; i< tickers.size();i++) {
				String ticker = tickers.get(i).getTicker();
				List<FBBar> barList =  dc.get(15, 60, ticker, false);
				historicalMap.put(ticker, barList);
			}
		}
		return historicalMap;
	}
	
	
	private double avgHeavyVolume(List<FBBar> list,int indx){
		int barCount=Util.barsSinceStart(list.get(indx).getStartTime());
		int startIndx=indx-barCount+1;
		double vol=0;int count=0;
		for(int i=startIndx;i<=startIndx+30;i++){
			if(i>=0 && list.size()>i && Util.getDoubleTime(list.get(i).getStartTime())<=10){
				count ++;
				vol +=list.get(i).getVolume();
			}
		}

		return Math.round(vol/count);		
	}
	
	private double isProfitable(List<FBBar> list,int indx){
		double profit=0;int exitIndx=0;
		FBBar barB = list.get(indx);
		double lossMultiplier=1.25;
		double profitMultiplier=1.5;
		if(barB.getCandleType().equals(CandlePatternType.BULLISH)){
			double stopLoss= barB.close()-barB.getHeight()*lossMultiplier;
			double profitLine=barB.close() + barB.getHeight()*profitMultiplier;
			

			
			for (int i = indx; i<list.size(); i++) {
				if(list.get(i).low()<stopLoss){
					profit =list.get(i).low()-barB.close();
					exitIndx=i;
					break;
				}else if(list.get(i).high()>=profitLine){
					profit=list.get(i).high()-barB.close();
					exitIndx=i;
					break;
				}
			}
		}else if(barB.getCandleType().equals(CandlePatternType.BEARISH)){
			double stopLoss= barB.close() + barB.getHeight()*lossMultiplier;
			double profitLine=barB.close() - barB.getHeight()*profitMultiplier;

			for (int i = indx; i<list.size(); i++) {
				if(list.get(i).high()>stopLoss){
					profit =barB.close() -list.get(i).high();
					exitIndx=i;
					break;
				}else if(list.get(i).low()<=profitLine){
					profit=barB.close()-list.get(i).low();
					exitIndx=i;
					break;
				}
			}
		}
		if(Util.getDate(barB.getStartTime())!=Util.getDate(list.get(exitIndx).getStartTime())){
			return 0;
		}
		return profit;
	}
}

package com.sesnu.fireball.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.OrderState;
import com.ib.controller.ApiController;
import com.sesnu.fireball.model.DayStat;
import com.sesnu.fireball.model.FBBar;
import com.sesnu.fireball.model.FBOrderState;
import com.sesnu.fireball.model.FBPosition;
import com.sesnu.fireball.model.Portofolio;
import com.sesnu.fireball.model.Shares;
import com.sesnu.fireball.model.SubmitedOrder;
import com.sesnu.fireball.strategies.GapDownClose;
import com.sesnu.fireball.strategies.GapDownSlide;
import com.sesnu.fireball.strategies.GapUpClose;
import com.sesnu.fireball.strategies.GapUpShoot;
import com.sesnu.fireball.strategies.Venus;
import com.sesnu.handler.OrderHandler;


public class TickerProcessor {
	
	private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	
	private ApiController api;
	private FBPosition pos;
	private OrderEntry orderEntry;
	private OrderState orderState;
	private int lastParentId;
	private double totalUsedCashAmount;
	private DaoService daoService;
	private long lastLiveOrder;
	private Set<String> partialSet;
	private Set<String> usedEntries;
	private Integer volMultiplier;
	private String ticker;
	private double volumeLimit;
	private List<FBBar> list;
	private Common common;
	private List<SubmitedOrder> submitedOrdersList;
	private Map<Integer,String> orderStatusMap;
	private double instantProfit;
	private boolean guest;
	private DayStat dayStat;
	private HistoricalProcessor hist;

	
	public TickerProcessor(ApiController api,String ticker,DaoService daoService,Common common,boolean guest){
		this.api=api;
		this.common=common;
		this.daoService=daoService;
		this.guest=guest;
		this.pos=new FBPosition(0,0);
		orderEntry = new OrderEntry(ticker,api,new OrderHandler());
		partialSet = new HashSet<String>();
		usedEntries = new HashSet<String>();
		list=new ArrayList<FBBar>();
		submitedOrdersList=new ArrayList<SubmitedOrder>();
		orderStatusMap = new HashMap<Integer,String>();
		this.ticker=ticker;
		this.dayStat=new DayStat();
	}
	

	public void update(FBBar fbBar) {
		if(pos.getShares()!=0){
			instantProfit = (pos.getAvgCost()-fbBar.close())*pos.getShares();
		}else{
			instantProfit =0;
		}
	}

	private long lastSent=0;
	public void addHistBarList(FBBar bar) {
			bar = new Indicators().addIndicators(list, bar);
		    list.add(bar);
		    dayStat.updateDayStat1(list);

		    runApollo(bar);
		    // add one min strategies here

		    aggTo5Min(bar);
		    
		    if(System.currentTimeMillis()-lastSent>50000){
		    	lastSent=System.currentTimeMillis();
		    	common.addTicker(ticker);
		    }
		
	}
	
	private void fiveMinTrigger(FBBar bar){
		dayStat.updateDayStat5(fiveMinBarList);
		if(fiveMinBarList.size()<5)return;

	}
	
	boolean tickerDone=false;
	private void runApollo(FBBar bar){
		if(list.size() > 2 && Util.getDate(list.get(list.size()-1).getStartTime())!=Util.getDate(list.get(list.size()-2).getStartTime()) && 
				Util.getDate(bar.getStartTime())==Util.getDate(System.currentTimeMillis())
				&& Math.abs(dayStat.getGapPerc()) >=0.3 && !tickerDone){
			 mainL.info("{} ~ gapped {} % on {}", ticker,dayStat.getGapPerc(),Util.getDateStr(bar.getStartTime()));
			 mainL.info("barDetail" +  bar.toCSV());
			 hist = new HistoricalProcessor(list,dayStat.getGap(),common);			
			 new Thread(hist).start();
			 tickerDone=true;
		}
		
//		if(!hist.getStatus().isEmpty() && hist.getHistResponse()!=null && hist.getHistResponse().getAction()!=null){
//			
//		}
		
	}



	
	private void processOrder(Shares shares,FBBar bar){

		SubmitedOrder submitedOrder = orderEntry.placeMainOrder(shares.getAction(), shares.getInPrice(), shares.getStopLose(), shares.getExitPrice(), shares.getShares());
		if(submitedOrder!=null){
			submitedOrdersList.add(submitedOrder);
			common.sendMessage("Order_status",submitedOrder.toJson());
			lastParentId = submitedOrder.getOrder().orderId();
			mainL.info("{} ~ New order submited, orderId:{}, Action: {}, shares: {},ema: {}, priceWhenPlaced:{}, slope% :{}, slopechange%: {},Strategy2 ,time: {}",
					bar.getTicker(),lastParentId, shares.getAction().name(),shares,bar.getEmaSlow(),bar.close(),Util.roundTo2D(bar.getEmaSlope()),
							Util.roundTo2D(bar.getEmaSlopeChange()),Util.getDoubleTime(bar.getStartTime()));
		}
	}
	



	public void updateTotalUsedAmount(double totalPositionAmount) {
		this.totalUsedCashAmount=totalPositionAmount;
		
	}


	public void updatePosition(FBPosition pos) {
		this.pos = pos;		
	}
	

	public void setOrderStatus(FBOrderState orderStatus){

		orderStatusMap.put(orderStatus.getOrder().orderId(), orderStatus.getOrderState().getStatus());
		
//		if(orderStatus.getOrder().orderId()==lastParentId){
//			this.orderState = orderStatus.getOrderState();
//		}else if(orderStatus.getOrder().getOrderType().equals(OrderType.STP)){
////			renkoChart.insertStpPrice(orderStatus.getOrder().auxPrice());
//		}
////		orderStatus.getOrderState().status().

	}


	public void updatePortofolio(Portofolio portofolio) {
		// TODO Auto-generated method stub
		
	}


	public void updateLive(FBBar fbBar) {
		// TODO Auto-generated method stub
		
	}
	
	
	List<FBBar> fiveMinBarList = new ArrayList<FBBar>();
	List<FBBar> aggBarList1 = new ArrayList<FBBar>();	
	private void aggTo5Min(FBBar fBar){
	
		 Integer min =Util.getMinTime(fBar.getStartTime());
		 aggBarList1.add(fBar);
		 if((min+1)%5==0){
			 //agg				 
			 List<List<FBBar>> groups= groupIntervals(aggBarList1);
			 for (List<FBBar> aggBarList : groups) {
				 long iVol=0;double low=1000;double high=0;
				 for (FBBar fbBar : aggBarList) {
					iVol +=fbBar.getVolume();
					low = fbBar.low()<low?fbBar.low():low;
					high = fbBar.high() >high?fbBar.high():high;
				}
				 FBBar openBar = aggBarList.get(0);
				 Integer sMin =Util.getMinTime(openBar.getStartTime());
				 long correctedStartTime = openBar.getStartTime() - (sMin%5) * 60000;
				 FBBar closeBar = aggBarList.get(aggBarList.size()-1);
				 FBBar nFBBar = new FBBar(fBar.getTicker(),correctedStartTime,openBar.open(),high,low,closeBar.close(),iVol);
				 nFBBar = new Indicators().addIndicators(fiveMinBarList, nFBBar);
				 fiveMinBarList.add(nFBBar);
	//			 System.out.println(nFBBar.toCSV());
				 fiveMinTrigger(nFBBar);
				 aggBarList1 = new ArrayList<FBBar>();
			}
		 }
	}
	

	
	
	private List<List<FBBar>> groupIntervals(List<FBBar> list){
		List<List<FBBar>> groups = new ArrayList<List<FBBar>>();
		List<FBBar> gmates = new ArrayList<FBBar>();
		Integer min =Util.getMinTime(list.get(0).getStartTime());
		int ceil = (int)Math.floor((double)min/5);
		for (FBBar v : list) {
			Integer iMin =Util.getMinTime(v.getStartTime());
			int iCeil = (int)Math.floor((double)iMin/5);
			if(iCeil==ceil){
				gmates.add(v);
			}else{
				groups.add(gmates);
				gmates = new ArrayList<FBBar>();
				gmates.add(v);
				ceil =  iCeil;
			}
		}
		groups.add(gmates);
		return groups;
	}
	
	private double distPercent(List<FBBar> list,int indx){
		double distanceFromEma=Math.abs(list.get(indx).close()-list.get(indx).getEmaMedium());
		double distPercent = Util.roundTo3D(distanceFromEma/list.get(indx).close()*100);
		return distPercent;
	}
	

	
	

}

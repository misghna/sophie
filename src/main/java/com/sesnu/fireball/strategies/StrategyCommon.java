package com.sesnu.fireball.strategies;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sesnu.fireball.model.FBBar;
import com.sesnu.fireball.model.Shares;
import com.sesnu.fireball.model.SubmitedOrder;
import com.sesnu.fireball.model.Ticker;
import com.sesnu.fireball.service.Common;
import com.sesnu.fireball.service.OrderEntry;
import com.sesnu.fireball.service.Util;

public class StrategyCommon {

	private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	
	OrderEntry orderEntry;
	Common common;
	List<SubmitedOrder> submitedOrdersList;
	
	public StrategyCommon(OrderEntry orderEntry,Common common, List<SubmitedOrder> submitedOrdersList){
		this.orderEntry=orderEntry;
		this.common=common;
		this.submitedOrdersList=submitedOrdersList;
	}
	
	public double distPercent(List<FBBar> list,int indx){
		double distanceFromEma=Math.abs(list.get(indx).close()-list.get(indx).getEmaMedium());
		double distPercent = Util.roundTo3D(distanceFromEma/list.get(indx).close()*100);
		return distPercent;
	}
	
	public double avgHeavyVolume(List<FBBar> list,int indx){
		int barCount=Util.barsSinceStart(list.get(indx).getStartTime());
		
		int startIndx=indx-barCount+1;
		double vol=0;int count=0;
		for(int i=startIndx;i<=startIndx+30;i++){
			if(i>=0 && i < list.size() && Util.getDoubleTime(list.get(i).getStartTime())<=10){
				count ++;
				vol +=list.get(i).getVolume();
			}
		}
		return Math.round(vol/count);		
	}
	
	
	public boolean isStatic(List<FBBar> list){
		Set<Double> hts= new HashSet<Double>();
		Integer noTail= 0;
		if(list.size()<=11)return true;
		for (int i = list.size()-1; i>= list.size()-11; i--) {
			FBBar bar = list.get(i);
			hts.add(bar.getHeight());
			if(bar.getBodyHeight()==bar.getHeight())noTail++;
		}
		return hts.size() < 4 || noTail>4 ;
	}
	

	public void processOrder(Shares shares,FBBar bar){

		SubmitedOrder submitedOrder = orderEntry.placeMainOrder(shares.getAction(), shares.getInPrice(), shares.getStopLose(), shares.getExitPrice(), shares.getShares());
		if(submitedOrder!=null){
			submitedOrdersList.add(submitedOrder);
			common.sendMessage("Order_status",submitedOrder.toJson());
			mainL.info("{} ~ New order submited, orderId:{}, Action: {}, shares: {},ema: {}, priceWhenPlaced:{}, slope% :{}, slopechange%: {},Strategy2 ,time: {}",
					bar.getTicker(),submitedOrder.getOrder().parentId(), shares.getAction().name(),shares,bar.getEmaSlow(),bar.close(),Util.roundTo2D(bar.getEmaSlope()),
							Util.roundTo2D(bar.getEmaSlopeChange()),Util.getDoubleTime(bar.getStartTime()));
		}
	}
}

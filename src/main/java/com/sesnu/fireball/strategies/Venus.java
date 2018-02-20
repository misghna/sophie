package com.sesnu.fireball.strategies;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sesnu.fireball.model.CandlePatternType;
import com.sesnu.fireball.model.FBBar;
import com.sesnu.fireball.model.Shares;
import com.sesnu.fireball.model.SubmitedOrder;
import com.sesnu.fireball.service.Common;
import com.sesnu.fireball.service.OrderEntry;
import com.sesnu.fireball.service.Util;

public class Venus extends StrategyCommon{

	private Common common;
	
	public Venus(OrderEntry orderEntry,Common common, List<SubmitedOrder> submitedOrdersList){
		super(orderEntry,common,submitedOrdersList);
		this.common=common;
	}
	
	private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	
	public void run(List<FBBar> list){
		
		if(list.size() < 2) return;
		
		 FBBar barA= list.get(list.size()-2);
		 FBBar barB= list.get(list.size()-1);
		 String ticker = barA.getTicker();
		 Double time = Util.getDoubleTime(barB.getStartTime());


			 if(barB.getVolume() > 100000
					 && barB.getCandleType().equals(CandlePatternType.BEARISH) 
					 && barB.getVolume()<barB.getAvgVol() *0.52
					 && barB.getVolume()>barB.getAvgVol() *.44
					 && barB.getEmaSlow()/barB.low()>1.011
					 && barB.getEmaSlow()/barB.getEmaFast()>1.0048
					 && barB.getEmaSlow()/barB.getEmaFast()<1.0116
					 ){
				 
				   Shares shares = new Shares(barA,barB,"Venus2");
				 
					mainL.info("Pre-Order {} ~ vol: {}, gap: {}, gap%: {},InvProfitRatio: {},RiskReward: {},slope: {},Inv:{},Strategy: Venus2 ,isReasonable:{}, @: {}",
							ticker,barB.getVolume(),0,0,shares.getInvProfRatio(),shares.getRiskReward(),barB.getEmaSlope(),
							shares.getFundRequired(),shares.isReasonable(),time);
					if(shares.isReasonable())common.sendMessage("orderCandidate_" + ticker + "_" + time +"dd",shares.toReport(ticker));
					
					if(shares.isReasonable() && (Util.isDevMode() || System.currentTimeMillis()-barB.getStartTime()<70000)){
						processOrder(shares,barB);
					}
			 }
	}
}

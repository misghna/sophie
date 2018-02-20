package com.sesnu.fireball.strategies;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sesnu.fireball.model.FBBar;
import com.sesnu.fireball.model.Shares;
import com.sesnu.fireball.model.SubmitedOrder;
import com.sesnu.fireball.service.Common;
import com.sesnu.fireball.service.OrderEntry;
import com.sesnu.fireball.service.Util;

public class GapUpClose extends StrategyCommon{

	private Common common;
	
	public GapUpClose(OrderEntry orderEntry,Common common, List<SubmitedOrder> submitedOrdersList){
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
		 
		 if(barB.getStartTime()-barA.getStartTime()>(60 * 60000)){
			 double gap = barB.open()-barA.close();
			 double gapPerc = Util.roundTo2D(gap/barA.close()*100);

			 if(gapPerc>0.95 && gapPerc<3 
//					 && barB.getCandleType().equals(CandlePatternType.BULLISH)
					 && barA.getEmaSlow()/barA.getEmaFast()>1.004
					 && barB.getVolume() > 15000){
				 

				 	Shares shares = new Shares(barA,barB,"GapUpClose");
				 
					mainL.info("Pre-Order {} ~ vol: {}, gap: {}, gap%: {},InvProfitRatio: {},RiskReward: {},slope: {},Inv:{},Strategy: GapUpClose ,isReasonable:{}, @: {}",
							ticker,barB.getVolume(),gap,gapPerc,shares.getInvProfRatio(),shares.getRiskReward(),barB.getEmaSlope(),
							shares.getFundRequired(),shares.isReasonable(),time);
					if(shares.isReasonable())common.sendMessage("orderCandidate_" + ticker + "_" + time +"dd",shares.toReport(ticker));
					
					if(shares.isReasonable() && (Util.isDevMode() || System.currentTimeMillis()-barB.getStartTime()<70000)){
						processOrder(shares,barB);
					}
			 }
		 }
	}
}

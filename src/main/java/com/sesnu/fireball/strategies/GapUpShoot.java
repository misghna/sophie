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

public class GapUpShoot extends StrategyCommon{

	private Common common;
	
	public GapUpShoot(OrderEntry orderEntry,Common common, List<SubmitedOrder> submitedOrdersList){
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
				double g2=0.5;
				double g1=2.1;
			 if(gapPerc>g2 && gapPerc<g1
			 && (barB.getCandleType().equals(CandlePatternType.BEARISH) || barB.getCandleType().equals(CandlePatternType.DOJI))
					 && barA.getEmaFast()/barA.getEmaSlow()>1.003 && barA.getEmaFast()/barA.getEmaSlow()<1.01
					 && barB.getVolume() > 15000){
				 

				 	Shares shares = new Shares(barA,barB,"GapUpShoot");
				 
					mainL.info("Pre-Order {} ~ vol: {}, gap: {}, gap%: {},InvProfitRatio: {},RiskReward: {},slope: {},Inv:{},Strategy: GapUpShoot ,isReasonable:{}, @: {}",
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

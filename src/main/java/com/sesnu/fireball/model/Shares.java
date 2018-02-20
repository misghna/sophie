package com.sesnu.fireball.model;

import org.json.simple.JSONObject;

import com.ib.client.Types.Action;
import com.sesnu.fireball.service.Util;

public class Shares {
	
	private int shares;
	private double riskReward;
	private double invProfRatio;
	private double fundRequired;
	private boolean isReasonable;
	private double exitPrice;
	private double inPrice;
	private double exitPrice2;
	private double inPrice2;
	private double stopLose;
	private Action action;
	private double trend;
	private long time;
	private String strategy;
	
	
	public Shares(Shares shares) {
		this.shares = shares.getShares();
		this.riskReward = shares.getRiskReward();
		this.invProfRatio = shares.getInvProfRatio();
		this.fundRequired = shares.getFundRequired();
		this.isReasonable = shares.isReasonable;
		this.exitPrice = shares.getExitPrice();
		this.inPrice = shares.getInPrice();
		this.exitPrice2 = shares.getExitPrice2();
		this.inPrice2 = shares.getInPrice2();
		this.stopLose = shares.getStopLose();
		this.action = shares.getAction();
		this.trend=shares.getTrend();
		this.time=shares.getTime();
	}

	public Shares(FBBar barA,FBBar barB, String strategy){
		if(strategy.equals("Venus"))calVenusShares(barB);
		if(strategy.equals("Rocket"))calRocketShares(barB);
		if(strategy.equals("Gordon1")||strategy.equals("Gordon5"))calGordonShares(barB);
		if(strategy.equals("GapDownSlide"))calGapDownSlideShares(barA,barB);
		if(strategy.equals("GapDownClose"))calGapDownCloseShares(barA,barB);
		if(strategy.equals("SmallGapDownClose"))calSmallGapDownCloseShares(barA,barB);
		if(strategy.equals("GapUpShoot"))calGapUpShootShares(barA,barB);
		if(strategy.equals("GapUpClose"))calGapUpCloseShares(barA,barB);
		if(strategy.equals("Venus2"))calVenus2Shares(barA,barB);
		
		this.strategy=strategy;
	}

	
	private void calGapUpCloseShares(FBBar barA,FBBar barB) {
		
		// buy 
		 double gap = barB.open()-barA.close();
		 double gapPerc = Util.roundTo2D(gap/barA.close()*100);
		 
		 double stpMulti= ((gapPerc-3)*(1.2-.8))/(0.95-3)+0.8;
		 double profitMulti= ((gapPerc-3)*(1.2-.8))/(.95-3)+0.8;
		 double stp= Util.roundTo2D(barB.close()+Math.abs(gap)*stpMulti);							 
		 double profitTaker = Util.roundTo2D(barB.close() - Math.abs(gap)*profitMulti);
		 
		double maxRisk=250;
		
		inPrice = barB.close()-0.01;
		stopLose = stp;
		exitPrice = profitTaker;
		action=Action.SELL;

		
		this.trend=barB.getEmaSlope();
		stopLose=Util.roundTo2D(stopLose);
		exitPrice=Util.roundTo2D(exitPrice);
		inPrice=Util.roundTo2D(inPrice);
		exitPrice2=Util.roundTo2D(exitPrice2);
		inPrice2=Util.roundTo2D(inPrice2);

		
		time=barB.getStartTime();
		// calculate shares based on max cash allowed
		int maxAmoutPerTrade =Util.getInt("maxAmoutPerTrade");
		maxAmoutPerTrade = 25000;
		int shares = (int) (maxAmoutPerTrade/inPrice);
		
		//limit based on available funds
//		double availableFunds = Util.getDouble("maxCashFlow") * 0.95 - 100000;
//		shares = (int) (availableFunds < (shares * inPrice)? availableFunds/inPrice:shares);
		
		// calculate risk/profite margin
		int risk = (int) Math.abs(maxRisk/(inPrice - stopLose));
		shares = shares > risk ? risk:shares; 
		
		//round to hundredth
		shares= (int) Math.round((double)shares/100)*100;
		
		// check for investment to profit ratio
		double expectedProfit = Math.abs((inPrice - exitPrice)*shares);
		double investment = inPrice*shares;
		double InvProR = expectedProfit/investment*100;
		double commusion= shares/100;
		this.shares=shares;
		this.fundRequired = shares * inPrice;
		this.riskReward= Util.roundTo2D(expectedProfit);
		this.invProfRatio = Util.roundTo3D(InvProR) ;
		this.isReasonable = shares>=100 && shares<=5000 && InvProR>0.25 && expectedProfit>=150 && expectedProfit >=5*commusion;
		
	}
	
	
	private void calGapUpShootShares(FBBar barA,FBBar barB) {
		
		// buy 
		
		double g2=0.5;
		double g1=2.1;
		
		 double gap = barB.open()-barA.close();
		 double gapPerc = Util.roundTo2D(gap/barA.close()*100);
		 
		 double multi= ((gapPerc-g1)*(1.5-.75))/(g2-g1)+.75;
		 
		 double stp= Util.roundTo2D(barB.close()-Math.abs(gap)*multi);							 
		 double profitTaker = Util.roundTo2D(barB.close() + Math.abs(gap)*multi);
		 
		double maxRisk=250;
		
		inPrice = barB.close()+0.01;
		stopLose = stp;
		exitPrice = profitTaker;
		action=Action.BUY;

		
		this.trend=barB.getEmaSlope();
		stopLose=Util.roundTo2D(stopLose);
		exitPrice=Util.roundTo2D(exitPrice);
		inPrice=Util.roundTo2D(inPrice);
		exitPrice2=Util.roundTo2D(exitPrice2);
		inPrice2=Util.roundTo2D(inPrice2);

		
		time=barB.getStartTime();
		// calculate shares based on max cash allowed
		int maxAmoutPerTrade =Util.getInt("maxAmoutPerTrade");
		maxAmoutPerTrade = 25000;
		int shares = (int) (maxAmoutPerTrade/inPrice);
		
		//limit based on available funds
//		double availableFunds = Util.getDouble("maxCashFlow") * 0.95 - 100000;
//		shares = (int) (availableFunds < (shares * inPrice)? availableFunds/inPrice:shares);
		
		// calculate risk/profite margin
		int risk = (int) Math.abs(maxRisk/(inPrice - stopLose));
		shares = shares > risk ? risk:shares; 
		
		//round to hundredth
		shares= (int) Math.round((double)shares/100)*100;
		
		// check for investment to profit ratio
		double expectedProfit = Math.abs((inPrice - exitPrice)*shares);
		double investment = inPrice*shares;
		double InvProR = expectedProfit/investment*100;
		double commusion= shares/100;
		this.shares=shares;
		this.fundRequired = shares * inPrice;
		this.riskReward= Util.roundTo2D(expectedProfit);
		this.invProfRatio = Util.roundTo3D(InvProR) ;
		this.isReasonable = shares>=100 && shares<=5000 && InvProR>0.25 && expectedProfit>=150 && expectedProfit >=5*commusion;
		
	}

	private void calGapDownSlideShares(FBBar barA,FBBar barB) {
		
		 double gap = barB.open()-barA.close();
		 double gapPerc = Util.roundTo2D(gap/barA.close()*100);
		 
		// short sell
	   	double g2=-0.3;
		double g1=-2.1;
		 double multi= ((gapPerc-g1)*(1.5-0.8))/(g2-g1)+0.8;		 
		 double stp= Util.roundTo2D(barB.close()+Math.abs(gap)*multi);							 
		 double profitTaker = Util.roundTo2D(barB.close() - Math.abs(gap)*multi);
		 
		double maxRisk=250;
		
		inPrice = barB.close()-0.01;
		stopLose = stp;
		exitPrice = profitTaker;
		action=Action.SELL;

		
		this.trend=barB.getEmaSlope();
		stopLose=Util.roundTo2D(stopLose);
		exitPrice=Util.roundTo2D(exitPrice);
		inPrice=Util.roundTo2D(inPrice);
		exitPrice2=Util.roundTo2D(exitPrice2);
		inPrice2=Util.roundTo2D(inPrice2);

		
		time=barB.getStartTime();
		// calculate shares based on max cash allowed
		int maxAmoutPerTrade =Util.getInt("maxAmoutPerTrade");
		maxAmoutPerTrade = 25000;
		int shares = (int) (maxAmoutPerTrade/inPrice);
		
		//limit based on available funds
//		double availableFunds = Util.getDouble("maxCashFlow") * 0.95 - 100000;
//		shares = (int) (availableFunds < (shares * inPrice)? availableFunds/inPrice:shares);
		
		// calculate risk/profite margin
		int risk = (int) Math.abs(maxRisk/(inPrice - stopLose));
		shares = shares > risk ? risk:shares; 
		
		//round to hundredth
		shares= (int) Math.round((double)shares/100)*100;
		
		// check for investment to profit ratio
		double expectedProfit = Math.abs((inPrice - exitPrice)*shares);
		double investment = inPrice*shares;
		double InvProR = expectedProfit/investment*100;
		double commusion= shares/100;
		this.shares=shares;
		this.fundRequired = shares * inPrice;
		this.riskReward= Util.roundTo2D(expectedProfit);
		this.invProfRatio = Util.roundTo3D(InvProR) ;
		this.isReasonable = shares>=100 && shares<=5000 && InvProR>0.25 && expectedProfit>=150 && expectedProfit >=5*commusion;
		
	}
	
	private void calVenus2Shares(FBBar barA,FBBar barB) {
		
		double multi= Math.sqrt(barB.getHeight() * (barB.getEmaSlow()-barB.getEmaFast()) ) * 2.46;	 
		 double stp= Util.roundTo2D(barB.close()+multi);							 
		 double profitTaker = Util.roundTo2D(barB.close() - multi);
		 
		double maxRisk=250;
		
		inPrice = barB.close()-0.01;
		stopLose = stp;
		exitPrice = profitTaker;
		action=Action.SELL;

		
		this.trend=barB.getEmaSlope();
		stopLose=Util.roundTo2D(stopLose);
		exitPrice=Util.roundTo2D(exitPrice);
		inPrice=Util.roundTo2D(inPrice);
		exitPrice2=Util.roundTo2D(exitPrice2);
		inPrice2=Util.roundTo2D(inPrice2);

		
		time=barB.getStartTime();
		// calculate shares based on max cash allowed
		int maxAmoutPerTrade =Util.getInt("maxAmoutPerTrade");
		maxAmoutPerTrade = 25000;
		int shares = (int) (maxAmoutPerTrade/inPrice);
		
		//limit based on available funds
//		double availableFunds = Util.getDouble("maxCashFlow") * 0.95 - 100000;
//		shares = (int) (availableFunds < (shares * inPrice)? availableFunds/inPrice:shares);
		
		// calculate risk/profite margin
		int risk = (int) Math.abs(maxRisk/(inPrice - stopLose));
		shares = shares > risk ? risk:shares; 
		
		//round to hundredth
		shares= (int) Math.round((double)shares/100)*100;
		
		// check for investment to profit ratio
		double expectedProfit = Math.abs((inPrice - exitPrice)*shares);
		double investment = inPrice*shares;
		double InvProR = expectedProfit/investment*100;
		double commusion= shares/100;
		this.shares=shares;
		this.fundRequired = shares * inPrice;
		this.riskReward= Util.roundTo2D(expectedProfit);
		this.invProfRatio = Util.roundTo3D(InvProR) ;
		this.isReasonable = shares>=100 && shares<=5000 && InvProR>0.25 && expectedProfit>=150 && expectedProfit >=5*commusion;
		
	}

	
	
	private void calGapDownCloseShares(FBBar barA,FBBar barB) {
		
		// buy sell
		 double gap = barB.open()-barA.close();
		 double gapPerc = Util.roundTo2D(gap/barA.close()*100);
		 
		 double g2=-0.4;
		 double g1=-3.2;
		 		
		 double multi= ((gapPerc-g1)*(1.3-0.5))/(g2-g1)+0.5;
		 double stp= Util.roundTo2D(barB.close()-Math.abs(gap)*multi);							 
		 double profitTaker = Util.roundTo2D(barB.close() + Math.abs(gap)*multi);
		 
		double maxRisk=250;
		
		inPrice = barB.close()+0.01;
		stopLose = stp;
		exitPrice = profitTaker;
		action=Action.BUY;

		
		this.trend=barB.getEmaSlope();
		stopLose=Util.roundTo2D(stopLose);
		exitPrice=Util.roundTo2D(exitPrice);
		inPrice=Util.roundTo2D(inPrice);
		exitPrice2=Util.roundTo2D(exitPrice2);
		inPrice2=Util.roundTo2D(inPrice2);

		
		time=barB.getStartTime();
		// calculate shares based on max cash allowed
		int maxAmoutPerTrade =Util.getInt("maxAmoutPerTrade");
		maxAmoutPerTrade = 25000;
		int shares = (int) (maxAmoutPerTrade/inPrice);
		
		//limit based on available funds
//		double availableFunds = Util.getDouble("maxCashFlow") * 0.95 - 100000;
//		shares = (int) (availableFunds < (shares * inPrice)? availableFunds/inPrice:shares);
		
		// calculate risk/profite margin
		int risk = (int) Math.abs(maxRisk/(inPrice - stopLose));
		shares = shares > risk ? risk:shares; 
		
		//round to hundredth
		shares= (int) Math.round((double)shares/100)*100;
		
		// check for investment to profit ratio
		double expectedProfit = Math.abs((inPrice - exitPrice)*shares);
		double investment = inPrice*shares;
		double InvProR = expectedProfit/investment*100;
		double commusion= shares/100;
		this.shares=shares;
		this.fundRequired = shares * inPrice;
		this.riskReward= Util.roundTo2D(expectedProfit);
		this.invProfRatio = Util.roundTo3D(InvProR) ;
		this.isReasonable = shares>=100 && shares<=5000 && InvProR>0.25 && expectedProfit>=150 && expectedProfit >=5*commusion;
		
	}
	
	private void calSmallGapDownCloseShares(FBBar barA,FBBar barB) {
		
		// buy sell
		 double gap = barB.open()-barA.close();
		 double gapPerc = Util.roundTo2D(gap/barA.close()*100);
		 
		 double stp= barB.close()-Math.abs(gap)*1.6;
		 double profitTaker = barB.close() - Math.abs(gap)*1.8;
		 
		double maxRisk=250;
		
		inPrice = barB.close()+0.01;
		stopLose = stp;
		exitPrice = profitTaker;
		action=Action.BUY;

		
		this.trend=barB.getEmaSlope();
		stopLose=Util.roundTo2D(stopLose);
		exitPrice=Util.roundTo2D(exitPrice);
		inPrice=Util.roundTo2D(inPrice);
		exitPrice2=Util.roundTo2D(exitPrice2);
		inPrice2=Util.roundTo2D(inPrice2);

		
		time=barB.getStartTime();
		// calculate shares based on max cash allowed
		int maxAmoutPerTrade =Util.getInt("maxAmoutPerTrade");
		maxAmoutPerTrade = 25000;
		int shares = (int) (maxAmoutPerTrade/inPrice);
		
		//limit based on available funds
//		double availableFunds = Util.getDouble("maxCashFlow") * 0.95 - 100000;
//		shares = (int) (availableFunds < (shares * inPrice)? availableFunds/inPrice:shares);
		
		// calculate risk/profite margin
		int risk = (int) Math.abs(maxRisk/(inPrice - stopLose));
		shares = shares > risk ? risk:shares; 
		
		//round to hundredth
		shares= (int) Math.round((double)shares/100)*100;
		
		// check for investment to profit ratio
		double expectedProfit = Math.abs((inPrice - exitPrice)*shares);
		double investment = inPrice*shares;
		double InvProR = expectedProfit/investment*100;
		double commusion= shares/100;
		this.shares=shares;
		this.fundRequired = shares * inPrice;
		this.riskReward= Util.roundTo2D(expectedProfit);
		this.invProfRatio = Util.roundTo3D(InvProR) ;
		this.isReasonable = shares>=100 && shares<=5000 && InvProR>0.25 && expectedProfit>=150 && expectedProfit >=5*commusion;
		
	}
	
	private void calGordonShares(FBBar barB) {
		
		// short sell
		

		double maxRisk=250;
		
		inPrice = barB.close() -0.01;
		stopLose = barB.high() + 0.01;
		exitPrice = inPrice - barB.getHeight();
		action=Action.SELL;
		
		inPrice2 = inPrice;
		exitPrice2 = inPrice - barB.getHeight()*1.5;

		
		this.trend=barB.getEmaSlope();
		stopLose=Util.roundTo2D(stopLose);
		exitPrice=Util.roundTo2D(exitPrice);
		inPrice=Util.roundTo2D(inPrice);
		exitPrice2=Util.roundTo2D(exitPrice2);
		inPrice2=Util.roundTo2D(inPrice2);

		
		time=barB.getStartTime();
		// calculate shares based on max cash allowed
		int maxAmoutPerTrade =Util.getInt("maxAmoutPerTrade");
		maxAmoutPerTrade = 25000;
		int shares = (int) (maxAmoutPerTrade/inPrice);
		
		//limit based on available funds
//		double availableFunds = Util.getDouble("maxCashFlow") * 0.95 - 100000;
//		shares = (int) (availableFunds < (shares * inPrice)? availableFunds/inPrice:shares);
		
		// calculate risk/profite margin
		int risk = (int) Math.abs(maxRisk/(inPrice - stopLose));
		shares = shares > risk ? risk:shares; 
		
		//round to hundredth
		shares= (int) Math.round((double)shares/100)*100;
		
		// check for investment to profit ratio
		double expectedProfit = Math.abs((inPrice - exitPrice)*shares);
		double investment = inPrice*shares;
		double InvProR = expectedProfit/investment*100;
		double commusion= shares/100;
		this.shares=shares;
		this.fundRequired = shares * inPrice;
		this.riskReward= Util.roundTo2D(expectedProfit);
		this.invProfRatio = Util.roundTo3D(InvProR) ;
		this.isReasonable = shares>=100 && shares<=5000 && InvProR>0.25 && expectedProfit>=150 && expectedProfit >=5*commusion;
		
	}

	private void calRocketShares(FBBar barB) {
		
		double lossDistance=barB.getHeight() * 1.4;
		double profitDistance=barB.getHeight() * 1.8;
		double maxRisk=250;
		
		if(barB.getCandleType().equals(CandlePatternType.BULLISH)){
			inPrice = barB.close();
			stopLose = barB.close()-lossDistance;
			exitPrice = inPrice + profitDistance;			
			action=Action.BUY;
			
		}else if(barB.getCandleType().equals(CandlePatternType.BEARISH)){
			inPrice = barB.close();
			stopLose = barB.high()+lossDistance;
			exitPrice = inPrice - profitDistance;
			action=Action.SELL;
		}
		
		this.trend=barB.getEmaSlope();
		stopLose=Util.roundTo2D(stopLose);
		exitPrice=Util.roundTo2D(exitPrice);
		inPrice=Util.roundTo2D(inPrice);

		
		time=barB.getStartTime();
		// calculate shares based on max cash allowed
		int shares = (int) (Util.getInt("maxAmoutPerTrade")/inPrice);
		
		//limit based on available funds
//		double availableFunds = Util.getDouble("maxCashFlow") * 0.95 - 100000;
//		shares = (int) (availableFunds < (shares * inPrice)? availableFunds/inPrice:shares);
		
		// calculate risk/profite margin
		int risk = (int) Math.abs(maxRisk/(inPrice - stopLose));
		shares = shares > risk ? risk:shares; 
		
		//round to hundredth
		shares= (int) Math.round((double)shares/100)*100;
		
		// check for investment to profit ratio
		double expectedProfit = Math.abs((inPrice - exitPrice)*shares);
		double investment = inPrice*shares;
		double InvProR = expectedProfit/investment*100;
		double commusion= shares/100;
		this.shares=shares;
		this.fundRequired = shares * inPrice;
		this.riskReward= Util.roundTo2D(expectedProfit);
		this.invProfRatio = Util.roundTo3D(InvProR) ;
		this.isReasonable = shares>=100 && shares<=5000 && InvProR>0.25 && expectedProfit>=150 && expectedProfit >=5*commusion;
		
	}

	private void calVenusShares(FBBar barB){
		
		double lossDistance=0.01;
		double profitMultiplier=1.0;
		double entry2Ratio=0.3;
		double maxRisk=250;
		
		boolean intrend=false;
		if(barB.getCandleType().equals(CandlePatternType.BULLISH)){
			inPrice = barB.close();
			stopLose = barB.low()-lossDistance;
			exitPrice = inPrice + (inPrice- stopLose) * profitMultiplier;
			
			
			inPrice2 = barB.close()-entry2Ratio*barB.getBodyHeight();
			exitPrice2 = inPrice2 + (inPrice2 - stopLose) * profitMultiplier;
			action=Action.BUY;
			intrend = barB.getEmaSlope()>0;
			
		}else if(barB.getCandleType().equals(CandlePatternType.BEARISH)){
			inPrice = barB.close();
			stopLose = barB.high()+lossDistance;
			exitPrice = inPrice - (stopLose-inPrice)*profitMultiplier;
						
			inPrice2 = barB.close()+entry2Ratio*barB.getBodyHeight();
			exitPrice2 = inPrice2 - (stopLose-inPrice2) * profitMultiplier;
			action=Action.SELL;
			intrend = barB.getEmaSlope()<0 && barB.getEmaSlope() !=-1000;
		}
		
		this.trend=barB.getEmaSlope();
		stopLose=Util.roundTo2D(stopLose);
		exitPrice=Util.roundTo2D(exitPrice);
		inPrice=Util.roundTo2D(inPrice);
		exitPrice2=Util.roundTo2D(exitPrice2);
		inPrice2=Util.roundTo2D(inPrice2);
		
		time=barB.getStartTime();
		// calculate shares based on max cash allowed
		int shares = (int) (Util.getInt("maxAmoutPerTrade")/inPrice);
		
		//limit based on available funds
//		double availableFunds = Util.getDouble("maxCashFlow") * 0.95 - 100000;
//		shares = (int) (availableFunds < (shares * inPrice)? availableFunds/inPrice:shares);
		
		// calculate risk/profite margin
		int risk = (int) Math.abs(maxRisk/(inPrice - stopLose));
		shares = shares > risk ? risk:shares; 
		
		//round to hundredth
		shares= (int) Math.round((double)shares/100)*100;
		
		// check for investment to profit ratio
		double expectedProfit = Math.abs((inPrice - exitPrice)*shares);
		double investment = inPrice*shares;
		double InvProR = expectedProfit/investment*100;
		double commusion= shares/100;
		this.shares=shares;
		this.fundRequired = shares * inPrice;
		this.riskReward= Util.roundTo2D(expectedProfit);
		this.invProfRatio = Util.roundTo3D(InvProR) ;
		this.isReasonable = shares>=100 && shares<=5000 && InvProR>0.25 && expectedProfit>=150 && expectedProfit >=5*commusion && intrend;
				
	}
	
	public String toString(){
		String res = isReasonable?"Reasonable to Trade": "Not reasonable to trade";
		return "Shares: " + this.shares +  ", fundRequired($):" + this.fundRequired + ", riskReward($) : " + this.riskReward  + ", InvProR(%): " + this.invProfRatio +  ", " + res;
	}

	public int getShares() {
		return shares;
	}

	public double getRiskReward() {
		return riskReward;
	}


	public double getInvProfRatio() {
		return invProfRatio;
	}

	public boolean isReasonable() {
		return isReasonable;
	}

	public double getFundRequired() {
		return fundRequired;
	}

	public double getExitPrice() {
		return exitPrice;
	}

	public double getInPrice() {
		return inPrice;
	}

	public double getStopLose() {
		return stopLose;
	}

	public double getExitPrice2() {
		return exitPrice2;
	}

	public double getInPrice2() {
		return inPrice2;
	}

	public Action getAction() {
		return action;
	}


	public void setShares(int shares) {
		this.shares = shares;
	}


	public void setExitPrice(double exitPrice) {
		this.exitPrice = exitPrice;
	}


	public void setInPrice(double inPrice) {
		this.inPrice = inPrice;
	}
	
	
	
	public double getTrend() {
		return trend;
	}


	public long getTime() {
		return time;
	}


	public Shares switchTo2(Shares shares){
		Shares shares2= new Shares(shares);
		shares2.setExitPrice(shares.getExitPrice2());
		shares2.setInPrice(shares.getInPrice2());
		return shares2;
	}


	@SuppressWarnings("unchecked")
	public String toReport(String ticker) {
		JSONObject jo = new JSONObject();
		jo.put("Time", Util.getDateTime(time));
		jo.put("Ticker", ticker);
		jo.put("Action", action.name());
		jo.put("InPrice", this.inPrice);
		jo.put("ExitPrice", this.exitPrice);
		jo.put("stopLose", this.stopLose);
		jo.put("shares", this.shares);
		jo.put("IRRatio", this.invProfRatio);
		jo.put("Trend", this.trend);
		jo.put("RiskReward", this.riskReward);
		jo.put("Investment", this.fundRequired);
		jo.put("Strategy", this.strategy);
		jo.put("Reasonable", this.isReasonable);		
		return jo.toJSONString();
	}	

}

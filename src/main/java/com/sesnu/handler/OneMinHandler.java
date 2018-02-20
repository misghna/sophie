package com.sesnu.handler;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.controller.Bar;
import com.sesnu.fireball.model.FBBar;
import com.sesnu.fireball.service.TickerProcessor;
import com.sesnu.fireball.service.Util;
import com.ib.controller.ApiController.IHistoricalDataHandler;

public class OneMinHandler implements IHistoricalDataHandler {

	private static final Logger liveBarLog = LoggerFactory.getLogger("BarLiveLog");
	
	
	private TickerProcessor pr;
	private Bar barA;
	private String ticker;
	private Util util;
	
	public OneMinHandler(TickerProcessor pr,String ticker) {
		this.pr = pr;
		this.ticker=ticker;
		this.util=new Util();
	}

	@Override
	public void historicalData(Bar bar) {

		if(barA!=null && bar.time()!=barA.time()){
			FBBar fbBar =new FBBar(barA.time()*1000,0l,barA.low(),barA.high(),0,
					barA.volume()*100,barA.open(),barA.close(),ticker,0);			
			pr.addHistBarList(fbBar);
			if(util.isDevMode()){
//				pr.updateLive(fbBar);
			}
		}else{
			FBBar fbBar =new FBBar(bar.time()*1000,0l,bar.low(),bar.high(),0,
					bar.volume(),bar.open(),bar.close(),ticker,0);	
			pr.updateLive(fbBar);
//			liveBarLog.info("{}, {}", ticker,bar.toString());
		}		
		barA = bar;
	}

	@Override
	public void historicalDataEnd() {

	}


	
}

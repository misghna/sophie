package com.sesnu.handler;

import java.util.ArrayList;
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

public class OneMinHandler2 implements IHistoricalDataHandler {

	private static final Logger liveBarLog = LoggerFactory.getLogger("BarLiveLog");
	
	
	private Bar barA;
	private String ticker;
	private List<FBBar> list;
	private boolean done;
	
	public OneMinHandler2(String ticker) {
		this.ticker=ticker;
		list = new ArrayList<FBBar>();
	}

	@Override
	public void historicalData(Bar bar) {

		if(barA!=null && bar.time()!=barA.time()){
			FBBar fbBar =new FBBar(barA.time()*1000,0l,barA.low(),barA.high(),0,
					barA.volume()*100,barA.open(),barA.close(),ticker,0);			
			list.add(fbBar);
		}	
		barA = bar;
	}

	@Override
	public void historicalDataEnd() {
		done=true;
	}


	public boolean isDone(){
		return done;
	}
	
	public List<FBBar> getList(){
		return list;
	}
	
}

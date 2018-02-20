package com.sesnu.fireball.service;

import java.util.List;
import java.util.Map;

import com.sesnu.fireball.model.FBBar;
import com.sesnu.fireball.model.SimilarTrade;

public class Test2 {

	public static void main(String[] args) throws InterruptedException {
		

//		min gap 0.3%
		FBBar barGapUp = new FBBar(System.currentTimeMillis(),System.currentTimeMillis()+60,169.31,169.86,0,200000,169.80,169.52,"AAPL",0);
		FBBar barGapUp2 = new FBBar(System.currentTimeMillis(),System.currentTimeMillis()+60,156.55,157.20,0,200000,156.68,156.94,"AAPL",0);

		FBBar barGapDown = new FBBar(System.currentTimeMillis(),System.currentTimeMillis()+60,164.73,165.52,0,200000,165.44,165.94,"AAPL",0);
		
		FBBar barGapDownINTC = new FBBar(System.currentTimeMillis(),System.currentTimeMillis()+60,46.1,46.96,0,200000,46.2,46.62,"INTC",0);
		
		FBBar barGapDownNvda = new FBBar(System.currentTimeMillis(),System.currentTimeMillis()+60,225.25,226.77,0,200000,226.56,225.26,"NVDA",0);
		
		FBBar barGapDownNvdaDan = new FBBar(System.currentTimeMillis(),System.currentTimeMillis()+60,225.28,226.63,0,200000,226.63,225.35,"NVDA",0);

		
//		HistoricalProcessor hist = new HistoricalProcessor(null,-0.64,null);
		
//		new Thread(hist).start();
//		
//		while(hist.getStatus().isEmpty()){
//			Thread.sleep(1000);
//		}
		
//		System.out.println(hist.getHistResponse()==null?hist.getStatus():hist.getHistResponse().toString());
		
		FBBar barGapDownNvdafeb5 = new FBBar(System.currentTimeMillis(),System.currentTimeMillis()+60,235.62,237.14,0,200000,237.14,236.33,"NVDA",0);

		
		HistoricalProcessor hist2 = new HistoricalProcessor(barGapDownNvdafeb5,-1.41,null);
		
		new Thread(hist2).start();
		
		while(hist2.getStatus().isEmpty()){
			Thread.sleep(1000);
		}
		
//		List<FBBar> imported = hist2.fetchData();
//		System.out.println("total " + imported.size());
//		
//		Map<String,SimilarTrade> matchedFiles = hist2.search(imported, 1.5);
//		System.out.println("matched " + matchedFiles.size());
	}

}

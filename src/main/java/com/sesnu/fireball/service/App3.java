package com.sesnu.fireball.service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.Contract;
import com.ib.client.ScannerSubscription;
import com.ib.client.Types.BarSize;
import com.ib.client.Types.DurationUnit;
import com.ib.client.Types.WhatToShow;
import com.ib.controller.ApiController;
import com.ib.controller.ScanCode;
import com.sesnu.fireball.model.FBBar;
import com.sesnu.fireball.model.SimilarTrade;
import com.sesnu.handler.AccountHandler;
import com.sesnu.handler.ConnectionHandler;

import com.sesnu.handler.HistoricalHandlerScanner;
import com.sesnu.handler.LiveOrdersHandler;
import com.sesnu.handler.LoggerInHandler;
import com.sesnu.handler.LoggerOutHandler;
import com.sesnu.handler.OneMinHandler2;
import com.sesnu.handler.PositionHandler;

import com.sesnu.handler.ScannerHistHandler;
import com.sesnu.handler.TaskHandler;

public class App3 {

	private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	private static final int PORT_NO = 4002;
	private static final int CONNECTION_ID = 3;

	
	public static void main(String[] args) throws InterruptedException, ClientProtocolException, IOException {
		mainL.info("Initializing ...");
		

		ConnectionHandler conHandler = new ConnectionHandler(null);
		int connectionId=CONNECTION_ID;
		ApiController api = new ApiController(conHandler,new LoggerInHandler(),new LoggerOutHandler());
		api.connect(Util.getString("gatewayIp"), Util.getInt("gatewayPort"), connectionId, null);
		
		while(!conHandler.isConnected() && !conHandler.isIbControllerOff()){
			mainL.info("waiting for connection to establish ...");
			Thread.sleep(5000);
			if(conHandler.msgCode()==507){
				System.out.println("changing connection id");
				api.disconnect();
				api.connect(Util.getString("gatewayIp"), Util.getInt("gatewayPort"), connectionId+1, null);
			}
		}
		
		if(conHandler.isIbControllerOff()){
			mainL.error("IB Controller is OFF, please turn on TWS and try again!");
			Util.sendMail("Fireball",Util.getString("notificationEmail") ,"Terminated @ " + Util.getDoubleTime());
			System.exit(0);
		}
		
		mainL.info("API Connection established on Port No {} and connection Id {}",PORT_NO,CONNECTION_ID);

		
		OneMinHandler2 histHand = new OneMinHandler2("NVDA");
		api.reqHistoricalData(getContract("NVDA",null), "",  2, DurationUnit.DAY,BarSize._1_min, WhatToShow.TRADES, true, false,histHand);
		
		while(!histHand.isDone()){
			Thread.sleep(1000);
		}
		
		api.cancelHistoricalData(histHand);
		api.disconnect();
		
		System.out.println(histHand.getList().size());
		List<FBBar> list = histHand.getList();
		HistoricalProcessor hist2 = null;
		List<FBBar> newList = new ArrayList<FBBar>();
		for (int i=1; i<list.size(); i++) {
			FBBar barA = list.get(i-1);
			FBBar barB = list.get(i);
			newList.add(barB);
			if(Util.getDate(barA.getStartTime())!=Util.getDate(barB.getStartTime())){
				
				double gapPerc = (barB.open()-barA.close())/barA.close()*100;
				System.out.println("gap " + gapPerc + " date " + Util.getDateStr(barB.getStartTime()));
				if(gapPerc>0){
					Calendar calendar = Calendar.getInstance();
			        calendar.setTimeInMillis(barB.getStartTime());
			        String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
					System.out.println("activ date" + Util.getDateStr(barB.getStartTime()) + " " + dayOfWeek + " gap:" + gapPerc);
					hist2 = new HistoricalProcessor(newList,gapPerc,null);				
					new Thread(hist2).start();

					break;
				}
			}
		}
		
		while(hist2!=null && hist2.getStatus().isEmpty()){
			Thread.sleep(1000);
		}
		
		if(hist2!=null){
			Map<String, SimilarTrade> matched = hist2.getHistResponse().getMatchedTrades();
			Map<String, Double> rmseMap = new HashMap<String, Double>();
			for (Map.Entry<String, SimilarTrade> entry : matched.entrySet()) {
				Calendar calendar = Calendar.getInstance();
		        calendar.setTimeInMillis(entry.getValue().getBarList().get(0).getStartTime());
		        String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
				rmseMap.put(entry.getKey()+"_" + dayOfWeek + "_" + Util.getDateStr(entry.getValue().getBarList().get(0).getStartTime()) ,entry.getValue().getRmse());
				
			}
			rmseMap = Util.sortByValue(rmseMap);
			for (Map.Entry<String, Double> entry : rmseMap.entrySet()) {
				if(entry.getValue()<=0.017){
					System.out.println(entry.getKey());
				}
			}
			System.out.println(rmseMap);
			List<FBBar> prevDay = matched.get("Shoot_90").getPrevDayBarList();
			for (FBBar fbBar : prevDay) {
				Util.writeToFile(fbBar.toCSV(), "NVDA_90_prev", false);
			}
			
			List<FBBar> dayList = matched.get("Shoot_90").getBarList();
			for (FBBar fbBar : dayList) {
				Util.writeToFile(fbBar.toCSV(), "NVDA_90_today", false);
			}
		}

		System.out.println("done");
		
		
		
//		ScannerHistHandler byVolume = new ScannerHistHandler();				
//		api.reqScannerSubscription(getSubDetail(ScanCode.HIGH_OPEN_GAP.name(),100), byVolume); //MOST_ACTIVE
//		
//		while(byVolume.getScannResult().size()<1){
//			Thread.sleep(1000);
//		}
//		
//		System.out.println(byVolume.getScannResult());


	}

	static String [] tickers = new String[]{"A","AAL","AAP","AAPL","ABBV","ABC","ABT","ACN","ADBE","ADI","ADM","ADP","ADS","ADSK","AEE","AEP","AES","AET","AFL","AGN","AIG","AIV","AIZ","AJG","AKAM","ALB","ALGN","ALK","ALL","ALLE","ALXN","AMAT","AMD","AME","AMG","AMGN","AMP","AMT","AMZN","ANDV","ANSS","ANTM","AON","AOS","APA","APC","APD","APH","APTV","ARE","ARNC","ATVI","AVB","AVGO","AVY","AWK","AXP","AYI","AZO","BA","BAC","BAX","BBT","BBY","BDX","BEN","BF.B","BHF","BHGE","BIIB","BK","BLK","BLL","BMY","BRK.B","BSX","BWA","BXP","C","CA","CAG","CAH","CAT","CB","CBG","CBOE","CBS","CCI","CCL","CDNS","CELG","CERN","CF","CFG","CHD","CHK","CHRW","CHTR","CI","CINF","CL","CLX","CMA","CMCSA","CME","CMG","CMI","CMS","CNC","CNP","COF","COG","COL","COO","COP","COST","COTY","CPB","CRM","CSCO","CSRA","CSX","CTAS","CTL","CTSH","CTXS","CVS","CVX","CXO","D","DAL","DE","DFS","DG","DGX","DHI","DHR","DIS","DISCA","DISCK","DISH","DLR","DLTR","DOV","DPS","DRE","DRI","DTE","DUK","DVA","DVN","DWDP","DXC","EA","EBAY","ECL","ED","EFX","EIX","EL","EMN","EMR","EOG","EQIX","EQR","EQT","ES","ESRX","ESS","ETFC","ETN","ETR","EVHC","EW","EXC","EXPD","EXPE","EXR","F","FAST","FB","FBHS","FCX","FDX","FE","FFIV","FIS","FISV","FITB","FL","FLIR","FLR","FLS","FMC","FOX","FOXA","FRT","FTI","FTV","GD","GE","GGP","GILD","GIS","GLW","GM","GOOG","GOOGL","GPC","GPN","GPS","GRMN","GS","GT","GWW","HAL","HAS","HBAN","HBI","HCA","HCN","HCP","HD","HES","HIG","HII","HLT","HOG","HOLX","HON","HP","HPE","HPQ","HRB","HRL","HRS","HSIC","HST","HSY","HUM","IBM","ICE","IDXX","IFF","ILMN","INCY","INFO","INTC","INTU","IP","IPG","IQV","IR","IRM","ISRG","IT","ITW","IVZ","JBHT","JCI","JEC","JNJ","JNPR","JPM","JWN","K","KEY","KHC","KIM","KLAC","KMB","KMI","KMX","KO","KORS","KR","KSS","KSU","L","LB","LEG","LEN","LH","LKQ","LLL","LLY","LMT","LNC","LNT","LOW","LRCX","LUK","LUV","LYB","M","MA","MAA","MAC","MAR","MAS","MAT","MCD","MCHP","MCK","MCO","MDLZ","MDT","MET","MGM","MHK","MKC","MLM","MMC","MMM","MNST","MO","MON","MOS","MPC","MRK","MRO","MS","MSFT","MSI","MTB","MTD","MU","MYL","NAVI","NBL","NCLH","NDAQ","NEE","NEM","NFLX","NFX","NI","NKE","NLSN","NOC","NOV","NRG","NSC","NTAP","NTRS","NUE","NVDA","NWL","NWS","NWSA","O","OKE","OMC","ORCL","ORLY","OXY","PAYX","PBCT","PCAR","PCG","PCLN","PDCO","PEG","PEP","PFE","PFG","PG","PGR","PH","PHM","PKG","PKI","PLD","PM","PNC","PNR","PNW","PPG","PPL","PRGO","PRU","PSA","PSX","PVH","PWR","PX","PXD","PYPL","QCOM","QRVO","RCL","RE","REG","REGN","RF","RHI","RHT","RJF","RL","RMD","ROK","ROP","ROST","RRC","RSG","RTN","SBAC","SBUX","SCG","SCHW","SEE","SHW","SIG","SJM","SLB","SLG","SNA","SNI","SNPS","SO","SPG","SPGI","SRCL","SRE","STI","STT","STX","STZ","SWK","SWKS","SYF","SYK","SYMC","SYY","T","TAP","TDG","TEL","TGT","TIF","TJX","TMK","TMO","TPR","TRIP","TROW","TRV","TSCO","TSN","TSS","TWX","TXN","TXT","UA","UAA","UAL","UDR","UHS","ULTA","UNH","UNM","UNP","UPS","URI","USB","UTX","V","VAR","VFC","VIAB","VLO","VMC","VNO","VRSK","VRSN","VRTX","VTR","VZ","WAT","WBA","WDC","WEC","WFC","WHR","WLTW","WM","WMB","WMT","WRK","WU","WY","WYN","WYNN","XEC","XEL","XL","XLNX","XOM","XRAY","XRX","XYL","YUM","ZBH","ZION","ZTS"};

	
//	private static void getToday(ApiController api) throws InterruptedException{
//
//			long startTime = System.currentTimeMillis();
//
//				String ticker = "NVDA";
//				HistoricalHandlerFilter histHand = new HistoricalHandlerFilter(ticker,scanlist,api);
//				api.reqHistoricalData(getContract(ticker,null), "",  1, DurationUnit.DAY,BarSize._1_min, WhatToShow.TRADES, true, false,histHand);
//			}
//			while(scanlist.size()!=(i*50+50) && System.currentTimeMillis()-startTime<4000){
//				Thread.sleep(1000);
//				System.out.println("sofar done " + scanlist.size());
//			}
//		}
//
//		api.disconnect();
//		analyseData(scanlist);
//	}
	
	

	private static ScannerSubscription getSubDetail(String scanCode,int rows){
		ScannerSubscription s = new ScannerSubscription();
		s.instrument("STK");
		s.locationCode("STK.US.MAJOR");
		s.scanCode(scanCode);
//		s.scanCode(ScanCode.HOT_BY_VOLUME.name());
//		s.scanCode(ScanCode.volu);
		s.numberOfRows(50);
		s.abovePrice(10);
		s.belowPrice(200);
//		s.scanCode(ScanCode.HIGH_OPEN_GAP.name());
		s.aboveVolume(100000);
		s.marketCapAbove(1000000000);
		return s;
	}
	
	private static Contract getContract(String ticker,String ex){
		   Contract contract = new Contract();
	       contract.symbol(ticker); 
	       contract.secType("STK");
	       contract.currency("USD");	       
	       if(ex==null){
	    	   contract.exchange("SMART"); 
	       }else{
	    	   contract.exchange(ex); 
	       }
	       contract.primaryExch("ISLAND");
	       return contract;
	}


	
}

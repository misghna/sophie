package com.sesnu.fireball.service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.ib.client.ScannerSubscription;
import com.ib.controller.ApiController;
import com.ib.controller.ScanCode;
import com.sesnu.fireball.dao.TickerDao;
import com.sesnu.fireball.model.Ticker;
import com.sesnu.handler.AccountHandler;
import com.sesnu.handler.ConnectionHandler;
import com.sesnu.handler.LiveOrdersHandler;
import com.sesnu.handler.PositionHandler;
import com.sesnu.handler.ScannerHandler;
import com.sesnu.handler.TaskHandler;


@Configuration
@EnableScheduling
public class CoreService {
		private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	 
	    @Autowired TickerDao tickerDao;
	    
	    @Autowired DaoService daoService;
	    
	    @Autowired VolumeCalculator volumeCalculator;
	    		
	    private List<Ticker> tickerList;

	    @Autowired Common common;
	    
	    private ScannerHandler gapScannerHandler;
	    private boolean scannerCanceled;
	    private ApiController api;
	    private ConnectionHandler conHandler;
	    private AccountHandler acctHandler;
	    private PositionHandler posHand;
	    private LiveOrdersHandler liveOrdersHand;
	    private TaskHandler taskHandler;
	    private Thread gwControllerThread;
	    private GwController gwController;
	    private boolean gapUpScannerStarted;
	    private boolean gapDownScannerStarted;
	    private boolean liveFeedStarted;
	    private GapScanner gapScanner;
	    
		@SuppressWarnings("unused")
		@Scheduled(fixedRate=60000)
		public void init(){
			
			try{
				double time = Util.getDoubleTime();
				//Update volume percentage multiplier 
				volumeCalculator.updateVolumePercent();
//				mainL.info("Core still running @ {}",time);
				
				if(Util.getBoolean("enableIBController")){
					if(gwController!=null && gwControllerThread!=null && gwController.getRunTime()>30000){
						mainL.info("terminating GWController thread forcibly @ {}", time);
						gwControllerThread.stop();
					}
				}
				
				// instantiate handlers
				if(conHandler==null){
					conHandler = new ConnectionHandler(common);
					acctHandler = new AccountHandler(daoService,common);
					posHand= new PositionHandler(common);
				}
				
				// monitor IB connection
				if((time>=9.25 || Util.isDevMode()) && !conHandler.isConnected()){
					
					if(Util.getBoolean("enableIBController")){
						gwController=new GwController();
						gwControllerThread = new Thread(gwController);
						gwControllerThread.start();
					}

					common.sendMessage("BrokerGW","Offline");
					
					ConnectionManager cm = new ConnectionManager(conHandler);
					if(api!=null)api.disconnect();
					api=cm.connect();
					if(conHandler.isConnected()){
						common.sendMessage("BrokerGW","Online");
					}			
				}
				
				if(conHandler.isConnected() && acctHandler.getNetLiquid().isEmpty()){
					mainL.info("Starting Account info feed...");					
					api.reqAccountUpdates(true,Util.getAcctNo(),acctHandler);
					
					mainL.info("Starting Position info feed...");
					api.cancelPositions(posHand);									
					api.reqPositions(posHand);
					
					liveOrdersHand = new LiveOrdersHandler(daoService);
					if(!Util.isDevMode())api.reqLiveOrders(liveOrdersHand);
				}
				
				if(conHandler.isConnected() && !liveFeedStarted){
					startAllTickers();
					liveFeedStarted=true;
				}
				
				
				//send connection status to UI
				if(conHandler!=null && conHandler.isConnected()){				
					common.removeOldTickers();
				}else{
					common.sendMessage("BrokerGW","Offline");
				}
				
				
//				if(conHandler.isConnected() && (Util.isDevMode() || time >9.25) && !gapUpScannerStarted){
//					ExecutorService ex = Executors.newSingleThreadExecutor();
//					gapScanner = new GapScanner(api,common,"Up");
//					ex.execute(gapScanner);
//					ex.awaitTermination(30, TimeUnit.SECONDS);
//					gapUpScannerStarted=true;
//					
//				}else if(gapUpScannerStarted && !gapDownScannerStarted && gapScanner!=null && gapScanner.isGapUpDone()){
//					ExecutorService ex = Executors.newSingleThreadExecutor();
//					gapScanner = new GapScanner(api,common,"Down");
//					ex.execute(gapScanner);
//					ex.awaitTermination(30, TimeUnit.SECONDS);
//					gapDownScannerStarted=true;
//					
//				}else if(gapDownScannerStarted && gapScanner.isGapDownDone() && !liveFeedStarted){
//					startAllTickers();
//					liveFeedStarted=true;
//				}
				
				if(!Util.isDevMode() && time<8  && api!=null && conHandler!=null && conHandler.isConnected()){
					mainL.info("Terminating GW Controller");
					api.disconnect();
					GwController.stop();
				}
				
				if(gapScannerHandler!=null && gapScannerHandler.getScannResult().size()>48 && !scannerCanceled){
					scannerCanceled=true;
					api.cancelScannerSubscription(gapScannerHandler);
				}
				
			}catch(Exception e){
				mainL.error("error in core servcie",e);
			}

		}
		
		// for Feb 15
//		List<String> tickerList2 = Arrays.asList(new String[]{"FCX","ALXN","BIIB","MRO","CHK","HBI","AGN","FLIR","CTAS","HPE","REGN","PGR","HPQ","BHF","TAP","BLK","DVA","DLTR","PYPL","CFG","JEC","DXC","IVZ","AYI","INTU","TXT","PWR","CDNS","WY","INTC","GPN","TDG","TXN","MCHP","VMC","OMC","HII","CB","DLR","NOC","ADS","SO","MCD","EIX"});
		//for feb 14
//		List<String> tickerList2 = Arrays.asList(new String[]{"UA","MAT","TRIP","UAA","FMC","CHK","BHF","KMX","HCP","AGN","SCG","GPS","LB","NWSA","KSS","RF","GGP","RRC","NWS","UAL","RHT","TGT","HCN","ALGN","FE","DLTR","VRTX","CMG","EXR","PCG","VNO","LRCX","PLD","AAL","WEC","DE","SLG","DRE","AEP","O","AMP","JEC","FRT","AMGN","MET","XLNX","AMAT","LNT","RL","BXP"});

		
		List<String> tickerList2 = Arrays.asList(new String[]{"NVDA"});
		private void startAllTickers() throws InterruptedException{
			taskHandler = new TaskHandler(api,daoService,acctHandler,posHand,liveOrdersHand,common);
			if(tickerList==null){
				int counter=0;
				tickerList = tickerDao.findAll();
//				for (int i=0;i< tickerList.size()-4 ; i++) {
//					counter++;
//					taskHandler.addTicker(tickerList.get(i).getTicker(),false);
//				}


//				Thread.sleep(3000);
				for (String ticker : tickerList2) {
					taskHandler.addTicker(ticker,true);
					counter++;
				}
				mainL.info("total requested " +  counter);
//				mainL.info("Scanning gapdown ...");
//				gapScannerHandler = new ScannerHandler(taskHandler);				
//				api.reqScannerSubscription(getSubDetail(ScanCode.LOW_OPEN_GAP.name(),50), gapScannerHandler); //MOST_ACTIVE
				mainL.info("requested livefeed for {} tickers",counter);
			}
			
		}
		
		
		private static ScannerSubscription getSubDetail(String scanCode,int rows){
			ScannerSubscription s = new ScannerSubscription();
			s.instrument("STK");
			s.locationCode("STK.US.MAJOR");
			s.scanCode(scanCode);
			s.numberOfRows(rows);
			s.abovePrice(5);
			s.belowPrice(200);
			s.aboveVolume(100000);
			s.marketCapAbove(1000000000);
			return s;
		}
		

}

package com.sesnu.fireball.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController;
import com.sesnu.handler.ConnectionHandler;
import com.sesnu.handler.LoggerInHandler;
import com.sesnu.handler.LoggerOutHandler;


public class ConnectionManager {
	
	private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	private static int DEFAULT_CONNECTION_ID=2;
	private int maxTrials=9;
	private ConnectionHandler conHandler;
	
	public ConnectionManager(ConnectionHandler conHandler){
		this.conHandler=conHandler;
	}
	
	public ApiController connect(){
		
		ApiController api = null;
		try{
			
//			//start Gateway
//			if(Util.getBoolean("enableIBController"))
//				GwController.restart(new GwController());
			
			api = new ApiController(conHandler,new LoggerInHandler(),new LoggerOutHandler());
			api.connect(Util.getString("gatewayIp"), Util.getInt("gatewayPort"), DEFAULT_CONNECTION_ID, null);
			
			int trialCounter=0;
			while(!conHandler.isConnected() && !conHandler.isIbControllerOff() && trialCounter<maxTrials){
				mainL.info("waiting for connection to establish ...");
				Thread.sleep(5000);		
				trialCounter ++;
			
				//retry
				api.disconnect();
				Thread.sleep(1000);	
				api = new ApiController(conHandler,new LoggerInHandler(),new LoggerOutHandler());
				api.connect(Util.getString("gatewayIp"), Util.getInt("gatewayPort"), DEFAULT_CONNECTION_ID, null);
			}
			
			if(conHandler.isIbControllerOff()){
				mainL.error("IB Controller is OFF, please turn on TWS and try again!");
//	 			Util.sendMail("Fireball",Util.getString("notificationEmail") ,"IB Controller is OFF, please turn on TWS!");
			}
		}catch(Exception e){
			mainL.error("Error establishing connection with Ib",e);
		}
		
		return api;
	}

}

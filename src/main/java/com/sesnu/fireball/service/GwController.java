package com.sesnu.fireball.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GwController implements Runnable {

	private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	
    private static String stpCmd2 = "taskkill /F /FI \"WindowTitle eq IBController*\"";
    private static String stpCmd1 =  "taskkill /F /FI \"WindowTitle eq IB Gateway*\"";
 //   private static String startCmd = "C:\\IBController\\IBControllerGatewayStart.bat";
    
    private long startTime;
    
    
	@Override
	public void run() {
		startTime = System.currentTimeMillis();
    	if(SystemUtils.IS_OS_WINDOWS){   	
	    	try {
	    		mainL.info("restarting IB gateway...");
	    		stop();
	    		Thread.sleep(5000);
	    		execute(Util.getString("IbControllerPath"));
	    		mainL.info("IB gateway has restarted successfully...");
			} catch (InterruptedException e) {
				mainL.error("Error starting IB gateway",e);
			}
    	}else{
    		mainL.info("Not windown OS, cant start IB gateway");
    	}
	}
	
	public long getRunTime(){
		return System.currentTimeMillis()-startTime;
	}
    
    public static void stop(){
    	if(SystemUtils.IS_OS_WINDOWS){
	    	execute(stpCmd1);
	    	execute(stpCmd2);
    	}
         
    }

    private static String execute(String cmd){

        StringBuffer sb = new StringBuffer();
        try {           
            Process p = Runtime.getRuntime().exec(cmd);
             InputStream in = p.getInputStream();
             final BufferedReader reader = new BufferedReader(
                     new InputStreamReader(in));
             String line = null;
             while ((line = reader.readLine()) != null) {
            	 sb.append(line);
             }
             reader.close();
             
        } catch (Exception e) {
        	mainL.error("Error executing IB gateway command",e);
        }
        return sb.toString();
    }


}

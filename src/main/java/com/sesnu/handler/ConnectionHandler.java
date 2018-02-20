package com.sesnu.handler;

import java.util.List;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IConnectionHandler;
import com.sesnu.fireball.service.Common;
import com.sesnu.fireball.service.Util;

public class ConnectionHandler implements IConnectionHandler {

	private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	
	private boolean connected;
	private boolean ibControllerOff;
	private Common common;
	private int conMsgCode;
	
	public ConnectionHandler(Common common){
		this.common=common;
	}
	
	@Override
	public void connected() {
		mainL.info("connected with IB GW @ {}", Util.getDate(System.currentTimeMillis()));
		if(common!=null)common.sendMessage("BrokerGW", "Online");
		connected = true;

	}

	@Override
	public void disconnected() {
		mainL.info("disconnected");

	}

	@Override
	public void accountList(List<String> list) {
		// TODO Auto-generated method stub

	}

	@Override
	public void error(Exception e) {

		if (e.getMessage()!=null && !e.getMessage().equals("Socket closed")){
			mainL.error("Error while connecting to Ib",e);
		}else if(e.toString().equals("java.lang.NullPointerException")){
			ibControllerOff = true;
		}
		
	}

	@Override
	public void message(int id, int msgCode, String msg) {
		conMsgCode=msgCode;
		if(msgCode==507 || msg.trim().equals("Not connected"))connected=false;		
		mainL.info("Message from IB controller, code: {} , message: {}",msgCode,msg);
		if(common!=null && msg!=null && msg.indexOf("cause")>-1){
			String errorMsg = msg.split("cause")[1];
			JSONObject jo = new JSONObject();
			jo.put("Time", Util.getDateTime(System.currentTimeMillis()));
			jo.put("msg", errorMsg);
			common.sendMessage("IBErrorMsg", jo.toJSONString());
		}
	}

	@Override
	public void show(String showStr) {
		mainL.info(showStr);

	}

	public boolean isIbControllerOff(){
		return ibControllerOff;
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public int msgCode(){
		return conMsgCode;
	}

	
}

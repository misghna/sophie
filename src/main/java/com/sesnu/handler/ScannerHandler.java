package com.sesnu.handler;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.ContractDetails;
import com.ib.controller.ApiController.IScannerHandler;

public class ScannerHandler implements IScannerHandler {

	private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	
	private TaskHandler taskHandler;
	private List<String> tickersList = new ArrayList<String>();
	private boolean done;
	
	public ScannerHandler(TaskHandler taskHandler){
		this.taskHandler=taskHandler;
		tickersList = new ArrayList<String>();
	}
	
	@Override
	public void scannerParameters(String xml) {

	}

	@Override
	public void scannerData(int rank, ContractDetails contractDetails, String legsStr) {
		if(contractDetails.contract().symbol()!=null && contractDetails.contract().symbol().indexOf(" ")<0 &&
				!tickersList.contains(contractDetails.contract()) 
				&& !contractDetails.contract().symbol().isEmpty()){
			taskHandler.addTicker(contractDetails.contract().symbol(), true);
//			tickersList.add(contractDetails.contract().symbol());
		
		}
	}

	@Override
	public void scannerDataEnd() {
		this.done=true;
	}

	
	public boolean isDone(){
		mainL.info("Gappers scan result " + tickersList);
		return this.done;
	}
	
	public List<String> getScannResult(){
		return this.tickersList;
	}
}

package com.sesnu.fireball.service;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.sesnu.fireball.model.FBBar;

public class CassWriter implements Runnable {

	private Cluster cluster ;
	private Session session;
	
	private BufferedReader br;
	private String ticker;
	public CassWriter(BufferedReader br, String ticker) {
		this.br=br;
		this.ticker=ticker;
		
	}
	
	@Override
	public void run() {
		try {
				int batchCounter=1;
				long volume =0;
				double prevGap=0;
				int dailyCount=0;
				String sCurrentLine;
				List<FBBar> barList = new ArrayList<FBBar>();
				List<FBBar> barListSep = new ArrayList<FBBar>();
				 StringBuffer sb = new StringBuffer();
				 sb.append("BEGIN BATCH ");
				 long prevTime=0;
				while ((sCurrentLine = br.readLine()) != null) { 
					if(sCurrentLine.indexOf("symbol")==-1 && sCurrentLine.indexOf("Date")==-1){
						FBBar bar = new FBBar(sCurrentLine,"historical",ticker);	
//						if(Util.getDateStr(bar.getStartTime()).replaceAll("/", "").equals("12302002"))System.out.println(bar.toCSV());
						if(barList.size()>1 && Util.getDate(barList.get(barList.size()-1).getStartTime())+1== Util.getDate(bar.getStartTime())){
							double changePerc = (barList.get(barList.size()-1).close()-barList.get(0).open())/barList.get(0).open() * 100;	
							String candleT=barList.get(0).open()>barList.get(0).close()?"br":"bl";
							candleT=barList.get(0).open()==barList.get(0).close()?"dj":candleT;
							dailyCount ++;
//							addDaily(ticker, barList.get(barList.size()-1).getStartTime(),prevTime,candleT, changePerc, prevGap, volume);
							
							//
							  String dateStr = Util.getDateStr(barList.get(barList.size()-1).getStartTime()).replaceAll("/", "");
							  String datePrevStr = prevTime==0?"":Util.getDateStr(prevTime).replaceAll("/", "");
						      String  query= " INSERT INTO dailysummary (dtstr,prevdtstr,ticker,first_bar,change,gap_perc,vol) "
							      		+ "VALUES ('" + dateStr + "','" + datePrevStr + "','" + ticker + "','"+ candleT + "'," + changePerc + ","+ prevGap + ","+ volume +");";
						    sb.append(query);
							prevGap= (bar.open()-barList.get(barList.size()-1).open())/barList.get(barList.size()-1).open() * 100;	
							prevTime=barList.get(barList.size()-1).getStartTime();
							volume=0;	
							barList = new ArrayList<FBBar>();
							if(dailyCount>99) {
								sb.append(" APPLY BATCH");
		//						addDaily(sb.toString());
								sb = new StringBuffer();
								sb.append("BEGIN BATCH ");
								dailyCount=0;
								
								
							}
						}
						barList.add(bar);
						volume += bar.getVolume();
						
						// save separate
						barListSep.add(bar);
						if(barListSep.size()>99) {
							addStockData(barListSep);
							barListSep = new ArrayList<FBBar>(); 
							batchCounter ++;
						}
					}
				}
				if(barList.size()>0)addStockData(barListSep);
				sb.append(" APPLY BATCH");
//				addDaily(sb.toString());
				closeConnection();
				System.out.println(ticker + "done!");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	
	   private synchronized void  addStockData(List<FBBar> list) {
		      //Query
		   	 
		   	 StringBuffer sb = new StringBuffer();
		   	sb.append("BEGIN BATCH ");
		   	 for (FBBar bar : list) {
		   		  String dateStr = Util.getDateStr(bar.getStartTime()).replaceAll("/", "");
			      String  query= " INSERT INTO " + bar.getTicker() + " (dtstr,time, open , high ,low ,close ,volume ) "
				      		+ "VALUES ('" + dateStr + "'," + bar.getStartTime() + ","+ bar.open() + ","+ bar.high() + ","+ bar.low() + ","+ bar.close() + "," + bar.getVolume() +");";
			      sb.append(query);
		   	 	}
		   	sb.append(" APPLY BATCH");
		   	

//		      System.out.println(query);
		      //Executing the query
		      getSession().execute(sb.toString());
	   }

	   
	   private synchronized void addDaily(String query) {
		      //Query
		      //Executing the query
		      getSession().execute(query);
	   }
	   
	   private synchronized void addDaily(String ticker,long time,long prevTime,String firstB, double change,double gapPerc,long volume) {
		      //Query

		   		  String dateStr = Util.getDateStr(time).replaceAll("/", "");
		   		  String datePrevStr = Util.getDateStr(time).replaceAll("/", "");
			      String  query= " INSERT INTO dailySummary  (dtstr,prevdtstr,ticker,first_bar,change,gap_perc,vol) "
				      		+ "VALUES ('" + dateStr + "','" + datePrevStr + "','" + ticker + "','"+ firstB + "'," + change + ","+ gapPerc + ","+ volume +");";	   	

		      //Executing the query
		      getSession().execute(query);
	   }
	   
	   private Session getSession() {
		      //Creating Cluster object
		   	if(cluster==null)
		      cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
		   
		      //Creating Session object
		   	if(session==null)
		      session = cluster.connect("stockdata");
		 
		      return session;
	   }
	   
	   private void closeConnection() {
		   if(session!=null)
		      session.close();
		   if(cluster!=null)
		      cluster.close();
	   }
}

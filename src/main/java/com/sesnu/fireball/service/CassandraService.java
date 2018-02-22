package com.sesnu.fireball.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.sesnu.fireball.model.FBBar;
import com.sesnu.fireball.model.IdenticalTrade;

public class CassandraService {

	private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	
	
	private Cluster cluster ;
	private Session session;
	
	//create table daily (dtstr text,ticker text,change double,gap_perc double,vol bigint, PRIMARY KEY (ticker,dtstr));
   public static void main2(String args[]) throws InterruptedException{

	   CassandraService casServ = new CassandraService();
	   casServ.getSession();
	   ExecutorService executor = Executors.newFixedThreadPool(5);
   
	   File folder = new File(Util.getString("historicalPath"));
	   File[] files = folder.listFiles();
	   double couter =0;boolean start=false;boolean exited=false;
	   for (File file : files) {
		   String ticker = file.getName().split(".txt")[0];
		   if(ticker.equals("NVDA")) {}
		   else if(ticker.equals("NWSA")) {
			   start=true;
		   }else if(ticker.equals("Zzz")){
			   break;
		   }else if(start) {
//			   casServ.createTable(ticker);
			   casServ.writeDaily(ticker,executor); 
			   
		   }
		   couter +=1;
		   double progress = Util.roundTo2D((couter*100/files.length));
		   System.out.println("Progress " + progress + "%");
	   }
	  
	   casServ.closeConnection();
	   System.out.println("Read complete!");
	   executor.awaitTermination(1, TimeUnit.HOURS);
	   
	   System.out.println("Write complete!");
   }
   
   public static void main(String args[]) throws InterruptedException{
	   CassandraService casServ = new CassandraService();
	   Map<String,IdenticalTrade> matched = casServ.find("NVDA", 1.5, "br");
	   System.out.println(matched.size());
	   
	   
   }
   
   public Map<String,IdenticalTrade> find(String ticker,double gap, String candleType) {
	   double from = gap - Math.abs(gap) * 0.1;
	   double to = gap + Math.abs(gap) * 0.1 ;
	   
	   String dailyQuery ="select * from dailysummary where ticker in ('" + ticker + "') and gap_perc > " + from +" and gap_perc < " + to +" and first_bar = '" + candleType + "' allow filtering;";
	   ResultSet result = getSession().execute(dailyQuery);
	   List<Row> rows = result.all();
	   Set<String> dateSet = new HashSet<String>();
	   Map<String,IdenticalTrade> matchingTradeMap = new HashMap<String,IdenticalTrade>();
	   Map<String,String> prevCurMap = new HashMap<String,String>();
	   for (Row row : rows) {
		   prevCurMap.put(row.getString("prevdtstr"),row.getString("dtstr"));
		   matchingTradeMap.put(row.getString("dtstr"), new IdenticalTrade(row.getDouble("gap_perc"),row.getDouble("change"),row.getLong("vol")));
		   dateSet.add("'" + row.getString("dtstr") +  "'");
		   dateSet.add("'" + row.getString("prevdtstr")  + "'");
	   }
	   
	   String dates = dateSet.toString().replace("[", "(").replace("]", ")");
	   String tickerQuery ="select * from " + ticker + " where dtstr in " + dates + ";";

	   ResultSet result2 = getSession().execute(tickerQuery);
	   List<Row> rows2 = result2.all();
	   String dtStr="";String curDt="";
	   List<FBBar> list = new ArrayList<FBBar>();
	   for (Row row : rows2) {
		   curDt = row.getString("dtstr");
		   if(dtStr.isEmpty()) {
			   dtStr=curDt;
		   }else if(!dtStr.equals(curDt)) {
			   addToMatched(matchingTradeMap, prevCurMap, list, curDt);
			   dtStr=curDt;
			   list = new ArrayList<FBBar>();
		   }
		   list.add(new FBBar(ticker,row));
	   }
	   addToMatched(matchingTradeMap, prevCurMap, list, curDt);
	   closeConnection();
	   cluster.closeAsync();
	   return matchingTradeMap;
   }

	private void addToMatched(Map<String, IdenticalTrade> matchingTradeMap, Map<String, String> prevCurMap,
			List<FBBar> list, String curtDt) {
		if(prevCurMap.containsKey(curtDt)) {
			   String curDt = prevCurMap.get(curtDt);
			   IdenticalTrade idTrade = matchingTradeMap.get(curDt);
			   idTrade.setPrevDayBarList(list);
			   matchingTradeMap.put(curDt,idTrade);
		   }else if (matchingTradeMap.containsKey(curtDt)){
			   IdenticalTrade idTrade = matchingTradeMap.get(curtDt);
			   idTrade.setBarList(list);
			   idTrade.setStartTime(list.get(0).getStartTime());
			   matchingTradeMap.put(curtDt,idTrade);
		   }
	}
   
   private List<FBBar> writeToCassandra(String ticker) throws InterruptedException{

		List<FBBar> barList = new ArrayList<FBBar>();
		try {
			File file = new File(Util.getString("historicalPath") + ticker + ".txt");
			if(!file.exists()){
				return barList;
			}else {
				mainL.info("fetching {}",ticker);
			}			
			

			BufferedReader br = null;
			if(!file.getName().endsWith(".DS_Store")){
				String sCurrentLine;
				br = new BufferedReader(new FileReader(file));
				int batchCounter=1;
				while ((sCurrentLine = br.readLine()) != null) { 
					if(sCurrentLine.indexOf("symbol")==-1 && sCurrentLine.indexOf("Date")==-1){
						FBBar bar = new FBBar(sCurrentLine,"historical",ticker);
						barList.add(bar);
						if(barList.size()>100) {
							addStockData(barList);
							barList = new ArrayList<FBBar>();
		//					System.out.println("writting batch # " + batchCounter);
							batchCounter ++;
						}
					}
				}
				addStockData(barList);
			}

		}catch(Exception e){
			mainL.error("Error reading historical data",e);
			return barList;
		}

//		mainL.info("fetching {}, returned {}",ticker,barList.size());

		return barList;
	}
   
   private List<FBBar> writeDaily(String ticker,ExecutorService executor){


		try {
			File file = new File(Util.getString("historicalPath") + ticker + ".txt");
			if(!file.exists()){
				return null;
			}else {
				mainL.info("fetching {}",ticker);
			}			
			
			BufferedReader br = null;
			if(!file.getName().endsWith(".DS_Store")){				
				br = new BufferedReader(new FileReader(file));
				executor.execute(new CassWriter(br,ticker));
			}

		}catch(Exception e){
			mainL.error("Error reading historical data",e);
		}


		return null;
	}
   
   private void addStockData(List<FBBar> list) {
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
	   	

//	      System.out.println(query);
	      //Executing the query
	      getSession().execute(sb.toString());
   }
   
   private void addDaily(String ticker,long time,String firstB, double change,double gapPerc,long volume) {
	      //Query

	   		  String dateStr = Util.getDateStr(time).replaceAll("/", "");
		      String  query= " INSERT INTO daily (dtstr,ticker,first_bar,change,gap_perc,vol) "
			      		+ "VALUES ('" + dateStr + "','" + ticker + "','"+ firstB + "'," + change + ","+ gapPerc + ","+ volume +");";	   	

	      //Executing the query
	      getSession().execute(query);
   }
   
   private void createTable(String ticker) {
	      //Query
	      String query = "CREATE TABLE " + ticker + " (dtstr text,time bigint, open double, high double,low double,close double,volume bigint,PRIMARY KEY (dtstr,time)) WITH CLUSTERING ORDER BY (time ASC);";

	      try {
	      //Executing the query
	    	  getSession().execute(query);
	      }catch(Exception e) {
	    	  mainL.info("table creation failed {}", e.getMessage());
	      }
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
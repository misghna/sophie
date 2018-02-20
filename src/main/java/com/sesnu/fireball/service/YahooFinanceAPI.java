package com.sesnu.fireball.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.sesnu.fireball.model.ScTable;


public class YahooFinanceAPI {

	private double floatShares;
	private double avgVolume;
	private double maxDailyChange;
	private double marketCap;
	private double lastPrice;
	
	
	
	public static void main(String [] args){
		try{
			YahooFinanceAPI yf = new YahooFinanceAPI();
			yf.getStat("NVDA");
//			yf.getHistorical("NVDA");
//			System.out.println(yf.maxDailyChange);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public  void analyse(String ticker, ScTable table){
		try{
			getStat(ticker);
//			getHistorical(ticker);
			
			table.setAvgVol(avgVolume);
			table.setFloatShares(floatShares);
			table.setMaxDayChange(maxDailyChange);
			table.setLastPrice(lastPrice);
			table.setMarketCap(marketCap);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	

	private void getStat(String ticker) {
		
		try{
			String result = getData("https://finance.yahoo.com/quote/" + ticker + "/key-statistics?p=" + ticker);
	
			if(!result.isEmpty()){
				floatShares = scrapVal(result,"float");
				avgVolume =  scrapVal(result,"averageDailyVolume3Month");
				marketCap = scrapVal(result,"marketCap");
		//		lastPrice = scrapVal(result,"regularMarketPrice");
			}
		}catch (Exception e){
			System.out.println(ticker + " failed to get data from Yahoo finance!");
		}

	}
	
	private double scrapVal(String result,String name){
		String strVal = result.toString().split(name)[1].split("fmt")[1].split("\"")[2];
		return  strNumberParser(strVal);
	}
	
	
	private String getData(String url) throws ClientProtocolException, IOException{
		StringBuffer result = new StringBuffer();
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);

		// add request header
		HttpResponse response = client.execute(request,getHttpContext());
//		HttpResponse response = client.execute(request);
		
		if(response.getStatusLine().getStatusCode()==200){
			BufferedReader rd = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			Header[] head = response.getAllHeaders();
//			for (Header header : head) {
//				System.out.println(header.getName() + "-" + (header.getValue()));
//			}
		}else{
			System.out.println(response.getStatusLine().getStatusCode());
		}
		return result.toString();
	}
	
	private HttpContext getHttpContext(){
		CookieStore cookieStore = new BasicCookieStore(); 
		BasicClientCookie cookie = new BasicClientCookie("cookie", "B=9c43969d7v7l1&b=3&s=hj");
		cookie.setDomain("finance.yahoo.com");
		cookie.setPath("/");
		cookie.setAttribute(ClientCookie.DOMAIN_ATTR, "true");
		
		cookieStore.addCookie(cookie); 
		HttpContext localContext = new BasicHttpContext();
		localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
		return localContext;
	}
	
	private double strNumberParser(String nmbr){
		if(nmbr.contains("M")){
			return Double.parseDouble(nmbr.replace("M", ""));
		}else if(nmbr.contains("B")){
			return Double.parseDouble(nmbr.replace("B", "")) * 1000;
		}else if(nmbr.contains("k")){
			return Util.roundTo2D(Double.parseDouble(nmbr.replace("k", "")) /1000);
		}
		return 0;
	} 
	
	private void getHistorical(String ticker) {
		WebDriver driver = new HtmlUnitDriver();
		long today = System.currentTimeMillis()/1000;
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -1); // to get previous year add -1
		long lastYear = cal.getTime().getTime();
		lastYear=lastYear/1000;
		driver.get("https://finance.yahoo.com/quote/" + ticker  +"/history?period1=" + lastYear + "&period2=" + today +"&interval=1d&filter=history&frequency=1d");
		String d ="";
		List<WebElement> list=driver.findElements(By.tagName("table"));
	       for(WebElement e : list){
	    	   List<WebElement> trList= e.findElements(By.tagName("tr"));
	    	   for(WebElement row : trList){
	    		   List<WebElement> cells = row.findElements(By.tagName("td")); 
	    		   String rowStr = "";
	    		    for (WebElement cell : cells) { 
	    		    	rowStr = rowStr + cell.getText() + ",";
	    		    }
	    		  
	    		    String[] rowSplits = rowStr.split(",");
	    		    if(rowSplits.length>8){
	    		    	double high = Double.parseDouble(rowSplits[3]);
	    		    	double low = Double.parseDouble(rowSplits[4]);
	    		    	lastPrice=Double.parseDouble(rowSplits[5]);
	    		    	double dayChange = Util.roundTo2D((high-low)/low*100);
	    		    	maxDailyChange = maxDailyChange<dayChange?dayChange:maxDailyChange;
	    		    }else if(rowStr.indexOf("Market Cap")>-1){
	    		    	marketCap=strNumberParser(rowSplits[1]);
	    		    	//52 Week Range,9.70 - 15.65,
	    		    	//Volume,85,458,235,
	    		    	//Avg. Volume,52,567,045,
	    		    	//Market Cap,10.912B,
//	    		    	System.out.println(marketCap);
	    		    }
	    	   }
	    	   
	       }		

	}
	
}

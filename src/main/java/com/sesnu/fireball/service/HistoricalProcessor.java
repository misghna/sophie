package com.sesnu.fireball.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.ib.client.Types.Action;
import com.sesnu.fireball.model.FBBar;
import com.sesnu.fireball.model.HistResponse;
import com.sesnu.fireball.model.SimilarTrade;


public class HistoricalProcessor implements Runnable {

	private static final Logger mainL = LoggerFactory.getLogger("MainLog");

	
	private FBBar xBar;
	private List<FBBar> activeBarList;
	private double gapPerc;
	private HistResponse histResponse;
	private String status="";
	
	private final static double START_GAP_TOLERANCE=0.3;
	private final static double HOUR_ESTIMATOR_THRESHOLD=2.5;
	private final static double STP_BUFFER=0.01;
	private final static double MIN_ACCEPTABLE_PERCENTAGE=62;
	private final static int MIN_MATCHES_GREATER_1=5;
	private final static int MIN_MATCHES_LESS_1=10;
	private Common common;
	
	public HistoricalProcessor(List<FBBar> activeBarList,double gapPerc,Common common){
		this.xBar=activeBarList.get(activeBarList.size()-1);
		this.activeBarList=activeBarList;
		this.gapPerc=gapPerc;
		this.common=common;
	}
	
	public HistoricalProcessor(FBBar xBar,double gapPerc,Common common){
		this.xBar=xBar;
		this.gapPerc=gapPerc;
		this.common=common;
	}
	
	@Override
	public void run() {
		
		long start = System.currentTimeMillis();
		
		List<FBBar> imported = fetchData();
		
		Map<String,SimilarTrade> matchedFiles=null;
		double gapTolerance=START_GAP_TOLERANCE;
		int minMatches=gapPerc<=1?MIN_MATCHES_LESS_1:MIN_MATCHES_GREATER_1;
		List<HistResponse> histResponseList=new ArrayList<HistResponse>();
		double absMax=0;
		for(int i=0;i<10;i++){
			gapTolerance = START_GAP_TOLERANCE - 0.02;
			matchedFiles = search(imported,gapTolerance);
			double curMax=0;
			if(matchedFiles.size()<minMatches)break;
			HistResponse histResponsei= anaylse(matchedFiles,gapTolerance);
			histResponseList.add(histResponsei);
			curMax=Math.max(histResponsei.getTrendPercent(),histResponsei.getTrendPercent());
			absMax = Math.max(absMax, curMax);
			if(curMax > 75)break;
		
		}
		
		for (HistResponse histResponse2 : histResponseList) {
			double curMax=Math.max(histResponse2.getTrendPercent(),histResponse2.getTrendPercent());
			if(curMax==absMax){
				histResponse=histResponse2;
				break;
			}
		}
		
		if(histResponseList.size()==0 && status.isEmpty()){
			this.status="cant make a recom, because too few matched trades";
		}else if(status.isEmpty()){
			this.status="Ready";
		}
		int timeTaken = (int) ((System.currentTimeMillis()-start)/1000);
		mainL.info("{} ~ Trade recomendations : date :{}, Time-Taken(Sec): {} ,gap:{}, matchedTrades: {}, status: {}, - {}",
					xBar.getTicker(),Util.getDateStr(xBar.getStartTime()),timeTaken,gapPerc,matchedFiles.size(),status,histResponse==null?"No Recom":histResponse.toCsv());
		
		if(histResponse!=null && histResponse.getAction()!=null &&  common!=null){
			common.sendMessage("Recommended_Trades" + xBar.getTicker() + "_dd", histResponse.toCsv());
		}
		

	}

	private HistResponse anaylse(Map<String,SimilarTrade> matchedFiles,double garpTolerance){
		List<Double> inlineList = new ArrayList<Double>();
		double inlineMinPerc=0;double inlineMaxPerc=0;
		double reverseMin=0;double reverseMax=0;

		// find min/max
		for(Map.Entry<String,SimilarTrade> entry : matchedFiles.entrySet()){
			if(gapPerc>0){
				if(entry.getKey().indexOf("Shoot")>-1){					
					inlineMinPerc =inlineMinPerc==0||inlineMinPerc>entry.getValue().getlMin()?entry.getValue().getlMin():inlineMinPerc;
					inlineMaxPerc =inlineMaxPerc<entry.getValue().getMaxDayPerc()?entry.getValue().getMaxDayPerc():inlineMaxPerc;
					inlineList.add(entry.getValue().getlMin());
				}else if(entry.getKey().indexOf("reversedDown")>-1){
					reverseMin=reverseMin==0 || reverseMin>entry.getValue().getMinDayPerc()?entry.getValue().getMinDayPerc():reverseMin;
					reverseMax=reverseMax==0 || reverseMax<entry.getValue().getMaxDayPerc()?entry.getValue().getMaxDayPerc():reverseMax;
				}
			
			}else if(gapPerc<0){
				if(entry.getKey().indexOf("Slide")>-1){
					inlineMaxPerc =inlineMaxPerc==0 || inlineMaxPerc<entry.getValue().getlMax()?entry.getValue().getlMax():inlineMaxPerc;
					inlineMinPerc =inlineMinPerc==0||inlineMinPerc>entry.getValue().getMinDayPerc()?entry.getValue().getMinDayPerc():inlineMinPerc;
					inlineList.add(entry.getValue().getlMax());
				}else if(entry.getKey().indexOf("reversedUp")>-1){
					reverseMax=reverseMax<entry.getValue().getMaxDayPerc()?entry.getValue().getMaxDayPerc():reverseMax;
					reverseMin=reverseMin==0||reverseMin>entry.getValue().getMaxDayPerc()?entry.getValue().getMaxDayPerc():reverseMin;
				}
			}			
		}
		
		double stp=0;double entry75=0;double entry50=0;double entry25=0;
		// compose 
		Collections.sort(inlineList);
		if(inlineList.size()==0){
			 mainL.info("Inline not found");
		}else if(gapPerc>0){
			 double perc100 = calcMedian(inlineList,0.5) * 2;
			 inlineMinPerc = perc100>inlineMinPerc?perc100:inlineMinPerc;			
			 stp=parsePercent(inlineMinPerc,xBar.high()) - STP_BUFFER;
			 List<Double> adjustedList  = adjustInline(inlineList,inlineMinPerc);
			 entry75 = parsePercent(calcMedian(adjustedList,0.75),xBar.high());
			 entry50 = parsePercent(calcMedian(adjustedList,0.5),xBar.high());
			 entry25 = parsePercent(calcMedian(adjustedList,0.25),xBar.high());
		}else if(gapPerc<0){
			 double perc100 = calcMedian(inlineList,0.5) * 2;  // check ??
			 inlineMaxPerc = perc100<inlineMaxPerc?perc100:inlineMaxPerc;
			 stp=parsePercent(inlineMaxPerc,xBar.low()) + STP_BUFFER;
			 List<Double> adjustedList  = adjustInline(inlineList,inlineMaxPerc);

			 entry75 = parsePercent(calcMedian(adjustedList,0.75),xBar.low());
			 entry50 = parsePercent(calcMedian(adjustedList,0.5),xBar.low());
			 entry25 = parsePercent(calcMedian(adjustedList,0.25),xBar.low());
		}
		
		
		// calculate probability percent
		int totalLoss=0;int totalInline=0;int laterGained=0;
		List<String> doneList= new ArrayList<String>();
		for(Map.Entry<String,SimilarTrade> entry : matchedFiles.entrySet()){
			    SimilarTrade simTrade = entry.getValue();
				if(gapPerc>0 && entry.getKey().indexOf("unAssigned")>-1){
					if(simTrade.getMinDayPerc()<inlineMinPerc){
						totalLoss ++;
						doneList.add(entry.getKey());
					}else if(simTrade.getMaxDayPerc()>0 && !doneList.contains(entry.getKey())){
						laterGained ++;
					}
				}else if(gapPerc<0 && entry.getKey().indexOf("unAssigned")>-1){
					
					if(simTrade.getMaxDayPerc()>inlineMaxPerc){
						totalLoss ++;
						doneList.add(entry.getKey());
					}else if(simTrade.getMinDayPerc()<0 && !doneList.contains(entry.getKey())){
						laterGained ++;
					}
				}else if(gapPerc>0 && entry.getKey().indexOf("unAssigned")<0 && entry.getValue().getlMin()>=inlineMinPerc){
						totalInline ++;
				}else if(gapPerc>0 && entry.getKey().indexOf("unAssigned")<0 && entry.getValue().getlMin()<inlineMinPerc){
					totalLoss ++;
				}else if(gapPerc<0 && entry.getKey().indexOf("unAssigned")<0 && entry.getValue().getlMax()<=inlineMaxPerc){
					totalInline ++;
				}else if(gapPerc<0 && entry.getKey().indexOf("unAssigned")<0 && entry.getValue().getlMax()>inlineMinPerc){
					totalLoss ++;
				}		
		}
		
		double inTrendPercent = Util.roundTo2D((double)(totalInline+laterGained)/(double)(totalInline+totalLoss+laterGained)*100);
		double reversePercent = Util.roundTo2D((double)(totalLoss)/(double)(totalInline+totalLoss+laterGained)*100);
	
		double reverseTrendCount =0;double inTrendCount =0;
		double revLocalMax=0;double revLocalMin=0;
		
		List<Double> reverseList = new ArrayList<Double>();
		if (inTrendPercent<MIN_ACCEPTABLE_PERCENTAGE){
			// calculate reverse probability
			
			for(Map.Entry<String,SimilarTrade> entry : matchedFiles.entrySet()){
			    SimilarTrade simTrade = entry.getValue();
				if(gapPerc>0 && simTrade.getMinDayPerc()<inlineMinPerc){
					revLocalMax=revLocalMax==0 || revLocalMax<simTrade.getlMax()?simTrade.getlMax():revLocalMax;
					if(simTrade.getlMax()!=0)reverseList.add(simTrade.getlMax());
					reverseTrendCount ++;
				}else if(gapPerc>0 && simTrade.getMinDayPerc()>inlineMinPerc){
					inTrendCount ++;
				}else if(gapPerc<0 && simTrade.getMaxDayPerc()>inlineMaxPerc){
					revLocalMin=revLocalMin==0 || revLocalMin>simTrade.getlMin()?simTrade.getlMin():revLocalMin;
					if(simTrade.getlMin()!=0)reverseList.add(simTrade.getlMin());
					reverseTrendCount ++;
				}else if(gapPerc<0 && simTrade.getMaxDayPerc()<inlineMaxPerc){
					inTrendCount ++;
				}		
			}
		}
		
		if(reverseList.size()>0){
			// calculate reverse parameters
			Collections.sort(reverseList);
			List<Double> adjustedList = adjustSkew(reverseList);
			Collections.sort(adjustedList);
			double revisedReversePercent = adjustedList.size()/(reverseTrendCount+inTrendCount) * 100;		
			
			if(revisedReversePercent > MIN_ACCEPTABLE_PERCENTAGE){
				reversePercent=revisedReversePercent;
				if(gapPerc>0){
					 stp=parsePercent(Collections.max(adjustedList),xBar.high()) + STP_BUFFER; // check sign issue ??
					 entry75 = parsePercent(calcMedian(adjustedList,0.75),xBar.low());
					 entry50 = parsePercent(calcMedian(adjustedList,0.5),xBar.low());
					 entry25 = parsePercent(calcMedian(adjustedList,0.25),xBar.low());
				}else if(gapPerc<0){
					 stp=parsePercent(Collections.min(adjustedList),xBar.high()) - STP_BUFFER; // check sign issue ??
					 entry75 = parsePercent(calcMedian(adjustedList,0.75),xBar.high());
					 entry50 = parsePercent(calcMedian(adjustedList,0.5),xBar.high());
					 entry25 = parsePercent(calcMedian(adjustedList,0.25),xBar.high());
				}
			}
		}
		
		// find action
		Action action = null;
		if(gapPerc>0 && inTrendPercent>70){
			action = Action.BUY;
		}else if(gapPerc>0 && reversePercent>70){
			action = Action.SELL;
		}else if(gapPerc<0 && inTrendPercent>70){
			action = Action.SELL;
		}else if(gapPerc<0 && reversePercent>70){
			action = Action.BUY;
		}
	
		
		return new HistResponse(action,stp,entry25,entry50,entry75,matchedFiles,inTrendPercent,
				reversePercent,garpTolerance,Util.getDate(xBar.getStartTime()));
		
	}
	
	private List<Double> adjustSkew(List<Double> list){
		double median = calcMedian(list,0.5);
		double max = list.get(list.size()-1);
		double distance = median-max;
		List<Double> newList = new ArrayList<Double>();
		for (Double val : list) {
			if(val>=median+distance){
				newList.add(val);
			}
		}
		return newList;
	}
	
	private List<Double> adjustInline(List<Double> list,double threshold){
		List<Double> listNew = new ArrayList<Double>();
		for (Double val : list) {
			if((val>0 && val<=threshold) || (val<0 && val>=threshold)){
				listNew.add(val);
			}
		}
		return listNew;
	}
	
	
	private double calcMedian(List<Double> inlineList, double perc){
		double n = ((double)inlineList.size()-1) * perc;
		double med =0;
		if(n%2==0){
			med = inlineList.get((int)n);
		}else{
			int num1= (int) Math.floor(n);
			int num2= num1+1;
			med = (inlineList.get(num1) + inlineList.get(num2))/2;
		}
		return med;
	}
	
	
	private double parsePercent(double perc,double val){
		return Util.roundTo2D(val + (perc * val/100));
	}
	
	public Map<String,SimilarTrade> search(List<FBBar> list,double gapTolerance){
		Map<String,SimilarTrade> matchedFiles = new HashMap<String,SimilarTrade>();
		if(list.size()<2)return matchedFiles;
		
		double xBodyPercent = xBar.getBodyHeight()/xBar.open()*100;
		
		for (int i=1;i<list.size();i++) {							
			List<FBBar> dayData = new ArrayList<FBBar>();
			List<FBBar> prevDayData = new ArrayList<FBBar>();
			 FBBar barA= list.get(i-1);
			 FBBar barB= list.get(i);
			 if(Util.getDate(barA.getStartTime())!=Util.getDate(barB.getStartTime())){
				 Integer day=Util.getDate(barB.getStartTime());
				 double gap = barB.open()-barA.close();
				 double histGapPerc = Util.roundTo2D(gap/barA.close()*100);
				 double histBodyPercent = barB.getBodyHeight()/barB.open()*100;
				 if((gapPerc>0?(histGapPerc>gapPerc*(1-gapTolerance) && histGapPerc<gapPerc*(1+gapTolerance)):
					(histGapPerc<gapPerc*(1-gapTolerance) && histGapPerc>gapPerc*(gapTolerance+1)))
					&& histBodyPercent > 0.5 * xBodyPercent
					 && (barB.getCandleType().equals(xBar.getCandleType()))){
					 prevDayData = getPrevDayData(list,i-1);
					 dayData = new ArrayList<FBBar>();
					 int barCount=0;String type="unAssigned";
					 double dMin=0;double dMax=0;
					 double lMin=barB.low();double lMax=barB.high();
					 for (int j=i;j<list.size();j++) {
						 barCount ++;
						 FBBar barC= list.get(j);
						 if(day==Util.getDate(barC.getStartTime())){
							 dayData.add(barC);
							 if(barCount<HOUR_ESTIMATOR_THRESHOLD * 60){
								 dMin=dMin==0||dMin>barC.low()?barC.low():dMin;
								 dMax=dMax==0||dMax<barC.high()?barC.high():dMax;
							 }
						 }else{
							 break;
						 }
						 //check if it closes or shoots
						 if(gapPerc>0){
							 if(barCount<=30){								 								 								 
								 if(barC.high()> barB.high() * 1.001){
									 type ="Shoot";
								 }else if(!type.equals("Shoot")){
									 lMin=lMin>barC.low()?barC.low():lMin;
									 lMax=lMax<barC.high()?barC.high():lMax;
								 }
							 }else if(barCount<=30 && type.equals("unAssigned")){
								 type="reversedDown";
							 }

						 }else if(gapPerc<0){
							 if(barCount<=30){								 								 
								 if(barC.low()< barB.low() * 0.999){
									 type ="Slide";
								 }else if(!type.equals("Slide")){
									 lMax=lMax<barC.high()?barC.high():lMax;
									 lMin=lMin>barC.low()?barC.low():lMin;
								 }
							 }else if(barCount<=30 && type.equals("unAssigned")){
								 type="reversedUp";
							 }
						 }
					 }
					 
					 // convert minimums/maximums to percentage
					 double minPercFromOpen=0;double maxPercFromOpen=0;
					 if(gapPerc>0){
						 lMin = (lMin-barB.high())/barB.high()*100;
						 lMax = (lMax-barB.low())/barB.low()*100;
						 minPercFromOpen = (dMin-barB.high())/barB.high()*100;
						 maxPercFromOpen = (dMax-barB.high())/barB.high()*100;
					 }else if(gapPerc<0){
						 lMin = (lMin-barB.high())/barB.high()*100;
						 lMax = (lMax-barB.low())/barB.low()*100;
						 minPercFromOpen = (dMin-barB.low())/barB.low()*100;
						 maxPercFromOpen = (dMax-barB.low())/barB.low()*100;
					 }
					 if(dayData.size()>0){
//						 System.out.println(Util.getDateStr(barB.getStartTime()) + "," +  type + " gap" + histGapPerc + " min: " + Util.roundTo3D(lMin) + " lMax: " + Util.roundTo3D(lMax)
//						 + " dMax: " + Util.roundTo3D(maxPercFromOpen) + " dMin: " + Util.roundTo3D(minPercFromOpen));
						 SimilarTrade similarStock = new SimilarTrade(xBar.getTicker(),histGapPerc,barB.getStartTime(),type,dayData,prevDayData,
								 barB.getEmaSlow(),maxPercFromOpen,minPercFromOpen,lMin,lMax,calcAggRmse(prevDayData));
						 matchedFiles.put(type + "_" + matchedFiles.size(), similarStock);
					 }
				 }

			 }
		}
		return matchedFiles;
	}
	
	private double calcAggRmse(List<FBBar> prevDayList){
		double rmseSum=0;
		List<FBBar> ydayList = getYDayData();
		if(ydayList== null) return -1;
		int yday = Util.getDate(ydayList.get(0).getStartTime());
		int prevDay = Util.getDate(prevDayList.get(0).getStartTime());
		
		rmseSum +=new Rmse().calc(ydayList,prevDayList,yday,prevDay,"CLOSE");

//		rmseSum +=new Rmse().calc(ydayList,prevDayList,yday,prevDay,"HIGH");
//		rmseSum +=new Rmse().calc(ydayList,prevDayList,yday,prevDay,"LOW");
//		rmseSum +=new Rmse().calc(ydayList,prevDayList,yday,prevDay,"VOLUME");
		
		return Util.roundTo3D(rmseSum/3);
		
	}
	
	private List<FBBar>  getPrevDayData(List<FBBar> list, int lastIndx){
		List<FBBar> prevDayList= new ArrayList<FBBar>(); 
		for(int i=lastIndx;i>=0; i--){
			if(Util.getDate(list.get(lastIndx).getStartTime())==Util.getDate(list.get(i).getStartTime())){
				prevDayList.add(list.get(i));
			}else{
				break;
			}			
		}
		prevDayList = Lists.reverse(prevDayList);
		return prevDayList;
	}
	
	private List<FBBar>  getYDayData(){
		if(activeBarList==null)return null;
		List<FBBar> yDayList= new ArrayList<FBBar>(); 
		for(int i=activeBarList.size()-2;i>=0; i--){
			if(Util.getDate(activeBarList.get(activeBarList.size()-2).getStartTime())==Util.getDate(activeBarList.get(i).getStartTime())){
				yDayList.add(activeBarList.get(i));
			}else{
				break;
			}			
		}
		yDayList = Lists.reverse(yDayList);
		return yDayList;
	}
	
	
	public List<FBBar> fetchData(){
		List<FBBar> barList = new ArrayList<FBBar>();
		try {
			File file = new File(Util.getString("historicalPath") + xBar.getTicker() + ".txt");
			if(!file.exists()){
				status="Historical data not found!";
				return barList;
			}else {
				mainL.info("fetching {}",xBar.getTicker());
			}			
			

			BufferedReader br = null;
			Set<Long> added = new HashSet<Long>();
			if(!file.getName().endsWith(".DS_Store")){
				String sCurrentLine;
				br = new BufferedReader(new FileReader(file));
								
				while ((sCurrentLine = br.readLine()) != null ) { 
					if(sCurrentLine.indexOf("symbol")==-1 && sCurrentLine.indexOf("Date")==-1){
						FBBar bar = new FBBar(sCurrentLine,"historical",xBar.getTicker());
//						bar = new Indicators().addIndicators(barList, bar);
						if(!added.contains(bar.getStartTime())){
							barList.add(bar);
							added.add(bar.getStartTime());
						}
					}
				}
			}

		}catch(Exception e){
			status="Historical data not found!";
			mainL.error("Error reading historical data",e);
			return barList;
		}
		if(barList.size()==0){
			status="Historical data not found!";
		}
		

		mainL.info("fetching {}, returned {}",xBar.getTicker(),barList.size());

		return barList;
	}

	public HistResponse getHistResponse() {
		return histResponse;
	}


	
	public String getStatus() {
		return status;
	}
}

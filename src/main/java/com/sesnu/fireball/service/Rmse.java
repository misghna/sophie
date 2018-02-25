package com.sesnu.fireball.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sesnu.fireball.model.FBBar;
import com.sesnu.fireball.model.IdenticalTrade;

public class Rmse {

	private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	
	public Map<String,IdenticalTrade> calc(List<FBBar> ydayList,Map<String,IdenticalTrade> identicalTrades,String type,int resultNo){
		
		Map<String,IdenticalTrade> indenticalTradesList = new HashMap<String,IdenticalTrade>();
		Map<String,Double> rmseList = new HashMap<String,Double>();
		for(Map.Entry<String, IdenticalTrade> entry : identicalTrades.entrySet()) {			
			IdenticalTrade identicalTrade = entry.getValue();
			if(identicalTrade.getPrevDayBarList()!=null) {
				List<Double> timeList = getCommonTimes(ydayList,identicalTrade.getPrevDayBarList());
				List<Double> list1= toList(ydayList,type,timeList);
				List<Double> normList2= normalize(identicalTrade.getPrevDayBarList(),type,timeList,ydayList.get(0));
				double rmse = calcRms(list1,normList2);
				rmseList.put(entry.getKey(), rmse);
				identicalTrade.setRmse(rmse);
				identicalTrades.put(entry.getKey(), identicalTrade);
			}else {
				mainL.info("PrevDay data for {} is missing",entry.getKey());
			}
		}
		rmseList = Util.reverseSortByValue(rmseList);
		// add best 5 only
		for(Map.Entry<String, Double> entry : rmseList.entrySet()) {
			if(indenticalTradesList.size()<resultNo) {
				indenticalTradesList.put(entry.getKey(),identicalTrades.get(entry.getKey()));
			}else {
				break;
			}
		}
		return indenticalTradesList;
	}
	
	public IdenticalTrade calcBest(List<FBBar> ydayList,Map<String,IdenticalTrade> identicalTrades,String type){
		
		Map<String,Double> rmseList = new HashMap<String,Double>();
		for(Map.Entry<String, IdenticalTrade> entry : identicalTrades.entrySet()) {			
			IdenticalTrade identicalTrade = entry.getValue();
			if(identicalTrade.getPrevDayBarList()!=null) {
				List<Double> timeList = getCommonTimes(ydayList,identicalTrade.getPrevDayBarList());
				List<Double> list1= toList(ydayList,type,timeList);
				List<Double> normList2= normalize(identicalTrade.getPrevDayBarList(),type,timeList,ydayList.get(0));
				double rmse = calcRms(list1,normList2);
				rmseList.put(entry.getKey(), rmse);
				identicalTrade.setRmse(rmse);
				identicalTrades.put(entry.getKey(), identicalTrade);
			}else {
				mainL.info("PrevDay data for {} is missing",entry.getKey());
			}
		}
		rmseList = Util.reverseSortByValue(rmseList);
		System.out.println("final short list " + rmseList);
		for(Map.Entry<String, Double> entry : rmseList.entrySet()) {
				return identicalTrades.get(entry.getKey());

		}
		return null;
	}
	
	public double calc(List<FBBar> list1,List<FBBar> list2,String type){
		
		List<Double> timeList = getCommonTimes(list1,list2);
		List<Double> normList1= toList(list1,type,timeList);
		List<Double> normList2= toList(list2,type,timeList);
		return calcRms(normList1,normList2);
	}
	
	private List<Double> getCommonTimes(List<FBBar> list1,List<FBBar> list2){
		List<Double> t1List = new ArrayList<Double>();
		for (int i=0; i< list1.size(); i++) {
				t1List.add(Util.getDoubleTime(list1.get(i).getStartTime()));
		}
		
		List<Double> t2List = new ArrayList<Double>();
		for (int i=0; i< list2.size(); i++) {
				t2List.add(Util.getDoubleTime(list2.get(i).getStartTime()));
		}
		
		t1List.retainAll(t2List);
		
		return t1List;
	}
	
	private List<Double> toList(List<FBBar> list1,String type,List<Double> timeList){
		List<Double> normList= new ArrayList<Double>();
		for (int i=0; i< list1.size(); i++) {
			if(timeList.contains(Util.getDoubleTime(list1.get(i).getStartTime()))){
				if(type.equals("HIGH")){
					normList.add((list1.get(i).high()));
				}else if(type.equals("LOW")){
					normList.add((list1.get(i).low()));
				}else if(type.equals("CLOSE")){
					normList.add((list1.get(i).close()));
				}else if(type.equals("SLOWEMA")){
					normList.add((list1.get(i).getEmaSlow()));
				}else if(type.equals("VOLUME")){
					normList.add((double) (list1.get(i).getVolume()));
				}
			}
		}
		
		
		return normList;
	}
	
	private List<Double> normalize(List<FBBar> list1,String type,List<Double> timeList,FBBar fst){
		List<Double> normList= new ArrayList<Double>();
		
		for (int i=0; i< list1.size(); i++) {
			if(timeList.contains(Util.getDoubleTime(list1.get(i).getStartTime()))){
				if(type.equals("HIGH")){
					double firstOpen = list1.get(0).high();
					normList.add((list1.get(i).high()/firstOpen*fst.high()));
				}else if(type.equals("LOW")){
					double firstOpen = list1.get(0).low();
					normList.add((list1.get(i).low()/firstOpen*fst.low()));
				}else if(type.equals("CLOSE")){
					double firstOpen = list1.get(0).close();
					normList.add((list1.get(i).close()/firstOpen*fst.close()));
				}else if(type.equals("SLOWEMA")){
					double firstOpen = list1.get(0).getEmaSlow();
					normList.add((list1.get(i).getEmaSlow()/firstOpen*fst.getEmaSlow()));
				}else if(type.equals("VOLUME")){
					double firstOpen = list1.get(0).getVolume();
					normList.add((double) (list1.get(i).getVolume()/firstOpen*fst.getVolume()));
				}
			}
		}
			
		return normList;
	}
	
	
	private double calcRms(List<Double> normList1,List<Double> normList2){
		List<Double> l1 = null;List<Double> l2 = null;
		
		if(normList1.size()<=normList2.size()){
			l1=normList1;
			l2=normList2;
		}else{
			l2=normList1;
			l1=normList2;
		}
		double diffSqrSum=0;
		for (int i=0; i<l1.size(); i++) {
			diffSqrSum += (l1.get(i) - l2.get(i)) * (l1.get(i) - l2.get(i));
		}
		
		return Math.sqrt(diffSqrSum/l1.size());
		
	}
	
}

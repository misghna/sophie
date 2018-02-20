package com.sesnu.fireball.service;

import java.util.ArrayList;
import java.util.List;

import com.sesnu.fireball.model.FBBar;

public class Rmse {

	public double calc(List<FBBar> list1,List<FBBar> list2,int date1,int date2,String type){
		
		List<Double> timeList = getCommonTimes(list1,list2,date1,date2);
		List<Double> normList1= toList(list1,type,timeList);
		List<Double> normList2= toList(list2,type,timeList);
		return calcRms(normList1,normList2);
	}
	
	private List<Double> getCommonTimes(List<FBBar> list1,List<FBBar> list2,int date1,int date2){
		List<Double> t1List = new ArrayList<Double>();
		for (int i=0; i< list1.size(); i++) {
			if(Util.getDate(list1.get(i).getStartTime())==date1){
				t1List.add(Util.getDoubleTime(list1.get(i).getStartTime()));
			}
		}
		
		List<Double> t2List = new ArrayList<Double>();
		for (int i=0; i< list2.size(); i++) {
			if(Util.getDate(list2.get(i).getStartTime())==date2){
				t2List.add(Util.getDoubleTime(list2.get(i).getStartTime()));
			}
		}
		
		t1List.retainAll(t2List);
		
		return t1List;
	}
	
	private List<Double> toList(List<FBBar> list1,String type,List<Double> timeList){
		List<Double> normList= new ArrayList<Double>();
		for (int i=0; i< list1.size(); i++) {
			if(timeList.contains(Util.getDoubleTime(list1.get(i).getStartTime()))){
				if(type.equals("HIGH")){
					normList.add((list1.get(i).high()/list1.get(0).high()));
				}else if(type.equals("LOW")){
					normList.add((list1.get(i).low()/list1.get(0).low()));
				}else if(type.equals("CLOSE")){
					normList.add((list1.get(i).close()/list1.get(0).close()));
				}else if(type.equals("SLOWEMA")){
					normList.add((list1.get(i).getEmaSlow()/list1.get(0).getEmaSlow()));
				}else if(type.equals("VOLUME")){
					normList.add((double) (list1.get(i).getVolume()/list1.get(0).getVolume()));
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

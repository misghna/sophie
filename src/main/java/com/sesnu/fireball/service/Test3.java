package com.sesnu.fireball.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.sesnu.fireball.model.IdenticalTrade;

public class Test3 {

	public static void main(String[] args) {
		
		Map<String,Double> nameAge = new HashMap<String,Double>();
		nameAge.put("Msghsde", 5.0);
		nameAge.put("Msghe", 22.0);
		nameAge.put("Tesfa", 12.1);
		nameAge.put("asds", 13.0);
		nameAge.put("Tesasdfa", 31.1);
		nameAge.put("sdas", 52.0);
		nameAge.put("fsd", 2.1);
		nameAge = Util.reverseSortByValue(nameAge);
		
		System.out.println(nameAge);
	}

	
	private static double median(List<Double> inlineList, double percent){
		double median;
		int num = (int)percent * 4;
		if (inlineList.size() % num == 0)
		    median = ((double)inlineList.get(inlineList.size()/num) + (double)inlineList.get(inlineList.size()/num - 1))/num;
		else
		    median = (double) inlineList.get(inlineList.size()/num);
		
		return median;
	}
}

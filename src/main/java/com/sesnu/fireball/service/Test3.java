package com.sesnu.fireball.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Test3 {

	public static void main(String[] args) {
		
		Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
       String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
       System.out.println(dayOfWeek);
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

package com.valtrader.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import com.valtrader.data.Quote;
import com.valtrader.service.Quotes;
import com.valtrader.service.Quotes;

public class Utils {
	public static String formatDate(java.util.Date value, String format) {
		return (new SimpleDateFormat(format)).format(value);

	}

	public static String formatDate(java.sql.Date value, String format) {
		return (new SimpleDateFormat(format)).format(value);

	}
	public static String myFormat(float number) {
		String fmt;
		if(number < 2) fmt = "%.4f";
		else if(number > 500) fmt = "%.0f";
		else fmt = "%.2f";
		return String.format(fmt, number);
	}

	static public String[][] parseArgs(String ind) {
		// ind: ind1 [a1 ...] [+ ind2 [a1 ...]]

		String[][] res = null;
		String[] inds = ind.split("\\+");

		res = new String[inds.length][];

		for (int i = 0; i < inds.length; i++) {
			res[i] = inds[i].split(" ");
		}

		return res;

	}

//	public static java.util.Date sql2util(java.sql.Date date) {
//		return new java.util.Date(date.getYear(), date.getMonth(),
//				date.getDate());
//	}

}

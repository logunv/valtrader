package com.valtrader.ui;

import java.sql.Date;

public class Trade {
	public Trade(String symbol, int csize, int size, Date entryDate, float entry, float stop,
			float target) {
		super();
		this.symbol = symbol;
		this.csize = csize;
		this.size = size;
		this.entry = entry;
		this.stop = stop;
		this.target = target;
		this.entryDate = entryDate;
	}
	String symbol;
	int csize, size;
	float entry, stop, target;
	Date entryDate;
}

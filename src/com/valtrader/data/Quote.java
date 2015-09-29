package com.valtrader.data;

import java.sql.Date;

public class Quote {
	
	public Quote() {}
    public float open, high, low, close;
    public int volume;
    public Date date;

    public Quote(Date dt, float op, float hi, float lo, float cl, int vol) {
    	this.date = dt;
    	this.open = op;
    	this.high = hi;
    	this.low = lo;
    	this.close = cl;
    	this.volume = vol;
    }
    public String toString() {
    	return date + ":" + open + ":" + high + ":" + low + ":" + close;
    }
}

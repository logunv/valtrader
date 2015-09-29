package com.valtrader.utils;

public class Signal {
	public final static int buy = 1;
	public final static int sell = -1;
	public final static int watch = 2;
	public int sig;
	public String notes;
	
	public Signal(int s, String n) {
		this.sig = s;
		this.notes = n;
	}
}

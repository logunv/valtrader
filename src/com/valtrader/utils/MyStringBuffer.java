package com.valtrader.utils;

public class MyStringBuffer {
	StringBuffer sb = new StringBuffer();
	public MyStringBuffer() {
		
	}
	public void append(String str) {
		sb.append(str);
		sb.append(";");
	}
	public String toString() {
		return sb.toString();
	}
}

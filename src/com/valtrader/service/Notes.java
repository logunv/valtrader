/**
 * Handle Notes in the chart.
 * It has position and the text
 */
 
package com.valtrader.service;

import java.sql.Date;

public class Notes {
	public Notes(double x, double y, String text, Date dt) {
		super();
		this.x = x;
		this.y = y;
		this.text = text;
		this.date = dt;
	}

	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
	public String getText() {
		return text;
	}

	public double x, y;
	public String text;
	Date date;
	
	public Date getDate() {
		return date;
	}

}

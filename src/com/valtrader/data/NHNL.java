package com.valtrader.data;

import java.sql.Date;

public class NHNL {
	public Date date;
	public int nh, nl;

	NHNL(Date d, int h, int l) {
		this.date = d;
		this.nh = h;
		this.nl = l;
	}
}

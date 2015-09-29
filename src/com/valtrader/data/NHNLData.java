package com.valtrader.data;
import java.sql.Date;
import java.util.Calendar;
import java.util.HashMap;

public class NHNLData {
//	static NHNL [] nhnldata = null;
	static HashMap<String,NHNL[]> cache = new HashMap<String, NHNL[]>();
	static MyDatabase db = new MyDatabase();

//	static public NHNL [] get(String period) {
//		try {
//		if(nhnldata == null) nhnldata = getData();
//		} catch(Exception e) {
//			System.err.println(e.getMessage());
//		}
//		
//		return toPeriod(nhnldata, period);
//		
//	}
	static public NHNL[] get(String period) {
		try {
			NHNL[] ret = cache.get(period);
			if (ret == null) {
				ret = getData(period);
				cache.put(period, ret);
			}
			return ret;
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return null;
	}

	/**
	 * select extract(year from day) yyy,extract(week from day) ddd,min(day),
	 * sum(newhigh), sum(newlow) from nhnl group by yyy,ddd order by 3 desc;
	 */
	static NHNL [] getData(String period) throws Exception {
		int cnt = 0;
		MyStatement st;
		String sql;
		if(period.equals("d")) {
			sql = "select count(*) from nhnl";
			st = db.execute(sql);
			if(!st.next()) return null;
			cnt = st.getInt(1);
			st.close();
			sql = "select day day1,newhigh,newlow from nhnl order by day desc";
		} else { // must be weekly
			String datasql = "select extract(year from day) yyy,extract(week from day) ddd,min(day) day1,"
					+ "sum(newhigh) newhigh, sum(newlow) newlow from nhnl group by yyy,ddd order by 3 desc";
			sql = "select count(*) from (" + datasql + ") foo";
			st = db.execute(sql);
			if(!st.next()) return null;
			cnt = st.getInt(1);
			st.close();
			sql = datasql;
		}
		NHNL [] nhnl = new NHNL[cnt];
		st = db.execute(sql);
		int row = 0;
		while (st.next()) {
			nhnl[row] = new NHNL(st.getDate("day1"), st.getInt("newhigh"), st.getInt("newlow"));
			row++;
		}

		st.close();

		return nhnl;
	}

//	static NHNL [] getData() throws Exception {
//		String sql = "select count(*) from nhnl";
//		MyStatement st = db.execute(sql);
//		if(!st.next()) return null;
//		int nd = st.getInt(1);
//		st.close();
//		
//		NHNL [] nhnl = new NHNL[nd];
//		
//		sql = "select * from nhnl order by day desc";
//
//		st = db.execute(sql);
//		int row = 0;
//		while (st.next()) {
//			nhnl[row] = new NHNL(st.getDate("day"), st.getInt("newhigh"), st.getInt("newlow"));
//			row++;
//		}
//
//		st.close();
//
//		return nhnl;
//	}

//	public static void refreshCache() {
//		nhnldata = null;
//	}
//	static public NHNL [] toPeriod(NHNL [] daily, String period) {
//		if (period.equalsIgnoreCase("d"))
//			return daily;
//
//		int prevday = 999, row = 0;
//		NHNL [] ret = new NHNL[(int)(daily.length/5)+15];
//		NHNL thisWeek = null;
//		Date d = null;
//		int nh = 0, nl = 0;
//		for(int i = 0; i < daily.length - 1; i++) {
//			NHNL n = daily[i];
//			int curday = getWeekday(n.date);
//			if(curday > prevday) {
//				ret[row] = new NHNL(d, nh, nl);
//				row++;
//				d = n.date;
//				nh=0;
//				nl=0;
//			}
//			nh += n.nh;
//			nl += n.nl;
//			d = n.date;
//			prevday = curday;
//			
//		}
//		return ret;
//
//	}

	static int getWeekday(Date d) {
		// Create a calendar with year and day of year.
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(d.getTime());
		return cal.get(Calendar.DAY_OF_WEEK);
	}


}


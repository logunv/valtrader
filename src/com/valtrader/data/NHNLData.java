/**
 * NHNL - New High New Low object service.
 */
 
package com.valtrader.data;
import java.sql.Date;
import java.util.Calendar;
import java.util.HashMap;

public class NHNLData {
	static HashMap<String,NHNL[]> cache = new HashMap<String, NHNL[]>();
	static MyDatabase db = MyDatabase.db;

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

	static int getWeekday(Date d) {
		// Create a calendar with year and day of year.
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(d.getTime());
		return cal.get(Calendar.DAY_OF_WEEK);
	}

}


package com.valtrader.data;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class MyStatement {
	Statement st;
	public ResultSet rs = null;

	public MyStatement(Connection con, String sql) throws Exception {
		try {
			st = con.createStatement();
			if (st.execute(sql)) {
				rs = st.getResultSet();
			}
		} catch (Exception e) {
			System.out.println(e);
			System.out.println(sql);
			throw e;
		}
	}
	public void close() {
		try {
			if(rs != null) rs.close();
			if(st != null) st.close();
			rs = null;
			st = null;
		} catch(Exception e) {
			// TODO:
		}
	}
	public boolean next() {
		try {
			return rs.next();
		} catch (Exception e) {
			System.out.println(e);
		}
		return false;
	}

	public int getInt(String col) throws Exception {
		return rs.getInt(col);
	}

	public int getInt(int col) {
		try {
			return rs.getInt(col);
		} catch (Exception e) {
			System.out.println(e);
		}
		return -1;
	}

	public float getFloat(int col) {
		try {
			return rs.getFloat(col);
		} catch (Exception e) {
			System.out.println(e);
		}
		return -1;
	}

	public float getFloat(String col) {
		try {
			return rs.getFloat(col);
		} catch (Exception e) {
			System.out.println(e);
		}
		return -1;
	}

	public Date getDate(String col) throws Exception {
		return rs.getDate(col);
	}
	public Date getDate(int col) throws Exception {
		return rs.getDate(col);
	}

	public String getString(int col) {
		try {
			if (rs.getMetaData().getColumnType(col) == java.sql.Types.DATE) {
				java.sql.Date d = rs.getDate(col);
				if (d == null)
					return "";
				return d.getYear() + "-" + d.getMonth() + "-" + d.getDate();
			}
			String v = rs.getString(col);
			if (v != null)
				v = v.trim();
			else
				v = "";
			return v;
		} catch (Exception e) {
			return null;
		}
	}

	public String getString(String col) throws Exception {
		String v = rs.getString(col);
		if (v != null)
			v = v.trim();
		else
			v = "";
		return v;
	}
	public ResultSetMetaData getMetaData() throws Exception {
		return rs.getMetaData();
	}

	public double getDouble(int i) {
		try {
			return rs.getDouble(i);
		} catch (Exception e) {
			return -1;
		}
	}

	public double getDouble(String i) {
		try {
			return rs.getDouble(i);
		} catch (Exception e) {
			return -1;
		}
	}
}

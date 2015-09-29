package com.valtrader.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Vector;

public class MyDatabase {
	static private Connection con = null;

	// private ResultSet rs = null;
	// private Statement st = null;

	static final String DB = "pgsql"; // mysql, rbw

	public MyDatabase() {
		this(DB);
	}

	public MyDatabase(String db) {
		try {
			if (con == null) {
				if (db.equals("rbw")) {
					Class.forName("redbrick.jdbc.RBWDriver");
					String url = "jdbc:rbw:protocol:localhost:5050/stocks/?UID=system&PWD=manager";
					con = DriverManager.getConnection(url);
				} else if (db.equals("pgsql")) {
					String url = "jdbc:postgresql:valtrader?user=postgres&password=logu";
					con = DriverManager.getConnection(url);
//				} else if (db.equals("mysql")) {
//					String url = "jdbc:postgresql:valtrader?user=postgres&password=logu";
//					con = DriverManager.getConnection(url);
				} else {
					System.err.println("Unsupported database requested: " + db);
					System.exit(1);
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void disconnect() {
		try {
			if (con != null)
				con.close();
			con = null;
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public MyStatement execute(String sql) {
//		System.err.println(sql);
		try {
			return new MyStatement(con, sql);
		} catch(Exception e) {
			return null;
		}
	}

	public Vector<String> getSymbols(String[] watchList) throws Exception {
		Vector<String> syms = new Vector<String>();
		String l = "(";
		String comma = "";
		for (int ac = 1; ac < watchList.length; ac++) {
			l += comma + "'" + watchList[ac] + "'";
			comma = ",";
			syms.add(watchList[ac]);
		}
		l += ")";
		MyStatement st = execute("select distinct upper(symbol) as symbol from watchlist where list in "
				+ l + " order by symbol");
		while (st.next()) {
			syms.add(st.getString(1));
		}
		st.close();
		return syms;
	}

}

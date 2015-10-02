package com.valtrader.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Vector;

public class MyDatabase {
	static private Connection con = null;

	// private ResultSet rs = null;
	// private Statement st = null;

	static final String DB = "pgsql"; // mysql

//	public MyDatabase() {
//		this(DB);
//	}

	public MyDatabase(String db, String user, String pwd) {
		try {
			if (con == null) {
				if (db.equals("pgsql")) {
					String url = "jdbc:postgresql:valtrader?user=postgres&password=logu";
					con = DriverManager.getConnection(url);
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

	public static MyDatabase db = null;
	public static MyDatabase init(String dsn, String user, String pwd) {
		return db = new MyDatabase(dsn, user, pwd);
	}

}

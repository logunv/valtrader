/**
 * jsql - jdbc based sql utility.
 * Supports connecting to any JDBC driver.
 * MyDatabase class encapsulates the database backend.
 */

package com.valtrader.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import com.valtrader.data.MyDatabase;
import com.valtrader.data.MyStatement;

public class jsql {
	private static BufferedReader stdin = null;
	private static PrintStream stderr = System.err;
	private static PrintStream stdout = System.out;
	
	final static int error = 1;
	final static int warn = 2;
	final static int success = 0;
	
	final static boolean isPrintStack = false;
	
	public static void main(String [] args)  throws Exception {
		int exitVal = success;
		try {
			stdin = new BufferedReader( new InputStreamReader( System.in ) );

			MyDatabase db = new MyDatabase();

			runQueries(db);

		db.disconnect();
		} catch(Exception exc) {
			exitVal = error;
			stderr.println(exc.getMessage());
			if(isPrintStack) {
				exc.printStackTrace();
			}
		}

	}

	static void runQuery(MyDatabase db, String sql) throws Exception {

		try {
			MyStatement st = db.execute(sql);
			if(st == null || st.rs == null) return;

			ResultSetMetaData rsmd = st.getMetaData();
			for(int i = 1; i <= rsmd.getColumnCount(); i++) {
				stdout.print(rsmd.getColumnName(i) + ",");
			}
			stdout.println("");

			int nrows = 0;
			while(st.next()) {

				for(int i = 1; i <= rsmd.getColumnCount(); i++) {
					String data;
					if(rsmd.getColumnType(i) == Types.DATE) {
						java.sql.Date d = st.getDate(i);
						data = String.format("%02d/%02d/%04d", 
								(d.getMonth() + 1), d.getDate(), (d.getYear() + 1900));
					} else {
						data = st.getString(i);
					}
					if(data == null) data = "(null)";
					stdout.print(data + ",");
				}
				stdout.println("");
				nrows++;
			}
			stdout.println(nrows + " Row(s) printed");
			st.close();
		} catch(Exception e) {
			stderr.println(e.getMessage());
			return;
		}

	}

	static void runQueries(MyDatabase db) throws Exception {
		String query = "";
		String inputLine;
		stderr.print( "jsql> " );
		while( ( inputLine = stdin.readLine() ) != null ) {
			query += inputLine + "\n";
			if( inputLine.endsWith( ";" ) ) {
				query = query.substring(0, query.length() - 2);
				try {
					if ( query.startsWith( "exit" ) ||
							query.startsWith( "quit" ) ) {
						return;
					}

					runQuery(db, query);

				} catch( SQLException e ) {
					stderr.println(e.getMessage());
				}
				stderr.print( "jsql> " );

				query = "";

			} else {
				stderr.print( "> " );
				query += " ";
			}
		}
	}
}


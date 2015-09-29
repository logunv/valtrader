package com.valtrader.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.ResultSetMetaData;

import com.valtrader.data.MyDatabase;
import com.valtrader.data.MyStatement;

public class rb2mysql {
	private static BufferedReader stdIn = null;
	public static void main(String [] args)  throws Exception {
		stdIn = new BufferedReader( new InputStreamReader( System.in ) );

		MyDatabase db = new MyDatabase("rbw");

		downloadTables(db);

//		runQueries(db);

		db.disconnect();

	}

	static void downloadTables(MyDatabase db) throws Exception {
		String inputLine;
		while( ( inputLine = stdIn.readLine() ) != null ) {
			if(inputLine.equals("\\q")) break;
			runQueryX(db, inputLine);
		}
		
	}

	static void runQueryX(MyDatabase db, String table) throws Exception {
		table = table.trim();
		System.err.println("Downloading ..." + table);
		
		String load = "LOAD DATA LOCAL INFILE 'c:/tmp/mysql/" + table + ".csv'\n" +
	    	"INTO TABLE " + table + " columns terminated by ',';\n";
	
		String sql = "Select * from " + table;
		try {
			MyStatement st = db.execute(sql);
			if(st == null) return;

			ResultSetMetaData rsmd = st.getMetaData();
			PrintStream out = new PrintStream(new FileOutputStream(new File("c:/tmp/mysql/rb.sql"),true));
			String ddl="Drop table " + table + ";\n";
			ddl += "Create table " + table + " (\n";
			String comma = "";
			String loadAppends = "";
			String select = "select ";
			for(int i = 1; i <= rsmd.getColumnCount(); i++) {
				
				String dataType = rsmd.getColumnTypeName(i);
				String lcol = rsmd.getColumnName(i);
				switch(rsmd.getColumnType(i)) {
				case java.sql.Types.CHAR:
					dataType += "(" + rsmd.getColumnDisplaySize(i) + ")";
					break;
				case java.sql.Types.DOUBLE:
					dataType = "float";
					break;
				case java.sql.Types.DATE:
					lcol = "string(" + lcol + ")";
					break;
				}
				select += comma + lcol;
				
				ddl += "  " + comma + rsmd.getColumnName(i) + " " + dataType + "\n";
//				datarow += comma + rsmd.getCatalogName(i);
				comma = ",";
			}
			select += " from " + table;
			ddl += ");\n";
			load += loadAppends + ";\n";
			
//			datarow += "\n";

			out.println(ddl);
			out.println(load);
			out.println("select count(*) from " + table + ";\n");
			out.close();
//			if(true) return;
			out = new PrintStream(new FileOutputStream(new File("c:/tmp/mysql/" + table + ".csv")));
			
			int nrows = 0;
			st.close();
			st = db.execute(select);
			while(st.next()) {
//System.err.println(nrows);
				for(int i = 1; i <= rsmd.getColumnCount(); i++) {
					String data = st.getString(i);
					if(data == null) data = "";
					out.print(data + ",");
				}
				out.println("");
				nrows++;
			}
			out.close();
			System.out.println(nrows + " Row(s) printed.");
			st.close();
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			return;
		}

	}

}


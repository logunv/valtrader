/*
 * (c) Copyright VAL Investments, 2003-2004
 */

/*
 - get the stock quote from yahoo as excel file
 - this will be more reliable than getting as html and parsing
 - it will be faster, page format change does not impact
 */
package equotes;


import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Vector;

import com.valtrader.data.MyDatabase;

public class GetQuote {
    // Usage: GetQuote startDate watchlist ...
    public static void main(String [] args) {
//    	args = new String[]{"12/01/2012", "test"};

        try {
            get(args);
            System.exit(0);
        } catch(Exception e) {
            System.exit(1);
        }
    }

    public static void get(String[] args) throws Exception {
	Vector<String> symbols = new Vector<String>();
        if(args[1].equals("-s")) { // list is symbols
		for(int i = 2; i < args.length; i++) {
			symbols.add(args[i]);
		}
        } else {
        MyDatabase d = new MyDatabase();
            symbols = d.getSymbols(args);
		d.disconnect();
	}
            int errCnt = 0;
            for(int j = 0; j < symbols.size(); j++) {
                try {
                    get(symbols.get(j), args[0]);
                } catch(Exception e) {
                    System.err.println("Could not get quote for " + 
                            symbols.get(j)  + ":	 " + e.getMessage());
                     System.err.println(e.getMessage());
//                     d.execute("Insert into nodata(symbol) values('" + symbols.get(j) + "')");
                    errCnt++;
                }
                System.err.print(j + " / " + symbols.size() + "          \r");
            }
            if(errCnt > 0) System.err.println("Could not get data for " + errCnt + 
                    " symbols in " + args);
    }

    static private void out(String s) { System.out.println(s); }

    static private void get(String symbol, String startDate)
				throws Exception {
        // startDate: mm/dd/yyyy
        String m, d, y;
        int idx = startDate.indexOf("/");
        m = startDate.substring(0, idx);
        startDate = startDate.substring(idx+1);
        idx = startDate.indexOf("/");
        d = startDate.substring(0, idx);
        y = startDate.substring(idx+1);
        get(symbol, m, d, y);
    }

    static private void get(String symbol, String m1, String d1, String  y1)
				throws Exception {
        String page;
        Date dt = new Date();
        int m = (new Integer(m1)).intValue() - 1;
        // Nov 14, 2012
        // this method does not work ^DJ* symbols. Yahoo reports that Dow Jones does not give the
        // data for these symbols this way. 
            page = "http://ichart.finance.yahoo.com/table.csv?s=" + symbol +
			"&a=" + m + "&b=" + d1 + "&c=" + y1 +
			"&d=" + dt.getMonth() + "&e=" + dt.getDate() + 
                            "&f=" + (1900 + dt.getYear()) +
                        "&g=d&ignore=.csv";
//System.err.println(page);
		URL url = new URL(page);
		InputStream is = url.openConnection().getInputStream();
		dumpPage(symbol, is);
//System.err.println("done");
    }

    static private void dumpPage(String sym, InputStream is) {
                // ignore the first line, the header
		int c;
		try {

		//while((c = is.read()) != '\n') ;
                boolean printSym = true;
		while((c = is.read()) != -1) {
                        if(c == '<') break;
                        if(printSym) {
			    System.out.print(sym + ",");
                            printSym = false;
			}
			System.out.print((char)c);
                        if(c == '\n') printSym = true;
		}
		} catch (Exception e) {}
    }
}


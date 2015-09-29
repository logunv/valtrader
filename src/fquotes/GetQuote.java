/*
 * (c) Copyright VAL Investments, 2003-2005
 */
package fquotes;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.StringTokenizer;
/**
 * GetQuote -- Get Futures Quote
 *
 * Usage: GetQuote [ yy [ mm [ dd ] ] ]
 *
 * arguments:
 *   none - get data for today
 *   yy   - get data for the entire year
 *   yy mm - get the data for the given year and month
 *   yy mm dd - get the data for the given date
 *
 */

public class GetQuote {
    // usage: GetQuote YEar [ month [ day ] ]
    public static void main(String [] args) {
        int y1, y2;
        int m1 = 1, m2 = 12;
        int d1 = 1, d2 = 31;

        int l;
        l =  args.length;
        if (l < 1) {
            //out("Usage: GetQuote YEar [ month [ day ] ]");
            Date dt = new Date();
            y1 = y2 = dt.getYear() + 1900;
            m1 = m2 = dt.getMonth() + 1;
            d1 = d2 = dt.getDate();
//out(d1 + " " + d2 + " " + m1 + " " + m2 + " " + y1 + " " + y2);
        } else {

        y1 = 2000 + (new Integer(args[0])).intValue();
        y2 = y1;

        if(l > 1) {
            m2 = m1 = (new Integer(args[1])).intValue();
        }
        if(l > 2) {
            d1 = d2 = (new Integer(args[2])).intValue();
        }
        }

        for(int y = y1; y <= y2; y++) {
            for(int m = m1; m <= m2; m++) {
                for(int d = d1; d <= d2; d++) {
                    try {
                        get(m, d, y);
                    } catch(Exception e) {
                    }
                }
            }
        }
    }

    static private void out(String s) { System.out.println(s); }

    static private void get(int month, int day, int  year)
				throws Exception {
        String page;
        Date dt = new Date();
if(true)
        page = "http://www.futuresguide.com/ss.php?day=" + day +
		"&month=" + month + "&year=" + year + "&t=f";
else
        page = "http://www.futuresguide.com/today.txt";
//out(page);
		URL url = new URL(page);
		InputStream is = url.openConnection().getInputStream();
		dumpPage(is);
	}

    static private void dumpPage(InputStream is) {
        try {
            String str;
            BufferedReader stdin = new BufferedReader(new InputStreamReader(is));
            while((str = stdin.readLine()) != null) {
                if(str.indexOf("/") >= 0 &&
                        str.indexOf("http") < 0 &&
                        str.indexOf("personal") < 0) {
                    StringTokenizer st = new StringTokenizer(str);
                    String s = "";
                    while(st.hasMoreTokens()) {
                        s += st.nextToken() + ",";
                    }
                    out(s);
                }
            }
        } catch(Exception e) {}
    }
}

/*
 * (c) Copyright VAL Investments, 2003-2005
 */
package fquotes;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
/**
 * GetCot -- Get current COT report from www.cftc.gov
 *
 */

public class extdata {
    static private void out(String s) { System.out.println(s); }
    public static void main(String [] args) throws Exception {
        // usage: symbol, file, date
//    	args = new String[] {
//    			"EUR", "t:/downloads/FUTURE_EC1.csv", "20121201"
//    	};
        String symbol = args[0], file = args[1];
        int date = (new Integer(args[2])).intValue();
        BufferedReader in = new BufferedReader(
                              new InputStreamReader(
                                new FileInputStream(file)
                              )
                        );
        String line;
    	int lineno = 0;
        try {
        while((line = in.readLine()) != null) {
        	lineno++;
            int idx = line.indexOf(",");
if(true) { // data from Quandl - Jan 05, 2013
	if(lineno == 1) continue;
String dateValue = line.substring(0,idx);
dateValue = dateValue.replaceAll("-","");
int intVal = Integer.parseInt(dateValue);
if(intVal >= date) System.out.println(symbol + "," + line);
} else {
            if((new Integer(line.substring(0, idx))).intValue() >= date) {
                System.out.println(symbol + "," + line);
            }
}

        }
        in.close();
        } catch(Exception e) { 
        	System.err.println(lineno + ":" + e.getMessage()); 
        }
    }
}

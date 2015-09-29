/*
 * (c) Copyright VAL Investments, 2003-2005
 */
package fquotes;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
/**
 * GetCot -- Get current COT report from www.cftc.gov
 *
 */

public class getdata {
    static private void out(String s) { System.out.println(s); }
    public static void main(String [] args) {
        try {
        String page = "http://www.tradingblox.com/Data/DataOnly.zip";
		URL url = new URL(page);
		InputStream is = url.openConnection().getInputStream();
            FileOutputStream os = new FileOutputStream("DataOnly.zip");
            int cnt;
            byte [] buffer = new byte[1024];
            while((cnt = is.read(buffer)) > 0) os.write(buffer, 0, cnt);
            os.close();
        } catch(Exception e) { System.err.println(e.getMessage()); }
    }
}

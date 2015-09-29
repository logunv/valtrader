package fquotes;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;


public class download {
	static public void main(String [] args) throws Exception {
		saveURL(args[0], args[1]);
	}
	
	static public void saveURL(String page, String outfile) throws Exception {
		URL url = new URL(page);
		InputStream is = url.openConnection().getInputStream();
	
		FileOutputStream fos = new FileOutputStream(new File(outfile));
		byte [] data = new byte[4096];
		int len;
		while ((len = is.read(data)) > 0) {
			fos.write(data, 0, len);
		}
		fos.close();
		is.close();
	}

}

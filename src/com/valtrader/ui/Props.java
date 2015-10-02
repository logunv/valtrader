package com.valtrader.ui;

import java.io.FileOutputStream;
import java.util.Properties;

public class Props extends Properties {

	String propFile = "config.properties";
	public Props() throws Exception {
		load(Props.class.getClassLoader().getResourceAsStream(propFile));
	}

	public String get(String key) {
		return get(key, null);
	}
	public String get(String key, String def) {
		String val = getProperty(key);
		if(val == null) return def;
		return val;
	}
	
	public int getInt(String key, int def) {
		return Integer.parseInt(get(key, def + ""));
	}
	void save() throws Exception {
		FileOutputStream fos = new FileOutputStream(propFile);
		store(fos, null);
		fos.close();
	}
}

package com.valtrader.service;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import com.valtrader.data.MyDatabase;
import com.valtrader.data.MyStatement;
import com.valtrader.data.Quote;
import com.valtrader.ui.Trade;
import com.valtrader.ui.ValTrader;
import com.valtrader.ui.Wave;
import com.valtrader.utils.Utils;

public class Quotes {

	static Map<String, String> symbolsMap = new java.util.HashMap<String, String>();

	static public void loadSymbolsMap() {
		try {
			MyStatement st = db.execute("select src, dst from symbol_map");
			while (st.next()) {
				symbolsMap.put(st.getString("src").toUpperCase(),
						st.getString("dst"));
			}
			st.close();
		} catch (Exception e) {
		}
	}

	private Quotes(String sym, String type, int numdays) {
		this.symbol = sym;
		this.type = type;
		this.numdays = numdays;
		open = new float[numdays];
		high = new float[numdays];
		low = new float[numdays];
		close = new float[numdays];
		volume = new int[numdays];
		date = new Date[numdays];
	}

	public Quote get(int day) {
		if (day >= size())
			return null;
		try {
			return new Quote(date[day], open[day], high[day], low[day],
					close[day], volume[day]);
		} catch (Exception e) {
			ValTrader.msgBox(e.getMessage());
		}
		return null;
	}

	int numdays;

	public int size() {
		return numdays;
	}

	String symbol, type;

	public String getSymbol() {
		return symbol;
	}

	public String getType() {
		return type;
	}

	public float[] getOpen() {
		return open;
	}

	public float[] getClose() {
		return close;
	}

	public float[] getLow() {
		return low;
	}

	public float[] getHigh() {
		return high;
	}

	public int[] getVolume() {
		return volume;
	}

	public Date[] getDate() {
		return date;
	}

	public Date getDate(int day) {
		return date[day];
	}

	public long getVolume(int day) {
		return volume[day];
	}

	public float getOpen(int day) {
		return open[day];
	}

	public float getClose(int day) {
		return close[day];
	}

	public float getLow(int day) {
		return low[day];
	}

	public float getHigh(int day) {
		return high[day];
	}

	float[] open;
	float[] high;
	float[] low;
	float[] close;
	int[] volume;
	Date[] date;

	static MyDatabase db = MyDatabase.db;

	static Map<String, Quotes> cache = new HashMap<String, Quotes>();

	static public Quotes getQuotes(String symbol, String type, int start,
			int numdays, String period) throws Exception {

		symbol = symbol.toUpperCase().trim();
		if (symbolsMap.containsKey(symbol)) {
			symbol = symbolsMap.get(symbol);
		}

		Quotes res = null;
		int ndays = numdays + start;
		if (period.equals("w"))
			ndays *= 5;

		if (cache.containsKey(symbol)) {
			res = cache.get(symbol);
			if (res.size() >= ndays)
				if (!weekly) {
					return returnQuote(res, start, numdays);
				} else {
					return toPeriod(res, start, numdays, period);
				}
			cache.remove(res);
		}

		res = getEquotes(symbol, type, ndays, period);
		cache.put(symbol, res);
		if (!weekly)
			return returnQuote(res, start, numdays);
		return toPeriod(res, start, numdays, period);
	}

	static boolean weekly = true;

	private static Quotes returnQuote(Quotes res, int start, int numdays) {
		if (res == null || res.size() == 0)
			return null;
		int i1;
		if (res.size() < numdays) {
			i1 = 100;
			numdays = res.size();
		}
		if (res.size() < numdays)
			numdays = res.size();
		Quotes quotes = new Quotes(res.symbol, res.type, numdays);
		numdays--;// TODO:
		for (int i = 0; i < numdays; i++) {
			Quote q = res.get(i + start);
			quotes.set(i, q);
		}

		quotes.notes = res.notes;

		quotes.setSize(numdays);

		return quotes;

	}

	private void set(int row, Quote quote) {
		if (row >= size())
			return;
		this.set(row, quote.date, quote.open, quote.high, quote.low,
				quote.close, quote.volume);
	}

	static public Quotes toPeriod(Quotes daily, int start, int numdays, String period) {
		if (period.equalsIgnoreCase("d"))
			return returnQuote(daily, start, numdays);

		// assume it is weekly and convert to weekly.

		Quote thisWeek = null, q;
		float o = 0, l = 0, c = 0, h = 0;
		Date d = null;

		int prevDay = 999, curDay;
		int v = 0;
		Quotes quotes = new Quotes(daily.symbol, daily.type, (daily.size() / 5));

		int row = 0;
		// ValTrader.msgBox(daily.size()+"");
		for (int i = 0; i < daily.size(); i++) {
			q = daily.get(i);

			curDay = getWeekday(q.date);
			if (curDay > prevDay) {
				thisWeek.open = o;
				thisWeek.low = l;
				thisWeek.close = c;
				thisWeek.high = h;
				thisWeek.volume = v;
				thisWeek.date = d;
				quotes.set(row, thisWeek.date, thisWeek.open, thisWeek.high,
						thisWeek.low, thisWeek.close, (int) thisWeek.volume);
				thisWeek = null;
				row++;
				if (row >= 200)
					break;
			}
			if (thisWeek == null) {
				thisWeek = new Quote();
				//
				c = q.close;
				l = q.low;
				o = q.open;
				v = q.volume;
				d = q.date;
				h = q.high;
			} else {
				o = q.open;
				d = q.date;
				if (l > q.low)
					l = q.low;
				if (h < q.high)
					h = q.high;
				v += q.volume;
			}

			prevDay = curDay;
		}
		return Quotes.returnQuote(quotes, 0, row - 2);
	}

	static int getWeekday(Date d) {
		// Create a calendar with year and day of year.
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(d.getTime());
		return cal.get(Calendar.DAY_OF_WEEK);
	}

	static Quotes getEquotes(String symbol, String type, int numdays,
			String period) throws Exception {

		return getQuotesData(symbol, type, "quotes", numdays, period);

	}

	static Quotes getFquotes(String symbol, String type, int numdays,
			String period) throws Exception {

		return getQuotesData(symbol, type, "fquotes", numdays, period);

	}

	static Properties loadedList = new Properties();

	public static void loadList(String wl) {
		try {
			// params: list, trade_day
			String sql = "select q.* from quotes q, watchlist w "
					+ "where w.list='%s' and w.symbol = q.symbol and "
					+ "trade_day >= '%s' order by symbol, trade_day desc";

			if (loadedList.getProperty(wl) != null)
				return;
			int numdays = 250;
			java.util.Date d = new java.util.Date();
			long nd = (long) (numdays * 1.5);
			d.setTime(d.getTime() - (nd * 24 * 60 * 60 * 1000));
			String dtValue = Utils.formatDate(d, "yyyy-MM-dd");

			MyStatement st = db.execute(String.format(sql, wl, dtValue));
			int row = 0;
			String prev = "";

			Quotes quotes = null;
			while (st.next()) {
				String sym = st.getString("symbol");
				if (!prev.equals(sym)) {
					if (quotes != null) {
						quotes.setSize(row);
						cache.put(prev, quotes);
						row = 0;
					}
					//System.err.println("Loading:" + sym);
					quotes = new Quotes(sym, "E", numdays);
					prev = sym;
					ValTrader.setStatus1(prev);
				}

				quotes.set(row, st.getDate("trade_day"),
						st.getFloat("open_price"), st.getFloat("high"),
						st.getFloat("low"), st.getFloat("close_price"),
						st.getInt("vol"));
				// if(quotes.size() >= numdays) break;
				row++;
			}
			if (quotes != null) {
				quotes.setSize(row);
				cache.put(prev, quotes);
				row = 0;
			}

			st.close();
			ValTrader.setStatus1("Done Loading: " + wl);

			loadedList.put(wl, wl);
		} catch (Exception e) {
			ValTrader.msgBox(e.getMessage());
		}
	}

	static Quotes getQuotesData(String symbol, String type, String table,
			int numdays, String period) throws Exception {
		// TODO: for dataset larger than 200, jfreechart seems to hang.

		// if (numdays > 200)
		// numdays = 200; // jfreechart bug; hangs if the data is more than 250
		// // or so
		// if(period.equals("w")) numdays *= 5;

		numdays += 200; // konjam kooda pottukkodu
		
		
		// let us get data for 300 weeks.
		numdays = 300 * 5;

		java.util.Date d = new java.util.Date();
		long nd = (long) (numdays * 1.5);
		d.setTime(d.getTime() - (nd * 24 * 60 * 60 * 1000));
		String dtValue = Utils.formatDate(d, "yyyy-MM-dd");

		String sql = "select * from %s where upper(symbol) = '%s' and trade_day >= '%s' order by trade_day desc limit "
				+ numdays;

		Quotes quotes = new Quotes(symbol, type, numdays);

		MyStatement st = db.execute(String.format(sql, table, symbol, dtValue));
		int row = 0;
		while (st.next()) {
			quotes.set(row, st.getDate("trade_day"), st.getFloat("open_price"),
					st.getFloat("high"), st.getFloat("low"),
					st.getFloat("close_price"), st.getInt("vol"));
			// if(quotes.size() >= numdays) break;
			row++;
		}
		quotes.setSize(row);

		st.close();

		// get notes if any
		// st =
		// db.execute(String.format("select * from notes where symbol = '%s'",
		// symbol));
		// quotes.notes = new Vector<Notes>();
		// while (st.next()) {
		// quotes.notes.add(new Notes(st.getDouble("x"), st.getDouble("y"), st
		// .getString("text"), st.getDate("notes_day")));
		// }
		//
		// st.close();

		return quotes;
	}

	private void setSize(int size) {
		numdays = size;
	}

	public float min = 9999;
	public float max = -9999;

	private void set(int row, Date date, float open, float high, float low,
			float close, int vol) {
		if (row >= size())
			return;
		this.date[row] = date;
		this.open[row] = open;
		this.high[row] = high;
		this.low[row] = low;
		this.close[row] = close;
		this.volume[row] = vol;

		if (min > low)
			min = low;
		if (max < high)
			max = high;
	}

	public static void dropSymbol(String wl, String sym) {

		String delete = "delete from watchlist where list = '%s' and symbol = '%s'";
		db.execute(String.format(delete, wl, sym));
	}

	public static void dropList(String wl) {

		String delete = "delete from watchlist where list = '%s'";
		db.execute(String.format(delete, wl));
	}

	public static void addSymbol(String list, String symbol, String st) {

		String insert = "Insert into watchlist(list, symbol, symbol_type, show_flag) values ('%s', '%s', '%s', 'y')";

		db.execute(String.format(insert, list, symbol, st));
	}

	public static void saveText(String sym, long x, float y, String text) {

		sym = sym.toUpperCase();
		String sql = "insert into notes(symbol, x, y, text) values('%s', %d, %f, '%s')";

		db.execute(String.format(sql, sym, x, y, text));

	}

	public Vector<Notes> notes = null;

	public static void refreshCache() {
		Quotes.cache.clear();
	}

	public static void removeAnnotations(String sym) {
		Quotes res = cache.get(sym);
		if (res == null)
			return;
		res.notes.clear();
	}

	static Map<String, Future> futures = new HashMap<String, Future>();

	public static float getCSize(String m) {

		if (futures.size() == 0) {
			loadFutures();
		}

		float csize = 1;

		Future f = futures.get(m);
		if (f != null)
			csize = f.cSize;

		return csize;
	}

	public static float getScale(String m) {

		if (futures.size() == 0) {
			loadFutures();
		}

		float scale = 1;

		Future f = futures.get(m);
		if (f != null)
			scale = f.scale;

		return scale;
	}

	static void loadFutures() {

		try {
			String sql = "select * from futures";

			MyStatement st = db.execute(sql);
			while (st.next()) {
				futures.put(st.getString("symbol"),
						new Future(st.getString("symbol"), st.getFloat("size"),
								st.getInt("scale"), st.getFloat("quote_unit")));
			}
			st.close();
		} catch (Exception e) {
		}
	}

	public static void addWave(String sym, int id, long dt, float val) {
		// symbol, wave_id, wave_date, wave_price
		String sql = "insert into waves values('%s', %d, %d, %f) ";

		db.execute(String.format(sql, sym, id, dt, val));
		waves.remove(sym);

	}

	static Map<String, Wave> waves = new HashMap<String, Wave>();
	static Map<String, Trade> trades = new HashMap<String, Trade>();

	static public Wave getWave(String symbol) throws Exception {

		Wave wave = waves.get(symbol);
		if (wave != null)
			return wave;

		String sql = "select * from waves where sym = '%s' order by id asc";
		MyStatement st = db.execute(String.format(sql, symbol));
		while (st.next()) {
			if (wave == null)
				wave = new Wave();
			wave.add((long) st.getFloat("dt"), st.getFloat("val"));
		}

		st.close();

		waves.put(symbol, wave);

		return wave;
	}

	static public Trade getTrade(String symbol) {
		try {
			Trade trade = trades.get(symbol);
			if (trade != null)
				return trade;
			if (trades.containsKey(symbol))
				return null;

			String sql = "select * from trades where symbol = '%s' and status = 'open'";
			MyStatement st = db.execute(String.format(sql, symbol));
			if (st.next()) {
				trade = new Trade(st.getString("symbol"), st.getInt("csize"),
						st.getInt("size"), st.getDate("entry_date"),
						st.getFloat("entry_price"), st.getFloat("stop_price"),
						st.getFloat("target_price"));
			}

			st.close();

			trades.put(symbol, trade);

			return trade;
		} catch (Exception e) {
			return null;
		}
	}

	public static void dropWave(String sym) {
		// symbol, wave_id, wave_date, wave_price
		String sql = "delete from waves where sym = '%s'";

		db.execute(String.format(sql, sym));

		waves.remove(sym);
	}

	public static void refreshCache(String sym) {
		cache.remove(sym);
	}

	public static void saveScan(String scan, String sym) {
		String sql = "insert into scan_result(scan, symbol) values('%s', '%s')";

		db.execute(String.format(sql, scan, sym));

	}

	public static void addTrade(String sym, int csize, int size,
			String entryDate, float entryPrice, float stopPrice,
			float targetPrice) {
		dropTrade(sym);
		// symbol, wave_id, wave_date, wave_price
		String sql = "insert into trades values('%s', %d, %d, '%s', %f, %f, %f, 'open') ";

		db.execute(String.format(sql, sym, csize, size, entryDate, entryPrice,
				stopPrice, targetPrice));

	}

	public static void dropTrade(String sym) {
		String sql = "delete from trades where symbol = '%s' ";

		db.execute(String.format(sql, sym));
		trades.remove(sym);

	}

	// load quotes of a given watch list
	public void loadWatchList(String wl, int numdays) throws Exception {
		// select * from quotes q, watchlist wl
		// where wl.list='' and wl.symbol = q.symbol
		// and

		numdays += 200; // konjam kooda pottukkodu

		java.util.Date d = new java.util.Date();
		long nd = (long) (numdays * 1.5);
		d.setTime(d.getTime() - (nd * 24 * 60 * 60 * 1000));
		String dtValue = Utils.formatDate(d, "yyyy-MM-dd");

		String sql = "select * from quotes q, watchlist wl"
				+ " where wl.list='%s' q.symbol = wl.symbol and"
				+ " trade_day >= '%s' order by symbol asc, trade_day desc ";

		Quotes quotes = new Quotes(symbol, type, numdays);

		MyStatement st = db.execute(String.format(sql, wl, dtValue));
		int row = 0;
		while (st.next()) {

			quotes.set(row, st.getDate("trade_day"), st.getFloat("open_price"),
					st.getFloat("high"), st.getFloat("low"),
					st.getFloat("close_price"), st.getInt("vol"));
			// if(quotes.size() >= numdays) break;
			row++;
		}
		quotes.setSize(row);

		st.close();

		// get notes if any
		st = db.execute(String.format(
				"select * from notes where symbol = '%s'", symbol));
		quotes.notes = new Vector<Notes>();
		while (st.next()) {
			quotes.notes.add(new Notes(st.getDouble("x"), st.getDouble("y"), st
					.getString("text"), st.getDate("notes_day")));
		}

		st.close();

	}

	public static void split(String sym, String dt, String factor) {

		String sql = "Update quotes set " + " open_price=open_price * "
				+ factor + ", low = low*" + factor + ", high = high*" + factor
				+ ", close_price = close_price*" + factor + ", vol = vol/"
				+ factor + " where symbol = '" + sym + "' and trade_day < '"
				+ dt + "'";
		String ok = ValTrader.getInput(sql, "");
		if (ok == null)
			return;
		db.execute(sql);

	}

	public static void addSymbols(String list, String symlist) {
		if (list == null || symlist == null)
			return;
		String[] syms = symlist.split(" ");
		for (String s : syms)
			addSymbol(list, s, "E");
	}

	public static void archiveList(String list) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		String today = sdf.format(new java.util.Date());
		String sql = "insert into watchlist(list,symbol, symbol_type, info, show_flag) "
				+ "select 'archive', symbol, 'E', '"
				+ list
				+ ":"
				+ today
				+ "','y'" + " from watchlist where list = '" + list + "';";
		;

		// ValTrader.msgBox(sql);
		db.execute(sql);
		ValTrader.msgBox("Symbols from " + list + " is archived");
	}

	public static String getSymbols(String list) {
		String sql = "select symbol from watchlist where list = '" + list + "'";
		MyStatement st = db.execute(sql);
		
		String ret = "";
		while(st.next()) {
			ret += st.getString(1) + " ";
		}
		st.close();
		return ret;
	}
	public static void copyList(String oldlist, String newlist) {
		String sql = "insert into watchlist(list, symbol, show_flag) "
				+ "select '" + newlist
				+ "', symbol, 'Y' from watchlist where list = '" + oldlist
				+ "'";
		
		db.execute(sql);
	}

	public static void removeDups(String wl) {
		List<String> dups = new ArrayList<String>();
		String sql = "select symbol,count(*) from watchlist where list='" + wl + "' group by symbol having count(*) > 1";
		MyStatement st = db.execute(sql);
		while(st.next()) {
			dups.add(st.getString(1));
		}
		st.close();
		for(String s: dups) {
			db.execute("delete from watchlist where list = '" + wl + "' and symbol = '" + s + "'");
			db.execute("insert into watchlist(list,symbol,show_flag) values('" + wl + "','" + s + "','y')");
		}
	}
}

class Future {
	public Future(String symbol, float cSize, int scale, float unit) {
		super();
		this.symbol = symbol;
		this.cSize = cSize;
		this.scale = scale;
		this.unit = unit;
	}

	String symbol;
	float cSize;
	int scale;
	float unit;
}

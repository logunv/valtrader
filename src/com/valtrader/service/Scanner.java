package com.valtrader.service;

import java.sql.Date;
import java.util.Vector;

import com.valtrader.data.MyDatabase;
import com.valtrader.data.MyStatement;
import com.valtrader.data.Quote;
import com.valtrader.ui.ValTrader;
import com.valtrader.utils.Calc;
import com.valtrader.utils.Callback;
import com.valtrader.utils.Candle;
import com.valtrader.utils.Utils;

public class Scanner {
	static public int [] getMOM1(Quotes quote) {
		int [] ret = new int[quote.size()];

		if(quote.size() < 50) return ret;

		float[] op = quote.getOpen();
		float[] hi = quote.getHigh();
		float[] lo = quote.getLow();
		float[] cl = quote.getClose();
		
		// 5. 3-5 day short term trends
		// in the last 10 days, find if there is established up or down trend
		for(int i = 0; i < quote.size() - 5; i++) {
			if(lo[i] > lo[i+1]  && hi[i] > hi[i+1] &&
					lo[i+1] > lo[i+2]  && hi[i+1] > hi[i+2] &&
					lo[i+2] > lo[i+3]  && hi[i+2] > hi[i+3]
					) {
				ret[i] = 1;
			}
			if(lo[i] < lo[i+1]  && hi[i] < hi[i+1] &&
					lo[i+1] < lo[i+2]  && hi[i+1] < hi[i+2] &&
					lo[i+2] < lo[i+3]  && hi[i+2] < hi[i+3]
					) {
				ret[i] = -1;
			}
		}
		
		return ret;
	}

	static public Vector<String> getMOM(Quotes quote) {
		
		// get Message of the Market
		// 1. H&S
		// 2. Double tops or bottoms
		// 3. Triangles and wedges
		// 4. Successful breakout of trading ranges
		// 5. 3-5 day short term trends
		// 6. NRBs
		// 7. Volume spikes
		// 8. Reversal days
		// 9. Bullish inverted hammer
		// 10. Bearish inverted hammer
		// 11. Bullish doji star
		// 12. bearish doji star
		// 13. Morning star
		// 14. Evening star
		// 15. Bullish doji star
		// 16. bearish doji star
		// 17. Bullish abandoned baby
		// 18. Bearish abandoned baby
		// 19. Bullish side by side white lines
		// 20. Bearish side by side white lines
		// 21. Bullish side by side black lines
		// 22. Bearish side by side black lines
		// 23. Upside tasuki gap
		// 24. Downside tasuki gap
		// 25. Upside gap filled
		// 26. Downside gap filled

		Vector<String> ret = new Vector<String>();

		if(quote.size() < 50) return ret;

		float[] op = quote.getOpen();
		float[] hi = quote.getHigh();
		float[] lo = quote.getLow();
		float[] cl = quote.getClose();
		int[] vol = quote.getVolume();
		
		// 5. 3-5 day short term trends
		// in the last 10 days, find if there is established up or down trend
		float [] atr = Calc.calcATR(quote, 20);
		float [] av = Calc.calcSMA(vol, 50);
		for(int i = 0; i < 10; i++) {
			if(lo[i] > lo[i+1]  && hi[i] > hi[i+1] &&
					lo[i+1] > lo[i+2]  && hi[i+1] > hi[i+2] &&
					lo[i+2] > lo[i+3]  && hi[i+2] > hi[i+3]
					) {
				ret.add("up:3-5 -- " + i);
//				break;
			}
			if(lo[i] < lo[i+1]  && hi[i] < hi[i+1] &&
					lo[i+1] < lo[i+2]  && hi[i+1] < hi[i+2] &&
					lo[i+2] < lo[i+3]  && hi[i+2] < hi[i+3]
					) {
				ret.add("down:3-5 -- " + i);
//				break;
			}
			if((hi[i] - lo[i]) < atr[i] * 0.5) ret.add("NRB:" + i);
			
			// volume spike
			if(vol[i] > av[i] * 1.5) ret.add("Volume Spike: " + i);
			
			// Bullish inverted hammer
			// long black candle + downside gap + no lower tail
			
			float r2 = hi[i+1] - lo[i+1];
			if(r2 > atr[i+2] * 1.5 && cl[i+1] < op[i+1] && // long black candle yesterday
					op[i] < lo[i+1] && // downside gap today
					lo[i] == op[i] ) { // no lower tail
				ret.add("Bullish inverted hammer:" + i);
			}
			if(r2 > atr[i+2] * 1.5 && cl[i+1] > op[i+1] && // long white candle yesterday
					op[i] > hi[i+1] && // up gap today
					(lo[i] == op[i] || lo[i] == cl[i]) ) { // no lower tail
				ret.add("Bearish inverted hammer:" + i);
			}
			
			// 
		}
		
		
		// 6. NRBs
		
		
		// 7. Volume spikes
		// 8. Reversal days
		// 9. Bullish inverted hammer
		// 10. Bearish inverted hammer
		// 11. Bullish doji star
		// 12. bearish doji star
		// 13. Morning star
		// 14. Evening star
		// 15. Bullish doji star
		// 16. bearish doji star
		// 17. Bullish abandoned baby
		// 18. Bearish abandoned baby
		// 19. Bullish side by side white lines
		// 20. Bearish side by side white lines
		// 21. Bullish side by side black lines
		// 22. Bearish side by side black lines
		// 23. Upside tasuki gap
		// 24. Downside tasuki gap
		// 25. Upside gap filled
		// 26. Downside gap filled

		
		
		return ret;
	}
	
	static public Vector<String> scan(ValTrader main, String sys,
			String watchlist, int st, int end, String dir, boolean stopAtFirst,
			Callback callback) {
		return scan(main, sys, watchlist.split(" "), st, end, dir, stopAtFirst,
				callback);
	}

	static public Vector<String> scan(ValTrader main, String system1,
			String[] watchlist, int st, int nd, String dir,
			boolean stopAtFirst, Callback callback) {

		int end = nd + st;
		Vector<String> res = new Vector<String>();

		String[][] params = Utils.parseArgs(system1);

		try {
			MyDatabase db = MyDatabase.db;
			Vector<Symbol> symbols = getSymbols(db, watchlist);
			
			int[] sigs;
			main.progressBar.setValue(symbols.size());

			int cnt = 0;
			String system = params[0][0];
			int numdays = 200;
			if (st + nd > numdays)
				numdays = st + nd + 155;
			for (Symbol sym : symbols) {
				try {
					boolean found = false;
					main.setProgress(
							(int) ((float) cnt / symbols.size() * 100),
							(cnt + 1) + " / " + symbols.size());
					cnt++;
					ValTrader.setStatus1(sym.symbol + "[" + cnt + " / " + symbols.size() + "]");
					Quotes quotes = Quotes.getQuotes(sym.symbol, sym.type, 0,
							numdays, "d");
					if (quotes == null)
						continue;
					if(system.startsWith("candle")) {
						// candle signames,... dir
						String [] tmp = system.split(" ");
						String what = null;
						int dir1 = 0;
						if(tmp.length > 1) what = tmp[1];
						if(tmp.length > 2) {
							String d = tmp[2];
							if(d.equals("up")) dir1 = Scanner.bullish; else dir1 = Scanner.bearish;
						}
						sigs = new int[quotes.size()];
						Candle.check(quotes, sigs, null, what, dir1);
					}
					if (system.contains("tail")) {
						sigs = checkForTail(quotes);
						if (sigs == null) {
							continue;
						}

						if (sigs.length < end) {
							System.err.println(sym.symbol
									+ " -- sigs.length < end: " + sigs.length);
							continue;
						}

						for (int ll = st; ll < end; ll++) {
							if (sigs[ll] != 0) {
								found = showSymbol(sym, quotes.getDate(ll), ll,
										sigs, "Tail", dir, callback);
								if (found)
									break;
							}
						}
					}
					if (found)
						continue;

					if (system.contains("elephant")) {
						sigs = checkForElephant(quotes);
						for (int ll = st; ll < end; ll++) {
							if (sigs[ll] != 0) {
								showSymbol(sym, quotes.getDate(ll), ll, sigs,
										"Elephant", dir, callback);
								found = true;
								break;
							}
						}
					}
					if (found)
						continue;

					if (system.equals("gap")) {
						sigs = checkForGap(quotes, params[0], st, end);
						for (int ll = st; ll < end; ll++) {
							if (sigs[ll] != 0) {
								found = showSymbol(sym, quotes.getDate(ll), ll,
										sigs, "gap", dir, callback);
								if (found)
									break;
							}
						}
					}
					if (found)
						continue;
					if (system.contains("trade")) {
						sigs = checkForTrade(sym.symbol);
						for (int ll = st; ll < end; ll++) {
							if (sigs[ll] != 0) {
								showSymbol(sym, quotes.getDate(ll), ll, sigs,
										"trade", dir, callback);
								found = true;
								break;
							}
						}
					}
					if (found)
						continue;
					if (system.contains("mac")) {
						sigs = checkForMac(quotes, end);
						for (int ll = st; ll < end; ll++) {
							if (sigs[ll] != 0) {
								showSymbol(sym, quotes.getDate(ll), ll, sigs,
										"mac", dir, callback);
								found = true;
								break;
							}
						}
					}
					if (found)
						continue;
					if (system.contains("rsi")) {
						sigs = checkForRSI(quotes);
						for (int ll = st; ll < end; ll++) {
							if (sigs[ll] != 0) {
								showSymbol(sym, quotes.getDate(ll), ll, sigs,
										"rsi", dir, callback);
								found = true;
								break;
							}
						}
					}
					if (found)
						continue;
					if (system.equals("rgap")) {
						sigs = checkForReversalGap(quotes, st, nd);
						for (int ll = st; ll < end; ll++) {
							if (sigs[ll] != 0) {
								showSymbol(sym, quotes.getDate(ll), ll, sigs,
										"rgap", dir, callback);
								found = true;
								break;
							}
						}
					}
					if(found) continue;
					if (system.equals("band")) {

						float[] ub = new float[quotes.size()];
						float[] lb = new float[quotes.size()];

						Calc.calcBand(quotes.getClose(), ub, lb);

						for (int ll = st; ll < end; ll++) {
							Quote q = quotes.get(ll);
							String val = sym.symbol + " ";
							if(q.low < lb[ll]) {
								val += ll + " sell put";
								found = true;
							}
							if(q.high > ub[ll]) {
								val += "-" + ll + " sell call";
								found = true;
							}
							if(found) {
								callback.onOk(val);
								break;
							}
						}
					}
					if(found) continue;
					if (system.contains("fireworks")) {
						sigs = checkForFireworks(quotes);
						for (int ll = st; ll < end; ll++) {
							if (sigs[ll] != 0) {
								showSymbol(sym, quotes.getDate(ll), ll, sigs,
										"fireworks", dir, callback);
								found = true;
								break;
							}
						}
					}
					
				} catch (Exception e) {
					System.err.println(sym.symbol + ":" + e.getMessage());
					e.printStackTrace();
					break;

				}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} finally {
			callback.onEnd();
		}

		main.setProgress(100);

		return res;
	}

	private static boolean showSymbol(Symbol sym, Date date, int ll,
			int[] sigs, String system, String dir, Callback callback) {
		if (dir.equals("up") && (sigs[ll] != buy && sigs[ll] != Scanner.bullish))
			return false;
		if (dir.equals("down") && (sigs[ll] != sell && sigs[ll] != Scanner.bearish))
			return false;

		String signal = "";
		if (sigs != null) {
			if (sigs[ll] == buy) {
				signal = "buy";
			} else {
				signal = "-- Sell";
			}
		}

		if (callback != null) {
			String data = sym.symbol + ' ' + signal + ' ' + ll + ' ' + system;
			callback.onOk(data);
			return true;
		}
		return true;
	}

	private static int[] checkForElephant(Quotes quotes) {
		int barAvgLen = 25;
		int[] sigs = new int[quotes.size()];

		for (int i = quotes.size() - (barAvgLen + 5); i >= 0; i--) {
			Quote q = quotes.get(i);
			float avgbar = 0;
			for (int j = i + 1; j < i + barAvgLen; j++) {
				Quote qj = quotes.get(j);
				avgbar += qj.high - qj.low;
			}

			avgbar /= barAvgLen;

			float curbar = q.high - q.low;

			if (curbar > avgbar * 2) {
				if (q.close > q.open) {
					sigs[i] = buy;
				} else {
					sigs[i] = sell;
				}
			}
		}
		return sigs;
	}

	static public final int buy = 1;
	static public final int bullish = 2;
	static public final int bearish = -2;
	static public final int sell = -1;

	public static int[] checkForTail(Quotes quotes) {

		int[] signals = new int[quotes.size()];

		float[] atr = Calc.calcATR(quotes, 20);

		for (int i = 1; i < quotes.size() - 22; i++) {
			Quote q = quotes.get(i);

			float rng = q.high - q.low;
			float nl = q.low;
			float nh = q.high;

			for (int j = i; j < i + 10; j++) {
				if (nl > quotes.get(j).low)
					nl = quotes.get(j).low;
				if (nh < quotes.get(j).high)
					nh = quotes.get(j).high;
			}

			if (rng > atr[i] * 1.75) {
				double bot1 = q.low + (rng * 0.4);
				double top1 = q.high - (rng * 0.4);
				if ((q.close > top1 && q.open > top1) && q.low == nl) {
					if (quotes.get(i - 1).close > q.high) {
						signals[i] = buy;
					}
				} else if ((q.close < bot1 && q.open < bot1) && q.high == nh) {
					if (quotes.get(i - 1).close < q.low)
						signals[i] = sell;
				} else {
					signals[i] = 0;
				}
			}
		}

		return signals;
	}

	public static Vector<String> getSymbols(String wl) {
		try {
			MyDatabase db = MyDatabase.db;
			Vector<Symbol> res1 = getSymbols(db, new String[] { wl });
			// db.close();
			Vector<String> res = new Vector<String>();
			for (Symbol s : res1)
				res.add(s.symbol);
			return res;
		} catch (Exception e) {
		}
		return null;
	}

	static Vector<Symbol> getSymbols(MyDatabase db, String[] watchlist)
			throws Exception {

		String in = "", comma = "";
		for (String w : watchlist) {
			in += comma + "'" + w + "'";
			comma = ",";
		}

		String sql = "Select distinct symbol, symbol_type, info from watchlist "
				+ "where list in (" + in + ") order by symbol";

		Vector<Symbol> symbols = new Vector<Symbol>();

		MyStatement st = db.execute(sql);
		while (st.next()) {
			symbols.add(new Symbol(st.getString("symbol") + " " + st.getString("info"),
					st.getString("symbol_type"), st.getString("info")));
		}
		st.close();
		return symbols;

	}

	public static Vector<String> getLists() {
		Vector<String> res = new Vector<String>();
		try {
			MyDatabase db = MyDatabase.db;
			MyStatement st = db
					.execute("select distinct list from watchlist where upper(show_flag) = 'Y' order by 1");
			while (st.next()) {
				res.add(st.getString(1));
			}
			st.close();
		} catch (Exception e) {
		}

		return res;
	}

	public static Vector<String> getSystems() {
		Vector<String> res = new Vector<String>();
		try {
			MyDatabase db = MyDatabase.db;
			MyStatement st = db
					.execute("select distinct name from setup order by 1");
			while (st.next()) {
				res.add(st.getString(1));
			}
			st.close();

		} catch (Exception e) {
		}

		return res;
	}

	static int[] checkForGap(Quotes quotes, String[] params, int st, int end) {
		int[] signals = new int[quotes.size()];

		double changePerc = (1.5 / 100.00);

		for (int i = st; i < end; i++) {
			Quote q = quotes.get(i);
			float o, h, l, c;
			o = quotes.get(i).open;
			h = quotes.get(i + 1).high;
			if (quotes.get(i).open >= quotes.get(i + 1).high * (1 + changePerc)) {
				if (params.length > 1
						&& params[1].equalsIgnoreCase("confirmed") && i > 0) {
					if (quotes.getClose(i - 1) > quotes.getHigh(i))
						signals[i] = buy;
				} else {
					signals[i] = buy;
				}
			} else if (quotes.get(i).open <= quotes.get(i + 1).low
					* (1 - changePerc)) {
				if (params.length > 1
						&& params[1].equalsIgnoreCase("confirmed") && i > 0) {
					if (quotes.getClose(i - 1) < quotes.getLow(i))
						signals[i] = sell;
				} else {
					signals[i] = sell;
				}
			}
		}

		return signals;

	}

	public static int[] checkForMac(Quotes quotes) {
		return checkForMac(quotes, quotes.size()-5);
	}
	static int[] checkForMac(Quotes quotes, int numdays) {
		int[] signals = new int[quotes.size()];
		
		if(numdays > quotes.size()) numdays = quotes.size() - 11;

		float[] ml8 = Calc.calcSMA(quotes.getLow(), 8);
		float[] mh10 = Calc.calcSMA(quotes.getHigh(), 10);

		for (int i = 0; i < numdays; i++) {
			Quote q = quotes.get(i);

			float c0 = quotes.get(i).close;
			float c1 = quotes.get(i + 1).close;
			float c2 = quotes.get(i + 2).close;
			float c3 = quotes.get(i + 3).close;

			if (c0 >= mh10[i] && c1 < mh10[i + 1] && c2 < mh10[i + 2]
					&& c3 < mh10[i + 3]) {
				signals[i] = buy;
			} else if (c0 < ml8[i] && c1 > ml8[i + 1] && c2 > ml8[i + 2]
					&& c3 > ml8[i + 3]) {
				signals[i] = sell;
			}
		}

		return signals;

	}

	static int[] checkForRSI(Quotes quotes) {
		int[] signals = new int[quotes.size()];

		float[] rsi = Calc.calcRSI(quotes.getClose(), 20);

		for (int i = 0; i < 5; i++) {
			if (rsi[i] > 70) {
				signals[i] = sell;
			} else if (rsi[i] < 30) {
				signals[i] = buy;
			}
		}

		return signals;

	}
	
	/*
	 * Check for fireworks -- 
	 * It is launched. It goes up. Makes little noise on the way up.
	 * when it reaches the top, it makes lots of noise, becomes very bright,
	 * and everyone is excited about it.
	 * What happens after that is interesting. It stops moving up, 
	 * loses its light, falls, and disappears.
	 * That is an opportunity. Let us capitalize on that. That is my
	 * income strategy.
	 * 
	 * Rules:
	 * - a trending market up/down for >= 50 days
	 * - at new high/low
	 * - reverses in force
	 * -- hvr; high volume + opposite candle
	 * -- tail
	 * -- elephant
	 * -- engulfing
	 * - high volume
	 * -- greater than yesterday's volume
	 * -- above 50 day moving average
	 * 
	 * Enter short/long
	 * Exit for 20% return.
	 * Usual risk management applies.
	 */
	public static int[] checkForFireworks(Quotes quotes) {
		int[] signals = new int[quotes.size()];
		float [] v50a = Calc.calcSMA(quotes.getVolume(), 50);

		for (int i = 0; i < quotes.size() - 55; i++) {
			// high volume
			// reversal
			Quote q1 = quotes.get(i);
			Quote q2 = quotes.get(i+1);
			Quote q50 = quotes.get(i+50);
			int sig = 0;
			if(i==134)  {
				stopForDebug();
			}
			double v50 = v50a[i] * 1.2;
			if(q1.close > q50.close) {
				// up trend
				// new high
				float nh = q1.high;
				for(int j = i; j < i+50; j++) {
					if(nh < quotes.getHigh(j)) nh = quotes.getHigh(j);
				}
				if(q1.high == nh) {
					// red candle
					if(q1.close < q1.open && 
							q1.volume >= v50 ) {
						sig = sell;
					}
				}
			} else {
				// down trend
				// new low
				float nl = q1.low;
				for(int j = i; j < i+50; j++) {
					if(nl > quotes.getLow(j)) nl = quotes.getLow(j);
				}
				if(q1.low == nl) {
					// red candle
					if(q1.close > q1.open && 
							q1.volume >= v50 ) {
						sig = buy;
					}
				}
			}
			signals[i] = sig;
		}

		return signals;

	}

	private static void stopForDebug() {
		// TODO Auto-generated method stub
		
	}

	static int[] checkForReversalGap(Quotes quotes, int sd, int nd) {
		// reversal gap:
		// new high
		// gapped up
		// close below open
		// high volume

		int[] signals = new int[quotes.size()];

		float[] ma = Calc.calcSMA(quotes.getVolume(), 50);

		for (int k = sd; k < sd + nd; k++) {

			if (quotes.getLow(k) < quotes.getHigh(k + 1))
				continue; // no gap

			if (quotes.getClose(k) > quotes.getOpen(k))
				continue; // not a reversal

			if (quotes.getVolume(k) < ma[k])
				continue; // not a strong volume

			float hh = quotes.getHigh(k);
			for (int i = k; i < k + 150; i++) {
				if (hh < quotes.getHigh(i))
					hh = quotes.getHigh(i);
			}

			if (hh != quotes.getHigh(k))
				continue; // not at new high

			// ok everything is fine
			signals[k] = sell;
		}

		return signals;

	}

	public static Vector<String> getScanResult() {
		Vector<String> result = new Vector<String>();

		try {
			String sql = "Select * from scan_result order by scan_day desc, symbol ";

			MyDatabase db = MyDatabase.db;
			MyStatement st = db.execute(sql);
			while (st.next()) {
				result.add(st.getString("symbol") + " " + st.getString("scan")
						+ " " + st.getString("scan_day"));
			}
			st.close();

//			db.disconnect();

		} catch (Exception e) {
		}

		return result;
	}
	
	static int numCompleted = 0;

	static public Vector<String> threadScan(final ValTrader main, final String system1,
			String[] watchlist, final int st, final int nd, final String dir,
			final boolean stopAtFirst, final Callback callback) {

		final int end = nd + st;

		final String[][] params = Utils.parseArgs(system1);

		try {
			MyDatabase db = MyDatabase.db;
			final Vector<Symbol> symbols = getSymbols(db, watchlist);
			
			main.progressBar.setValue(symbols.size());

			final String system = params[0][0];
			int numdays1 = 200;
			if (st + nd > numdays1)
				numdays1 = st + nd + 155;
			final int numdays = numdays1;
			int THREADS = 10;
			final int thSize = symbols.size()/THREADS + 1;
			numCompleted = 0;
			for(int i = 0; i < THREADS; i++) {
				final int ii = i;
				Thread t = new Thread() {
					public void run() {
						for(int j = thSize * ii; j < (thSize * ii) + thSize; j++) {
							if(j >= symbols.size()) return;
							numCompleted++;
							try {
								int[] sigs;
								boolean found = false;
								main.setProgress(
										(int) ((float) numCompleted / symbols.size() * 100),
										numCompleted + " / " + symbols.size());
								Symbol sym = symbols.get(j);
								Quotes quotes = Quotes.getQuotes(sym.symbol, sym.type, 0,
										numdays, "d");
								if (quotes == null)
									continue;
								if (system.contains("tail")) {
									sigs = checkForTail(quotes);
									if (sigs == null) {
										System.err.println("sigs == null");
										continue;
									}

									if (sigs.length < end) {
										System.err.println(sym.symbol
												+ " -- sigs.length < end: " + sigs.length);
										continue;
									}

									for (int ll = st; ll < end; ll++) {
										if (sigs[ll] != 0) {
											found = showSymbol(sym, quotes.getDate(ll), ll,
													sigs, "Tail", dir, callback);
											if (found)
												break;
										}
									}
								}
								if (found)
									continue;

								if (system.contains("elephant")) {
									sigs = checkForElephant(quotes);
									for (int ll = st; ll < end; ll++) {
										if (sigs[ll] != 0) {
											showSymbol(sym, quotes.getDate(ll), ll, sigs,
													"Elephant", dir, callback);
											found = true;
											break;
										}
									}
								}
								if (found)
									continue;

								if (system.equals("gap")) {
									sigs = checkForGap(quotes, params[0], st, end);
									for (int ll = st; ll < end; ll++) {
										if (sigs[ll] != 0) {
											found = showSymbol(sym, quotes.getDate(ll), ll,
													sigs, "gap", dir, callback);
											if (found)
												break;
										}
									}
								}
								if (found)
									continue;
								if (system.contains("mac")) {
									sigs = checkForMac(quotes, end);
									for (int ll = st; ll < end; ll++) {
										if (sigs[ll] != 0) {
											showSymbol(sym, quotes.getDate(ll), ll, sigs,
													"mac", dir, callback);
											found = true;
											break;
										}
									}
								}
								if (found)
									continue;
								if (system.contains("rsi")) {
									sigs = checkForRSI(quotes);
									for (int ll = st; ll < end; ll++) {
										if (sigs[ll] != 0) {
											showSymbol(sym, quotes.getDate(ll), ll, sigs,
													"rsi", dir, callback);
											found = true;
											break;
										}
									}
								}
								if (found)
									continue;
								if (system.equals("rgap")) {
									sigs = checkForReversalGap(quotes, st, nd);
									for (int ll = st; ll < end; ll++) {
										if (sigs[ll] != 0) {
											showSymbol(sym, quotes.getDate(ll), ll, sigs,
													"rgap", dir, callback);
											found = true;
											break;
										}
									}
								}
							} catch (Exception e) {
								
							}
							
						}
					}
				};
				t.start();
			}
		} catch (Exception e) {
			
		}
		return null;
	}

	
	static public int [] checkForTrade(String sym) {
		// weekly is in a a trend
		// daily gave entry signal
		int [] sigsRet = new int[200];
		try {
			Quotes w = Quotes.getQuotes(sym, "E", 0, 200, "w");
			Quotes d = Quotes.getQuotes(sym, "E", 0, 200, "d");
			
			float [] w20 = Calc.calcEMA(w.getClose(), 10);
			float [] w50 = Calc.calcEMA(w.getClose(), 20);
			
			if(w.getClose(0) >= w20[0] && w20[0] >= w50[0]) {
				// up trend
				// check for mac signal on daily
				int [] sigs = checkForMac(d, 10);
				for(int i = 0; i < 10; i++) {
					if(sigs[i] == buy) {
//						System.out.println("Buy " + sym + ": " +i);
						sigsRet[i] = buy;
					}
				}
			} else if(w.getClose(0) <= w20[0] && w20[0] <= w50[0]) {
					// up trend
					// check for mac signal on daily
					int [] sigs = checkForMac(d, 10);
					for(int i = 0; i < 10; i++) {
						if(sigs[i] == sell) {
//							System.out.println("Sell " + sym + ": " +i);
							sigsRet[i] = sell;
						}
					}
			}
			
		} catch(Exception e) {
			
		}
		return sigsRet;
	}
}

class Symbol {
	public Symbol(String symbol, String type, String info) {
		this.symbol = symbol;
		this.type = type;
		this.info = info;
	}

	String symbol, type, info;
}


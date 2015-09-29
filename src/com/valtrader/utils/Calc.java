package com.valtrader.utils;

import com.valtrader.data.Quote;
import com.valtrader.service.Quotes;

public class Calc {
	public static void calcNewHL(String wl, int [] nh, int [] nl) throws Exception {
		// wl: nas100
		// load all symbols
		// for each day, how many hit new highs and how many hit new lows
		String syms = Quotes.getSymbols("nas100");
		String [] symbols = syms.split(" ");

		for (String sym: symbols) {
 			Quotes q = Quotes.getQuotes(sym, "E", 0, 255, "d");
			for(int i = 199; i >= 0; i--) {
				// is this symbol at new high or new low today?
				float hh = q.getClose(i);
				float ll = hh;
				for(int j = i; j < i+33; j++) {
					if(hh < q.getClose(j)) hh = q.getClose(j);
					if(ll > q.getClose(j)) ll = q.getClose(j);
				}
				if(q.getClose(i) == hh) nh[i]++;
				if(q.getClose(i) == ll) nl[i]++;
			}
		}
	}
	
	
	public static float[] calcRSI(float[] prices, int length) {
		length = 14;
		// If UBound(p) < length Then Exit Sub

		float[] rsi = new float[prices.length];

		for (int i = 0; i < prices.length - length - 1; i++) {
			float u = (float) 0.000001;
			float d = (float) 0.000001;
			for (int j = i; j < i + length; j++) {
				if (prices[j] > prices[j + 1]) {
					u += prices[j + 1] - prices[j];
				}
				if (prices[j] < prices[j + 1]) {
					d += prices[j] - prices[j + 1];
				}
			}
			u /= length;
			d /= length;
			float rs = u / d;
			float rsival = 100 - (100 / (1 + rs));
			rsi[i] = rsival;
		}

		return rsi;
	}

	public static float[] calcROC(float[] prices, int length) {

		float[] rsi = new float[prices.length];

		for (int i = 0; i < prices.length - length - 1; i++) {
			float rsival = (prices[i] - prices[i + length])
					/ prices[i + length] * 100; // (close today - close n days
												// ago) / close n days ago * 100
			rsi[i] = rsival;
		}

		return rsi;
	}

	// allocate ub and lb before calling
	static public void calcBand(float[] prices, float[] ub, float[] lb) {
		int cnt = prices.length;

		float[] a22 = calcSMA(prices, 22);

		float[] sd = calcSD(prices, 22);

		for (int i = cnt - 22; i >= 0; i--) {
			ub[i] = a22[i] + (2 * sd[i]);
			lb[i] = a22[i] - (2 * sd[i]);
		}

	}

	static public void calcTurtle(Quotes quotes, int entryDays, int exitDays,
			float[] lentry, float[] lexit, float[] sentry, float[] sexit) {
		// price makes 20 day high, long entry
		// price makes 10 day low, it long exit
		// prices makes 20 day low, it is short entry
		// price makes 10 day high, it is short exit

		int max = Math.max(entryDays, exitDays);

		int size = quotes.size();
		float h20, l10, l20, h10;

		for(int i = size - max - 1; i >=0 ; i--) {
			if(i==0) {
				i=i;
			}
			h20 = quotes.getHigh(i+1);
			l20 = quotes.getLow(i+1);
			for(int j = i+1; j < i + entryDays; j++) {
				if(h20 < quotes.getHigh(j)) h20 = quotes.getHigh(j);
				if(l20 > quotes.getLow(j)) l20 = quotes.getLow(j);
			}
			h10 = quotes.getHigh(i+1);
			l10 = quotes.getLow(i+1);
			for(int j = i+1; j < i + exitDays; j++) {
				if(h10 < quotes.getHigh(j)) h10 = quotes.getHigh(j);
				if(l10 > quotes.getLow(j)) l10 = quotes.getLow(j);
			}
			lentry[i] = h20;
			lexit[i] = l10;
			sentry[i] = l20;
			sexit[i] = h10;
		}
		

	}

	static public void calcTurtle(float[] prices, float[] ub, float[] lb) {
		int cnt = prices.length;

		for (int i = cnt - 22; i >= 0; i--) {
			float h20 = prices[i];
			for (int j = i; j < i + 20; j++) {
				if (h20 < prices[j])
					h20 = prices[j];
			}
			float l10 = prices[i];
			for (int j = i; j < i + 10; j++) {
				if (l10 > prices[j])
					l10 = prices[j];
			}
			lb[i] = l10;
			ub[i] = h20;
		}

	}

	public static float[] calcSMA(float[] prices, int period) {
		int cnt = prices.length;

		float[] ma = new float[cnt];
		for (int i = cnt - 1; i >= 0; i--) {
			float a = 0;
			int per = period;
			int len = i + period;
			if (len > cnt) {
				len = cnt;
				per = cnt - i;
			}
			for (int j = i; j < len; j++) {
				a += prices[j];
			}
			ma[i] = a / per;
		}
		return ma;
	}

	public static float[] calcSMA(int[] prices, int period) {
		int cnt = prices.length;

		float[] ma = new float[cnt];
		for (int i = cnt - 1; i >= 0; i--) {
			float a = 0;
			int per = period;
			int len = i + period;
			if (len > cnt) {
				len = cnt;
				per = cnt - i;
			}
			for (int j = i; j < len; j++) {
				a += prices[j];
			}
			ma[i] = a / per;
		}
		return ma;
	}

	static float[] calcSD(float[] numbers, int period) {
		int cnt = numbers.length;

		float[] sd = new float[cnt];
		cnt -= period;

		for (int i = cnt - 1; i >= 0; i--) {
			float a = 0;
			for (int j = i; j < i + period; j++) {
				a += numbers[j];
			}
			a /= period;
			float sdv = 0;
			for (int j = i; j < i + period; j++) {
				float dev = numbers[j] - a;
				dev = dev * dev;
				sdv += dev;
			}
			sdv /= period;
			sd[i] = (float) Math.sqrt(sdv);
		}
		return sd;
	}

	public static float[] calcMACD(float[] prices) {
		// 1. Calculate a 12 day EMA of closing prices
		//
		// 2. Calculate a 26 day EMA of closing prices
		//
		// 3. Subtract the longer EMA in (2) from the shorter EMA in (1)
		//
		// 4. Calculate a 9 day EMA of the MACD line gotten in (3)

		float[] ema12 = calcEMA(prices, 12);
		float[] ema26 = calcEMA(prices, 26);

		int cnt = prices.length;

		float[] macd = new float[cnt - 26 + 1];

		for (int i = cnt - 26 - 1; i >= 0; i--) {
			macd[i] = ema12[i] - ema26[i];
		}

		return calcEMA(macd, 9);

	}

	// Nov 8, 2014
	public static float[] calcEMA(float[] prices, int period) {

		int cnt = prices.length;

		float[] ema = new float[cnt];

		float a = 0;
		for (int i = cnt - 1; i > cnt - period - 1; i--) {
			a += prices[i];
		}

		a /= period;

		for (int i = cnt - 1; i > cnt - period - 1; i--) {
			ema[i] = a;
		}
		float k = (float) 2.0 / (period + 1);

		for (int i = cnt - period - 1; i >= 0; i--) {
			ema[i] = (prices[i] * k) + (ema[i + 1] * (1 - k));
		}

		return ema;

	}

	// public static float [] calcEMA(Quotes1 quotes, int period) {
	//
	// int cnt = quotes.size();
	//
	// float [] ema = new float[cnt];
	//
	// float a = 0;
	// for(int i = cnt - 1; i > cnt - period-1; i--) {
	// // ema[i] = quotes.get(i).close;
	// a += quotes.get(i).close;
	// }
	//
	// a /= period;
	//
	// for(int i = cnt - 1; i > cnt - period; i--) {
	// ema[i] = a;
	// }
	// float k = (float)2.0/(period+1);
	//
	// for(int i = cnt - period; i >= 0; i--) {
	// ema[i] = (quotes.get(i).close * k) + (ema[i + 1] * (1 - k));
	// }
	//
	// return ema;

	// The formula for calculating EMA is as follows:
	// EMA = Price(t) * k + EMA(y) * (1 – k)
	// t = today, y = yesterday, N = number of days in EMA, k = 2/(N+1)

	// Use the following steps to calculate a 22 day EMA:

	// 1) Start by calculating k for the given timeframe. 2 / (22 + 1) = 0,0869

	// 2) Add the closing prices for the first 22 days together and divide them
	// by 22.

	// 3) You’re now ready to start getting the first EMA day by taking the
	// following day’s (day 23) closing price multiplied by k,
	// then multiply the previous day’s moving average by (1-k) and add the two.

	// 4) Do step 3 over and over for each day that follows to get the full
	// range of EMA.

	// To give you an algorithmic view on how this can be accomplished, see
	// below.

	// public float CalculateEMA(float todaysPrice, float numberOfDays, float
	// EMAYesterday){
	// float k = 2 / (numberOfDays + 1);
	// return todaysPrice * k + EMAYesterday * (1 – k);
	// }

	// This method would typically be called from a loop through your data,
	// looking something like this:
	// foreach (DailyRecord sdr in DataRecords){
	// //call the EMA calculation
	// ema = CalculateEMA(sdr.Close, numberOfDays, yesterdayEMA);
	// //put the calculated ema in an array
	// m_emaSeries.Items.Add(sdr.TradingDate, ema);
	// //make sure yesterdayEMA gets filled with the EMA we used this time
	// around
	// yesterdayEMA = ema;
	// }

	// Note that this is psuedo code. You would typically need to send the
	// yesterday CLOSE value as
	// yesterdayEMA until the yesterdayEMA is calculated from today’s EMA.
	// That’s happening only
	// after the loop has run more days than the number of days you have
	// calculated your EMA for.
	//
	// For a 22 day EMA, it’s only on the 23 time in the loop and thereafter
	// that the
	// yesterdayEMA = ema is valid. This is no big deal, since you will need
	// data from at least 100
	// trading days for a 22 day EMA to be valid.

	// }

	public static float[] calcATR(Quotes quotes, int nd) {
		return calcATR(quotes, nd, false);
	}

	public static float[] calcATR(Quotes quotes, int nd, boolean isavg) {
		// if (ind.length > 1) {
		// if (ind[1].equals("%"))
		// val = atr[i] / quotes.getClose(i) * 100;
		// }

		int cnt = quotes.size();

		if (cnt < nd + 1)
			return null;

		float[] atr = new float[cnt];
		for (int i = cnt - nd - 1; i >= 0; i--) {
			Quote q1 = quotes.get(i);

			// Wilder started with a concept called True Range (TR), which is
			// defined as the greatest of the following:
			// Method 1: Current High less the current Low
			// Method 2: Current High less the previous Close (absolute value)
			// Method 3: Current Low less the previous Close (absolute value)

			float a = 0;
			for (int j = i; j < i + nd; j++) {
				Quote q2 = quotes.get(j);
				float rng = q2.high - q2.low;
				float range2 = Math.abs(q2.high - quotes.get(j + 1).close);
				float range3 = Math.abs(q2.low - quotes.get(j + 1).close);
				if (rng < range2)
					rng = range2;
				if (rng < range3)
					rng = range3;
				a += rng;
				Quote q3 = quotes.get(j + 1);
				float tr = Math.max(q2.high - q2.low,
						Math.abs(q2.high - q3.close));
				tr = Math.max(tr, q2.low - q3.close);
				// a += tr;

			}
			atr[i] = a / nd;
			if (isavg)
				atr[i] = atr[i] / q1.close * 100;
		}
		float saved = atr[cnt - nd - 1];
		for (int i = cnt - nd - 1; i < cnt; i++)
			atr[i] = saved;
		return atr;
	}

	public static float[] calcTR(Quotes quotes) {

		int cnt = quotes.size();
		float a = 0;
		float[] tr = new float[cnt];
		for (int i = 0; i < cnt - 1; i++) {
			if (i == 21) {
				System.err.println("Stop");
			}

			Quote q1 = quotes.get(i);
			// Wilder started with a concept called True Range (TR), which is
			// defined as the greatest of the following:
			// Method 1: Current High less the current Low
			// Method 2: Current High less the previous Close (absolute value)
			// Method 3: Current Low less the previous Close (absolute value)

			Quote q2 = quotes.get(i + 1);
			float rng = q2.high - q2.low;
			float range2 = Math.abs(q1.high - q2.close);
			float range3 = Math.abs(q1.low - q2.close);
			if (rng < range2)
				rng = range2;
			if (rng < range3)
				rng = range3;
			if (i <= 21) {
				a += rng;
				System.err.println(rng + ":" + a);
			}
			tr[i] = rng;

		}
		// ValTrader.msgBox("avg:"+a/22);
		return tr;
	}

	public static float[][] calcStoc(Quotes quotes, int width) {
		// %K = (Current Close - Lowest Low)/(Highest High - Lowest Low) * 100
		// %D = 3-day SMA of %K
		//
		// Lowest Low = lowest low for the look-back period
		// Highest High = highest high for the look-back period
		// %K is multiplied by 100 to move the decimal point two places
		int cnt = quotes.size();

		if (cnt < width + 1)
			return null;

		float[] slow = new float[cnt];
		float[] fast = new float[cnt];

		for (int i = cnt - width; i >= 0; i--) {
			float lowN = quotes.getLow(i);
			float highN = quotes.getHigh(i);
			for (int j = i; j < i + width; j++) {
				if (lowN > quotes.getLow(j))
					lowN = quotes.getLow(j);
				if (highN < quotes.getHigh(j))
					highN = quotes.getHigh(j);
			}
			fast[i] = (quotes.getClose(i) - lowN) / (highN - lowN) * 100; // %K
		}

		for (int i = quotes.size() - width - 3; i >= 0; i--) {
			slow[i] = (fast[i] + fast[i + 1] + fast[i + 2]) / 3; // %D
		}

		return new float[][] { slow, fast };
	}
}

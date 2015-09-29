package com.valtrader.utils;

import com.valtrader.data.Quote;
import com.valtrader.service.Quotes;
import com.valtrader.service.Scanner;
import com.valtrader.ui.ValTrader;

/**
 * CandleStick utilities.
 * Check for reversal and continuation patterns
 *  
 */

public class Candle {
	static void stop() {
//		System.out.println("Stopping for debug");
	}
	
	static final String ALL_SIGNALS = "engulfing,harami,tail,elephant,3 soldiers,3 crows,abandoned baby,dark cloud,squeeze,thursting"; 
	public static void check(Quotes quotes, int [] sigs, String [] signotes, String what, int direction) {
		// what: comma separated signal names
		// direction: Scanner.buy, sell etc
		
		int size = quotes.size();
		
		float[] atr = Calc.calcATR(quotes, 20);

		for(int i = 0; i < size - 10; i++) {
			if(i==3) stop();
			Quote q1 = quotes.get(i);
			Quote q2 = quotes.get(i+1);
			Quote q3 = quotes.get(i+2);
			int sig = 0;
			MyStringBuffer notes = new MyStringBuffer();
			// bullish engufling

			if(dark(q2) && white(q1) && 
					engulf(q2, q1, atr[i]) && q1.open < q2.low) {
				sig = checkSignal(what, direction, "engulf",Scanner.bullish);
				notes.append("Engulf");
			}
			// bearish engulfing
			if(white(q2) && dark(q1) && 
					engulf(q2, q1, atr[i]) && q1.open >= q2.high) {
				sig = checkSignal(what, direction, "engulf",Scanner.bearish);
				notes.append("Engulf");
			}
			
			// bullish harami
			
			if(dark(q2) && white(q1) && 
					engulf(q1, q2, atr[i+1])) {
				sig = checkSignal(what, direction, "harami",Scanner.bullish);
				notes.append("harami");
			}
			
			// bearish harami
			if(white(q2) && dark(q1) && 
					engulf(q1, q2, atr[i+1])) {
				sig = checkSignal(what, direction, "harami",Scanner.bearish);
				notes.append("harami");
			}

						
			// check for tail

			int sig1 = checkTail(quotes, i, atr);
			if(sig1 != 0) {
				sig = checkSignal(what, direction, "tail",sig1);
				notes.append("Tail");
			}
			// meeting line
//			if(dark(q2) && white(q1) && q1.open < q2.low && q1.close >= q2.close) {
//				sig = Scanner.bullish;
//				notes.append("Meeting line");
//			}
//			
//			if(white(q2) && dark(q1) && q1.open > q2.high && q1.close <= q2.close) {
//				sig = Scanner.bearish;
//				notes.append("Meeting line");
//			}
//			
//			// strength
//			if(dark(q2) && white(q1) && q1.close > q2.high) {
//				sig = Scanner.bullish;
//				notes.append("Continue");
//			}
//			
//			if(white(q2) && dark(q1) && q1.close < q2.low) {
//				sig = Scanner.bearish;
//				notes.append("Continue");
//			}

			// 3 soldiers
			
			if(white(q1) && white(q2) && white(q3) && 
					q1.open > q2.open && q1.close > q2.close && q1.low > q2.low && q1.high > q2.high && 
					q2.open > q3.open && q2.close > q3.close && q2.low > q3.low && q2.high > q3.high) {
				sig = checkSignal(what, direction, "3soldiers",Scanner.bullish);
				int ns = numSoldiers(quotes,i);
				notes.append(ns + " soldiers");
			}
			
			// 3 crows
			
			if(dark(q1) && dark(q2) && dark(q3) && 
					q1.open < q2.open && q1.close < q2.close && q1.low < q2.low && q1.high < q2.high && 
					q2.open < q3.open && q2.close < q3.close && q2.low < q3.low && q2.high < q3.high) {
				sig = checkSignal(what, direction, "3crows",Scanner.bearish);
				int nc = numCrows(quotes, i);
				notes.append(nc + " crows");
			}
			

			// abandoned baby
			if(dark(q3) && white(q1) && 
					q2.high < q3.low && 
					q1.low > q2.high && 
					(q2.high - q2.low) < (q3.high-q3.low)) {
				sig = checkSignal(what, direction, "abandoned baby",Scanner.bullish);
				notes.append("Abandoned baby");
			}
			
			if(white(q3) && dark(q1) && 
					q2.low > q3.high && 
					q1.high < q2.low && 
					(q2.high - q2.low) < (q3.high-q3.low)) {
				sig = checkSignal(what, direction, "abandoned baby",Scanner.bullish);
				notes.append("Abandoned baby");
			}
			
			// general abadoned
			if(downGap(q3, q2) && upGap(q2, q1)) {
				sig = checkSignal(what, direction, "abandoned baby",Scanner.bullish);
				notes.append("Abandoned baby");
				
			}

			if(upGap(q3, q2) && downGap(q2, q1)) {
				sig = checkSignal(what, direction, "abandoned baby",Scanner.bearish);
				notes.append("Abandoned baby");

			}

			
			// dark cloud cover reversal
			if(white(q2) && dark(q1) && 
					 largeBody(quotes, i+1, atr) && largeBody(quotes, i, atr) &&
					q1.open > q2.close && q1.close < (q2.open+q2.close)/2
					) {
				sig = checkSignal(what, direction, "dark cloud",Scanner.bearish);
				notes.append("Dark Could");
			}
			// squeeze day
			if(longCandle(quotes, i+2, atr) && dark(q3) && inside(q3,q2) && inside(q2, q1)) {
				sig = checkSignal(what, direction, "squeeze",Scanner.bullish);
				notes.append("Squeeze");
			}
			
			if(longCandle(quotes, i+2, atr) && white(q3) && inside(q3,q2) && inside(q2, q1)) {
				sig = checkSignal(what, direction, "squeeze",Scanner.bearish);
				notes.append("Squeeze");
			}

			if(longCandle(quotes, i+1, atr) && white(q2) && 
					upGap(q2, q1) && dark(q1) && 
					q1.close > (q2.low+q2.high)/2) {
				sig = checkSignal(what, direction, "thursting",Scanner.bullish);
				notes.append("Thursting");
			}
			
			if(longCandle(quotes, i+1, atr) && dark(q2) && 
					downGap(q2, q1) && white(q1) && 
					q1.close < (q2.low+q2.high)/2) {
				sig = checkSignal(what, direction, "thursting",Scanner.bearish);
				notes.append("Thursting");
			}
			sigs[i] = sig;
			signotes[i] = notes.toString();			
		}
	}

	static int checkSignal(String what, int direction, String signalName, int signal) {
		if(what == null) {
			if(direction == 0) return signal;
			if(direction == signal) return signal;
			return 0;
		}
		String [] sigs = what.split(",");
		for(String s: sigs) {
			if(s.equalsIgnoreCase(signalName)) {
				// yes, this is what I am looking for
				if(direction == 0) return signal;
				if(direction == signal) return signal;
				return 0;
			}
		}

		return 0;
	}

	static boolean largeBody(Quotes q, int day, float [] atr) {
		return longCandle(q, day, atr) && 
				Math.abs(q.getClose(day) - q.getOpen(day)) >= 
					((q.getHigh(day) - q.getLow(day)) * 0.75);
		
	}
	static boolean longCandle(Quotes quotes, int day, float [] atr) {
		return Math.abs(quotes.getClose(day) - quotes.getOpen(day)) > atr[day] * 1.5;
	}
	
	static boolean inside(Quote q2, Quote q1) {
		// Is q1 an inside day of q2?
		return q1.high < q2.high && q1.low > q2.low;
	}
	static boolean downGap(Quote q2, Quote q1) {
		return q1.high < q2.low;
	}

	static boolean upGap(Quote q2, Quote q1) {
		return q1.low > q2.high;
	}
	static int checkTail(Quotes quotes, int i, float [] atr) {
		
		if(i > quotes.size() - 20) return 0;
		
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
					return Scanner.bullish;
				}
			} else if ((q.close < bot1 && q.open < bot1) && q.high == nh) {
				if (quotes.get(i - 1).close < q.low)
					return Scanner.bearish;
			}
		}
		
		return 0;
	}

	
	static boolean dark(Quote q) {
		return q.close < q.open;
	}
	static boolean white(Quote q) {
		return q.close > q.open;
	}

	static boolean engulf(Quote setup, Quote signal, float atr) {
		// check if signal day engulfs setup day
		
		float setupLow = Math.min(setup.open, setup.close);
		float setupHigh = Math.max(setup.open, setup.close);
		
		float signalLow = Math.min(signal.open, signal.close);
		float signalHigh = Math.max(signal.open, signal.close);
		
		return signalLow < setupLow && signalHigh > setupHigh && signal.low < setup.low && signal.high > setup.high && (signal.high-signal.low) >= atr * 1.0;
		
	}
	
	static int numSoldiers(Quotes quotes, int day) {
		int ns = 1;
		
		for(int i = day; i < quotes.size() - 1; i++) {
			Quote q1 = quotes.get(i);
			Quote q2 = quotes.get(i+1);
			if(white(q1) && white(q2) && 
					q1.open > q2.open && q1.close > q2.close && q1.low > q2.low && q1.high > q2.high) {
				ns++;
			} else {
				break;
			}
		}
			
		return ns;
	}

	static int numCrows(Quotes quotes, int day) {
		int ns = 1;
		
		for(int i = day; i < quotes.size() - 1; i++) {
			Quote q1 = quotes.get(i);
			Quote q2 = quotes.get(i+1);
			if(dark(q1) && dark(q2) && 
					q1.open < q2.open && q1.close < q2.close && q1.low < q2.low && q1.high < q2.high) {
				ns++;
			} else {
				break;
			}
		}
			
		return ns;
	}

}

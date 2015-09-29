package com.valtrader.ui;

import java.sql.Date;


public class Wave {
	int idx = -1;
	WaveValue [] waveValues = new WaveValue[6];
	public Wave() {
		
	}
	
	public void reset() {
		idx = -1;
		for(int i = 0; i < waveValues.length; i++) {
			waveValues[i] = null;
		}
	}
	
	float [] getTargets() {
		
		float [] w = new float[waveValues.length];
		if(idx < 1) return null;
		boolean uptrend = waveValues[1].low > waveValues[0].low;
		
		w[0] = uptrend ? waveValues[0].low : waveValues[0].high;
		w[1] = uptrend ? waveValues[1].high : waveValues[1].low;
		if(waveValues[2] != null) w[2] = uptrend ? waveValues[2].low : waveValues[2].high;
		if(waveValues[3] != null) w[3] = uptrend ? waveValues[3].high : waveValues[3].low;
		if(waveValues[4] != null) w[4] = uptrend ? waveValues[4].low : waveValues[4].high;
		if(waveValues[5] != null) w[5] = uptrend ? waveValues[5].high : waveValues[5].low;
		
		float [] targets = new float[5];

		if (w[0] == 0)
			return null;
		if (w[1] == 0)
			return null;

		// calc wave 2
		float w1 = Math.abs(w[1] - w[0]);

		w1 *= (float) 0.43;
		float t = uptrend ? w[1] - w1 : w[1] + w1;

		targets[0] = t;

		// calc wave 3
		// w1 = Math.Abs(wavePrices(1) - wavePrices(0))
		// w2 = Math.Abs(wavePrices(2) - wavePrices(1))
		// pw2 = wavePrices(2)
		// If up_trend Then
		// ' w3 = pw2 + w1 * 1.58
		// w3 = pw2 + w1 ' using 100% move up
		// priceTarget(0) = w3
		// ' w3 = pw2 + w2 * 3.66
		// w3 = pw2 + w1 * 1.58 ' using 100% move up
		// priceTarget(1) = w3
		// Else
		// w3 = pw2 - w1 * 1.19
		// priceTarget(0) = w3
		// w3 = pw2 - w2 * 2.75
		// priceTarget(1) = w3
		// End If
		if (w[2] == 0)
			return targets;
		w1 = Math.abs(w[1] - w[0]);
		float w2 = Math.abs(w[2] - w[1]);

		if (uptrend) {
			// using 100% of wave 1
			t = w[2] + w1;
			targets[0] = t;
			t = w[2] + w1 * (float) 1.58;
			targets[1] = t; 
		} else {
			t = w[2] - w1 * (float) 1.19;
			targets[0] = t;
			t = w[2] - w2 * (float) 2.75;
			targets[1] = t;
		}

		// calc wave 4
		/*
		 * If WaveId = 4 Then w1 = Math.Abs(wavePrices(1) - wavePrices(0)) w2 =
		 * Math.Abs(wavePrices(2) - wavePrices(1)) w3 = Math.Abs(wavePrices(3) -
		 * wavePrices(2))
		 * 
		 * If up_trend Then w4 = wavePrices(3) - w1 * 0.45 priceTarget(0) = w4
		 * w4 = wavePrices(3) - w2 * 1.02 priceTarget(1) = w4 w4 = wavePrices(3)
		 * - w3 * 0.29 priceTarget(2) = w4
		 * 
		 * Else w4 = wavePrices(3) + w1 * 0.32 priceTarget(0) = w4 w4 =
		 * wavePrices(3) + w2 * 0.72 priceTarget(1) = w4 w4 = wavePrices(3) + w3
		 * * 0.27 priceTarget(2) = w4 End If
		 */
		if (w[3] == 0)
			return targets;
		float w3 = Math.abs(w[3] - w[2]);
		if (uptrend) {
			t = w[3] - w1 * (float) 0.45;
			targets[0] = t;
			t = w[3] - w2 * (float) 1.02;
			targets[1] = t;
			t = w[3] - w3 * (float) 0.29;
			targets[2] = t;

		} else {
			t = w[3] + w1 * (float) 0.32;
			targets[0] = t;
			t = w[3] + w2 * (float) 0.72;
			targets[1] = t;
			t = w[3] + w3 * (float) 0.27;
			targets[2] = t;
		}

		// calc wave 5
		/*
		 * If WaveId = 5 Then w1 = Math.Abs(wavePrices(1) - wavePrices(0)) w2 =
		 * Math.Abs(wavePrices(2) - wavePrices(1)) w3 = Math.Abs(wavePrices(3) -
		 * wavePrices(2)) w4 = Math.Abs(wavePrices(4) - wavePrices(3))
		 * 
		 * If up_trend Then w5 = wavePrices(4) + w1 * 1.1 priceTarget(0) = w5 w5
		 * = wavePrices(4) + w2 * 2.58 priceTarget(1) = w5 w5 = wavePrices(4) +
		 * w3 * 0.7 priceTarget(2) = w5 w5 = wavePrices(4) + w4 * 2.51
		 * priceTarget(3) = w5 Else w5 = wavePrices(4) - w1 * 0.7 priceTarget(0)
		 * = w5 w5 = wavePrices(4) - w2 * 1.65 priceTarget(1) = w5 w5 =
		 * wavePrices(4) - w3 * 0.59 priceTarget(2) = w5 w5 = wavePrices(4) - w4
		 * * 2.28 priceTarget(3) = w5 End If
		 */

		if (w[4] == 0)
			return targets;
		float w4 = Math.abs(w[4] - w[3]);

		if (uptrend) {
			t = w[4] + w1 * (float) 1.1;
			targets[0] = t;
			t = w[4] + w2 * (float) 2.58;
			targets[1] = t;
			t = w[4] + w3 * (float) 0.7;
			targets[2] = t;
			t = w[4] + w4 * (float) 2.51;
			targets[3] = t;
		} else {
			t = w[4] - w1 * (float) 0.7;
			targets[0] = t;
			t = w[4] - w2 * (float) 1.65;
			targets[1] = t;
			t = w[4] - w3 * (float) 0.59;
			targets[2] = t;
			t = w[4] - w4 * (float) 2.28;
			targets[3] = t;
		}

		// calc wave II
		if (w[5] == 0)
			return targets;

		w1 = Math.abs(w[5] - w[0]);

		w1 *= (float) 0.43;
		t = uptrend ? w[5] - w1 : w[5] + w1;

		targets[0] = t;

		return targets;
	}

	public void add(Date dd, float low, float high) {
		if(idx >= 5) return;
		idx++;
		waveValues[idx] = new WaveValue(dd, low, high);
	}

	class WaveValue {
		Date dt;
		float low, high;
		WaveValue(Date dt, float low, float high) {
			this.dt = dt;
			this.low = low;
			this.high = high;
		}
	}

	long getDate(int i) {
		return waveValues[i].dt.getTime();
	}
	float getValue(int i) {
		if(waveValues[i] == null) return 0;
		if(waveValues[1] == null) return waveValues[i].low;
		boolean uptrend = waveValues[1].low > waveValues[0].low;
		switch(i) {
		case 0:
			return uptrend ? waveValues[i].low : waveValues[i].high;
		case 1:
			return uptrend ? waveValues[i].high : waveValues[i].low;
		case 2:
			return uptrend ? waveValues[i].low : waveValues[i].high;
		case 3:
			return uptrend ? waveValues[i].high : waveValues[i].low;
		case 4:
			return uptrend ? waveValues[i].low : waveValues[i].high;
		case 5:
			return uptrend ? waveValues[i].high : waveValues[i].low;
		}
		return waveValues[i].low;
	}

	public int size() {
		int i = 0;
		for(i = 0; i < waveValues.length; i++) {
			if(waveValues[i] == null) break;
		}
		return i;
	}

	public void back() {
		if(idx == -1) return;
		waveValues[idx] = null;
		idx--;
	}

	public void add(long int1, float float1) {
		waveValues[++idx] = new WaveValue(new Date(int1), float1, float1);
	}
}

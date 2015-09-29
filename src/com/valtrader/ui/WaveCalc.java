package com.valtrader.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RefineryUtilities;

import com.valtrader.utils.Utils;

public class WaveCalc extends JDialog {
	final JTextField[] waves = new JTextField[7];
	JLabel[][] targets = new JLabel[7][];

	CalcChart chart;

	public WaveCalc(final ValTrader app) {
		super(app);
		setTitle("Valtrader - Elliott Wave Calculator");
		setLocationRelativeTo(app);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(chart = new CalcChart(), BorderLayout.CENTER);
		
		JButton btnNew = new JButton("New");
		btnNew.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				for(JTextField tf: waves) tf.setText("");
				// clear all targets
				if (targets != null) {
					for (JLabel[] lbls : targets) {
						if (lbls != null)
							for (JLabel lbl : lbls) {
								if (lbl != null)
									lbl.setText("");
							}
					}
				}
				chart.clear();
			}
			
		});

		mainPanel.add(btnNew, BorderLayout.SOUTH);

		JPanel calcPanel = new JPanel();
		calcPanel.setLayout(new MigLayout());
		// add(calcPanel);
		mainPanel.add(calcPanel, BorderLayout.NORTH);

		calcPanel.add(new JLabel(""), "gap para");
		for (int i = 0; i < 7; i++) {
			String what = "gap para";
			// if(i == 6) what = "wrap";
			String waveId = (i == 6) ? "II" : i + "";
			calcPanel.add(new JLabel("Wave " + waveId), what);
		}
		calcPanel.add(new JLabel(""), "wrap");
		calcPanel.add(new JLabel(""), "gap para");
		for (int i = 0; i < 7; i++) {
			final JTextField tf = new JTextField("", 10);
			calcPanel.add(tf);
			if (i == 6)
				tf.setEditable(false);
			waves[i] = tf;
			tf.addFocusListener(new FocusListener() {

				@Override
				public void focusGained(FocusEvent arg0) {
					tf.selectAll();
					calcWave();
				}

				@Override
				public void focusLost(FocusEvent arg0) {

				}

			});
		}

		calcPanel.add(new JLabel(""), "wrap");

		for (int i = 2; i < 7; i++) {
			String waveId = (i == 6) ? "II" : i + "";
			calcPanel.add(new JLabel("Target " + waveId), "gap para");
			targets[i] = new JLabel[7];
			for (int j = 0; j < 7; j++) {
				JLabel tgt = new JLabel("");
				calcPanel.add(tgt, "gap para");
				targets[i][j] = tgt;
			}
			calcPanel.add(new JLabel(""), "wrap");
		}
		add(mainPanel);

		setPreferredSize(new Dimension(600, 500));
//		RefineryUtilities.centerFrameOnScreen(this);
		setLocation(300, 200);

		pack();
	}

	void calcWave() {

		chart.showChart(waves);

		// clear all targets
		if (targets != null) {
			for (JLabel[] lbls : targets) {
				if (lbls != null)
					for (JLabel lbl : lbls) {
						if (lbl != null)
							lbl.setText("");
					}
			}
		}

		float[] w = new float[6];
		for (int i = 0; i < 6; i++) {
			w[i] = getValue(waves[i]);
		}

		if (w[0] == 0)
			return;
		if (w[1] == 0)
			return;

		boolean uptrend = (w[1] > w[0]);

		// calc wave 2
		float w1 = Math.abs(w[1] - w[0]);

		w1 *= (float) 0.43;
		float t = uptrend ? w[1] - w1 : w[1] + w1;

		// target[waveX][usingWaveY] = calculatedValue;

		targets[2][1].setText(Utils.myFormat(t));

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
			return;
		w1 = Math.abs(w[1] - w[0]);
		float w2 = Math.abs(w[2] - w[1]);

		if (uptrend) {
			// using 100% of wave 1
			t = w[2] + w1;
			targets[3][1].setText(Utils.myFormat(t)); // wave 3 calculated using
														// wave 1
			t = w[2] + w1 * (float) 1.58;
			targets[3][2].setText(Utils.myFormat(t)); // wave 3 calculated using
														// wave 2
		} else {
			t = w[2] - w1 * (float) 1.19;
			targets[3][1].setText(Utils.myFormat(t));
			t = w[2] - w2 * (float) 2.75;
			targets[3][2].setText(Utils.myFormat(t));
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
			return;
		float w3 = Math.abs(w[3] - w[2]);
		if (uptrend) {
			t = w[3] - w1 * (float) 0.45;
			targets[4][1].setText(Utils.myFormat(t));
			t = w[3] - w2 * (float) 1.02;
			targets[4][2].setText(Utils.myFormat(t));
			t = w[3] - w3 * (float) 0.29;
			targets[4][3].setText(Utils.myFormat(t));

		} else {
			t = w[3] + w1 * (float) 0.32;
			targets[4][1].setText(Utils.myFormat(t));
			t = w[3] + w2 * (float) 0.72;
			targets[4][2].setText(Utils.myFormat(t));
			t = w[3] + w3 * (float) 0.27;
			targets[4][3].setText(Utils.myFormat(t));
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
			return;
		float w4 = Math.abs(w[4] - w[3]);

		if (uptrend) {
			t = w[4] + w1 * (float) 1.1;
			targets[5][1].setText(Utils.myFormat(t));
			t = w[4] + w2 * (float) 2.58;
			targets[5][2].setText(Utils.myFormat(t));
			t = w[4] + w3 * (float) 0.7;
			targets[5][3].setText(Utils.myFormat(t));
			t = w[4] + w4 * (float) 2.51;
			targets[5][4].setText(Utils.myFormat(t));
		} else {
			t = w[4] - w1 * (float) 0.7;
			targets[5][1].setText(Utils.myFormat(t));
			t = w[4] - w2 * (float) 1.65;
			targets[5][2].setText(Utils.myFormat(t));
			t = w[4] - w3 * (float) 0.59;
			targets[5][3].setText(Utils.myFormat(t));
			t = w[4] - w4 * (float) 2.28;
			targets[5][4].setText(Utils.myFormat(t));
		}

		// calc wave II
		if (w[5] == 0)
			return;

		w1 = Math.abs(w[5] - w[0]);

		w1 *= (float) 0.43;
		t = uptrend ? w[5] - w1 : w[5] + w1;

		targets[6][5].setText(Utils.myFormat(t));
		waves[6].setText(Utils.myFormat(t));

	}

	float getValue(JTextField tf) {
		if (tf.getText().isEmpty())
			return 0;
		return Float.parseFloat(tf.getText());
	}



/*
 * 
 * Private Sub wavePrices_LostFocus(WaveId As Integer) w1 = wavePrices(1) w0 =
 * wavePrices(0) up_trend = w1 > w0 If WaveId = 2 Then w1 =
 * Math.Abs(wavePrices(1) - wavePrices(0)) w2 = IIf(up_trend, wavePrices(1) - w1
 * * 0.43, wavePrices(1) + w1 * 0.43) priceTarget(0) = w2
 * 
 * ElseIf WaveId = 3 Then w1 = Math.Abs(wavePrices(1) - wavePrices(0)) w2 =
 * Math.Abs(wavePrices(2) - wavePrices(1)) pw2 = wavePrices(2) If up_trend Then
 * ' w3 = pw2 + w1 * 1.58 w3 = pw2 + w1 ' using 100% move up priceTarget(0) = w3
 * ' w3 = pw2 + w2 * 3.66 w3 = pw2 + w1 * 1.58 ' using 100% move up
 * priceTarget(1) = w3 Else w3 = pw2 - w1 * 1.19 priceTarget(0) = w3 w3 = pw2 -
 * w2 * 2.75 priceTarget(1) = w3 End If ElseIf WaveId = 4 Then w1 =
 * Math.Abs(wavePrices(1) - wavePrices(0)) w2 = Math.Abs(wavePrices(2) -
 * wavePrices(1)) w3 = Math.Abs(wavePrices(3) - wavePrices(2)) If up_trend Then
 * w4 = wavePrices(3) - w1 * 0.45 priceTarget(0) = w4 w4 = wavePrices(3) - w2 *
 * 1.02 priceTarget(1) = w4 w4 = wavePrices(3) - w3 * 0.29 priceTarget(2) = w4
 * Else w4 = wavePrices(3) + w1 * 0.32 priceTarget(0) = w4 w4 = wavePrices(3) +
 * w2 * 0.72 priceTarget(1) = w4 w4 = wavePrices(3) + w3 * 0.27 priceTarget(2) =
 * w4 End If ElseIf WaveId = 5 Then w1 = Math.Abs(wavePrices(1) - wavePrices(0))
 * w2 = Math.Abs(wavePrices(2) - wavePrices(1)) w3 = Math.Abs(wavePrices(3) -
 * wavePrices(2)) w4 = Math.Abs(wavePrices(4) - wavePrices(3)) If up_trend Then
 * w5 = wavePrices(4) + w1 * 1.1 priceTarget(0) = w5 w5 = wavePrices(4) + w2 *
 * 2.58 priceTarget(1) = w5 w5 = wavePrices(4) + w3 * 0.7 priceTarget(2) = w5 w5
 * = wavePrices(4) + w4 * 2.51 priceTarget(3) = w5 Else w5 = wavePrices(4) - w1
 * * 0.7 priceTarget(0) = w5 w5 = wavePrices(4) - w2 * 1.65 priceTarget(1) = w5
 * w5 = wavePrices(4) - w3 * 0.59 priceTarget(2) = w5 w5 = wavePrices(4) - w4 *
 * 2.28 priceTarget(3) = w5 End If Else
 * 
 * If WaveId = 6 Then ' I am going to assume wave0-wave5 as wave1 of bigger
 * degree and calculate wave6 (as wave2) w1 = Math.Abs(wavePrices(5) -
 * wavePrices(0)) priceTarget(4) = IIf(up_trend, wavePrices(5) - w1 * 0.43,
 * wavePrices(5) + w1 * 0.43) End If End Sub
 */

class CalcChart extends ChartPanel {
	Color bgColor = Color.white;
	Color gridColor = Color.lightGray;
	boolean uptrend = true; // let us be positive.
	
	public CalcChart() {
		super(null);

		NumberAxis rangeAxis = new NumberAxis();
		rangeAxis.setAutoRange(true);
		rangeAxis.setAutoRangeIncludesZero(false);
		final CategoryPlot pricePlot = new CategoryPlot(null, new CategoryAxis(
				""), rangeAxis, new LineAndShapeRenderer() {
			@Override
			public Paint getItemPaint(int row, int col) {
				if(!uptrend) return (col % 2 == 0) ? Color.blue : Color.red;
				return (col % 2 == 0) ? Color.red : Color.blue;
			}
		});

//		pricePlot.setBackgroundPaint(bgColor);
		pricePlot.setDomainGridlinePaint(Color.blue);
		pricePlot.setRangeGridlinePaint(gridColor);
		pricePlot.setDomainGridlinesVisible(true);
		pricePlot.setRangePannable(true);

		pricePlot.setRangeAxisLocation(AxisLocation.TOP_OR_RIGHT);
		JFreeChart chart = new JFreeChart("Wave Chart", JFreeChart.DEFAULT_TITLE_FONT,
				pricePlot, false);

		this.setChart(chart);
	}

	public void clear() {
	}

	public void showChart(JTextField[] waves) {
		if (waves[0].getText().isEmpty())
			return;
		CategoryDataset waveSeries = getWaveSeries(waves);

		JFreeChart chart = getChart();

		CategoryPlot plot = (CategoryPlot) chart.getPlot();

		plot.setDataset(waveSeries);
		float p = Float.parseFloat(waves[0].getText());

		String decFormat = (p < 2) ? "0.0000" : ((p > 200) ? "0" : "0.00");

		((NumberAxis) plot.getRangeAxis())
				.setNumberFormatOverride(new DecimalFormat(decFormat));

	}

	CategoryDataset getWaveSeries(JTextField[] waves) {
		final DefaultCategoryDataset result = new DefaultCategoryDataset();
		final String series1 = "s1";
		for (int i = 0; i < waves.length; i++) {
			if (waves[i].getText().isEmpty())
				break;
			float f = Float.parseFloat(waves[i].getText());
			result.addValue(f, series1, "Wave " + i);
			if(i > 1) {
				float w0 = Float.parseFloat(waves[0].getText());
				float w1 = Float.parseFloat(waves[1].getText());
				uptrend = w1 > w0;
			}
		}
		return result;
	}

}

}

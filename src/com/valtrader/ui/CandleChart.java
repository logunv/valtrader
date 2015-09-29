package com.valtrader.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import javax.swing.JDialog;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.general.AbstractSeriesDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.jfree.data.xy.OHLCDataset;

import com.valtrader.data.Quote;
import com.valtrader.service.Quotes;

public class CandleChart extends JDialog {
	static CChart chart = null;

	public CandleChart(final ValTrader app) {
		super(app);
		setTitle("CandleChart");
		setLocationRelativeTo(app);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(chart = new CChart(), BorderLayout.CENTER);

		add(mainPanel);

		setPreferredSize(new Dimension(300, 300));
		setLocation(20, 90);

		pack();
	}
	
	public static void showCandle(Quotes quotes, int day) {
		try {
			if(chart != null) chart.showChart(quotes, day);
		}catch(Exception e){}
	}


	public class CChart extends ChartPanel {
		Color gridColor = Color.lightGray;
		Color upColor = new Color(0, 0, 204); // Color.blue;
		Color downColor = Color.red;
		Color bgColor = Color.white;

		CandlestickRenderer candleRenderer;

		public CChart() {
			super(null);

			// 1. candle renderer
			candleRenderer = new CandlestickRenderer() {
				public Paint getItemOutlinePaint(int row, int column) {
					OHLCDataset dataset = (OHLCDataset) getPlot().getDataset();

					double open = dataset.getOpenValue(row, column);
					double close = dataset.getCloseValue(row, column);

					if (open > close) {
						return getDownPaint();
					} else {
						return getUpPaint();
					}
				}
			};
			candleRenderer.setUpPaint(upColor);
			candleRenderer.setCandleWidth(1.6);
			candleRenderer.setUseOutlinePaint(true);
			candleRenderer.setToolTipGenerator(null);

			// 3.
			DateAxis domainAxis = new DateAxis();
			SegmentedTimeline timeline = SegmentedTimeline
					.newMondayThroughFridayTimeline();
			domainAxis.setTimeline(timeline); // Logu
			domainAxis.setDateFormatOverride(new SimpleDateFormat("MM/dd/yy"));

			NumberAxis rangeAxis = new NumberAxis();
			rangeAxis.setAutoRange(true);
			rangeAxis.setAutoRangeIncludesZero(false);

			// 4. price plot
			pricePlot = new XYPlot(null, domainAxis, rangeAxis, candleRenderer);
			pricePlot.setBackgroundPaint(bgColor);
			pricePlot.setDomainGridlinePaint(gridColor);
			pricePlot.setRangeGridlinePaint(gridColor);
			pricePlot.setRangePannable(true);
			// >>

			mainPlot = new CombinedDomainXYPlot(domainAxis);
			mainPlot.add(pricePlot, 6); // keep the price plot all the time.
			mainPlot.setGap(0.01);

			mainPlot.setBackgroundPaint(bgColor);
			mainPlot.setDomainGridlinePaint(Color.white);
			mainPlot.setDomainGridlinesVisible(true);
			mainPlot.setDomainPannable(true);

			pricePlot.setRangeAxisLocation(AxisLocation.TOP_OR_RIGHT);
			// return the new combined chart
			chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, mainPlot,
					false);

			this.setChart(chart);

			this.setFocusable(true);
			this.requestFocus();
		}

		JFreeChart chart;

		final CombinedDomainXYPlot mainPlot;
		XYPlot pricePlot;

		public void showChart(Quotes quotes, int day) throws Exception {

			OHLCDataset priceDS = createPriceDataset(quotes, day);

			JFreeChart chart = getChart();

			// CombinedDomainXYPlot cdplot = (CombinedDomainXYPlot) chart.getPlot();
			XYPlot subplot1 = (XYPlot) mainPlot.getSubplots().get(0);
			subplot1 = pricePlot;

			//
			XYItemRenderer r = subplot1.getRenderer(1);

			//
			float p = quotes.getClose(0);
			String decFormat = (p < 2) ? "0.0000" : ((p > 200) ? "0" : "0.00");

			((NumberAxis) subplot1.getRangeAxis())
					.setNumberFormatOverride(new DecimalFormat(decFormat));

			subplot1.setDataset(priceDS);
				pricePlot.setRenderer(candleRenderer);

			((AbstractSeriesDataset) subplot1.getDataset(0)).seriesChanged(null); // refresh

			this.requestFocusInWindow();

		}

		private OHLCDataset createPriceDataset(Quotes quotes, int day) {

			OHLCSeries s1 = new OHLCSeries("candle");
			
			Quote q = quotes.get(day);

			float open = quotes.getOpen(day+2), 
						high = q.high, low = q.low, close = q.close;
				
			for(int i = day; i < day + 3; i++) {
				if(low > quotes.getLow(i)) low = quotes.getLow(i);
				if(high < quotes.getHigh(i)) high = quotes.getHigh(i);
			}
			try {
				s1.add(new Day(q.date), open, high, low, close);
			} catch (Exception e) {
			}
			OHLCSeriesCollection dataset = new OHLCSeriesCollection();
			dataset.addSeries(s1);

			return dataset;
		}
	}
}


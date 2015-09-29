package com.valtrader.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.general.AbstractSeriesDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Week;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

import com.valtrader.data.NHNL;
import com.valtrader.data.NHNLData;
import com.valtrader.data.Quote;
import com.valtrader.service.Notes;
import com.valtrader.service.Quotes;
import com.valtrader.service.Scanner;
import com.valtrader.utils.Calc;
import com.valtrader.utils.Candle;
import com.valtrader.utils.Utils;

public class ValChart extends ChartPanel {
	// Color bgColor = Color.black;
	// Color gridColor = Color.gray;
	// Color upColor = Color.green;
	// Color downColor = Color.gray;
	// Color gridColor = new Color(153, 153, 255); //lightBlue
	// Color gridColor = Color.gray;

	Color gridColor = Color.lightGray;
	Color upColor = new Color(0, 0, 204); // Color.blue;
	Color downColor = Color.red;
	Color bgColor = Color.white;

	ValTrader app;
	ValueMarker marker;

	Vector<Color> uiColors = new Vector<Color>(); // upper indicator colors
	Vector<Color> liColors = new Vector<Color>(); // lower indicator colors

	// Vector<MyLine> lines = new Vector<MyLine>();
	CandlestickRenderer candleRenderer;
	StandardXYItemRenderer lineRenderer;

	// XYBarRenderer barRenderer;

	public ValChart(ValTrader frame) {
		super(null);
		app = frame;

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

		// barRenderer = new XYBarRenderer();

		lineRenderer = new StandardXYItemRenderer() {
			@Override
			public Paint getItemPaint(int row, int col) {
				return Color.black;
//				col = quotes.size() - col - 1;
//				return sig20[col];
			}
		};

		// 2. lower renderer

		lower1Renderer = new StandardXYItemRenderer() {
			@Override
			public Paint getItemPaint(int row, int col) {

				if (liColors.size() <= row)
					return Color.black;
				return liColors.get(row);
			}
		};

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

		// 5. marker in the upper plot

		marker = new ValueMarker(0);
		marker.setPaint(bgColor);
		marker.setLabelPaint(Color.lightGray);
		marker.setLabelFont(new Font("Verdana", Font.PLAIN, 25));
		marker.setLabelAnchor(RectangleAnchor.BOTTOM_RIGHT);
		marker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);

		// 6. now set renderer for upper indicator plot.
		pricePlot.setRenderer(1, new StandardXYItemRenderer() {
			@Override
			public Paint getItemPaint(int row, int col) {
				if (row >= uiColors.size())
					return Color.black;
				return uiColors.get(row);
			}
		});

		lowerIndPlot1 = new XYPlot(null, null, new NumberAxis(""),
				lower1Renderer);
		lowerIndPlot1.setBackgroundPaint(bgColor);
		lowerIndPlot1.setDomainGridlinePaint(gridColor);
		lowerIndPlot1.setRangeGridlinePaint(gridColor);

		lowerIndPlot2 = new XYPlot(null, null, new NumberAxis(""),
				lower1Renderer);
		lowerIndPlot2.setBackgroundPaint(bgColor);
		lowerIndPlot2.setDomainGridlinePaint(gridColor);
		lowerIndPlot2.setRangeGridlinePaint(gridColor);
		lowerIndPlot2.setRangeAxisLocation(AxisLocation.TOP_OR_RIGHT);
		mainPlot = new CombinedDomainXYPlot(domainAxis);
		mainPlot.add(pricePlot, 6); // keep the price plot all the time.
		mainPlot.setGap(0.01);

		mainPlot.setBackgroundPaint(bgColor);
		mainPlot.setDomainGridlinePaint(Color.white);
		mainPlot.setDomainGridlinesVisible(true);
		mainPlot.setDomainPannable(true);

		pricePlot.setRangeAxisLocation(AxisLocation.TOP_OR_RIGHT);
		lowerIndPlot1.setRangeAxisLocation(AxisLocation.TOP_OR_RIGHT);
		// return the new combined chart
		chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, mainPlot,
				false);

		this.setChart(chart);

		this.setFocusable(true);
		this.requestFocus();

		setupMouse();
		setupPopup();

	}

	JFreeChart chart;
	Wave wave = new Wave();

	void drawWaveB(ChartMouseEvent event) {

		int mouseX = event.getTrigger().getX();
		int mouseY = event.getTrigger().getY();
		Point2D p = translateScreenToJava2D(new Point(mouseX, mouseY));
		XYPlot plot = (XYPlot) getChart().getPlot();
		ChartRenderingInfo info = getChartRenderingInfo();
		Rectangle2D dataArea = info.getPlotInfo().getDataArea();

		ValueAxis domainAxis = plot.getDomainAxis();
		RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();

		// NumberAxis rangeAxis = (NumberAxis) subplot1.getRangeAxis(0);
		// RectangleEdge rangeAxisEdge = subplot1.getRangeAxisEdge();
		double chartX = domainAxis.java2DToValue(p.getX(), dataArea,
				domainAxisEdge);
		Date dd = new Date((long) chartX);
		Quote q = getQuote(dd);

		if (q == null) {
			ValTrader.msgBox("q == null");
			return;
		}
		Wave qwave = new Wave();
		qwave.add(dd, q.low, q.high);

		// find the lowest or highest point from here.
		int pointH = 0, pointL = 0;
		float ll = q.low, hh = q.high;

		// for (int i = getQuoteDay(dd); i >= 0; i--) {
		// if (quotes.getLow(i) < ll) {
		// ll = quotes.getLow(i);
		// pointL = i;
		// }
		//
		// if (quotes.getHigh(i) > hh) {
		// hh = quotes.getHigh(i);
		// pointH = i;
		// }
		// }

		int point1 = (q.low > ll) ? pointL : pointH;

		qwave.add(quotes.getDate(point1), quotes.getLow(point1),
				quotes.getHigh(point1));

		drawWave(qwave);
	}

	void drawWave(ChartMouseEvent event) {

		int mouseX = event.getTrigger().getX();
		int mouseY = event.getTrigger().getY();
		Point2D p = translateScreenToJava2D(new Point(mouseX, mouseY));
		XYPlot plot = (XYPlot) getChart().getPlot();
		ChartRenderingInfo info = getChartRenderingInfo();
		Rectangle2D dataArea = info.getPlotInfo().getDataArea();

		ValueAxis domainAxis = plot.getDomainAxis();
		RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();

		// XYPlot subplot1 = (XYPlot) ((CombinedDomainXYPlot)
		// plot).getSubplots()
		// .get(0);

		// NumberAxis rangeAxis = (NumberAxis) subplot1.getRangeAxis(0);
		// RectangleEdge rangeAxisEdge = subplot1.getRangeAxisEdge();
		double chartX = domainAxis.java2DToValue(p.getX(), dataArea,
				domainAxisEdge);
		Date dd = new Date((long) chartX);
		Quote q = getQuote(dd);

		if (q == null) {
			ValTrader.msgBox("q == null");
			return;
		}
		if (wave == null)
			wave = new Wave();
		wave.add(dd, q.low, q.high);

		drawWave(wave);
	}

	void drawWave(Wave wave) {
		// if(!app.optViewWave) return;

		if (wave == null || wave.size() <= 0)
			return;
		removeAnnotations(pricePlot);
		float[] targets = wave.getTargets();
		if (targets == null) {
			long x = wave.getDate(0);
			this.drawVerticalLine(pricePlot, x, wave.getValue(0));
			return;
		}

		Color[] waveColors = { Color.blue, Color.red, Color.green,
				Color.magenta, Color.cyan, Color.gray, Color.black };
		String text = "Targets: ";
		double chartX = wave.getDate(wave.size() - 1);

		for (int i = 0; i < targets.length; i++) {
			if (targets[i] != 0) {
				XYLineAnnotation a1 = new XYLineAnnotation(chartX - (10 * DAY),
						targets[i], chartX + (40 * DAY), targets[i],
						new BasicStroke(2.5f), waveColors[i]);
				pricePlot.addAnnotation(a1);
				text += Utils.myFormat(targets[i]) + " : ";
			}
		}

		for (int i = 1; i < wave.size(); i++) {
			long x1 = wave.getDate(i - 1);
			float y1 = wave.getValue(i - 1);
			long x2 = wave.getDate(i);
			float y2 = wave.getValue(i);
			XYLineAnnotation a1 = new XYLineAnnotation(x1, y1, x2, y2,
					new BasicStroke(1.5f), waveColors[i]);
			pricePlot.addAnnotation(a1);
		}

		int idx = wave.size() - 1;
		long x = wave.getDate(idx);
		float y = wave.getValue(idx);
		this.writeText(pricePlot, x, y, text, Color.black);
	}

	StandardXYItemRenderer lower1Renderer;
	// Wave wave = new Wave();
	double prev = -1;

	private void setupMouse() {
		this.addChartMouseListener(new ChartMouseListener() {

			@Override
			public void chartMouseClicked(final ChartMouseEvent event) {
				requestFocusInWindow();

				savedMouseEvent = event;
				double f = getPoint(event);
				if (prev > 0) {
					app.setStatus(1, Utils.myFormat((float) (f - prev)));
				}

				prev = f;
				// requestFocus();
				// long x = (long)pricePlot.getDomainCrosshairValue();
				// if(x == 0) return;
				// wave.add(x);
				if (app.optViewWave)
					drawWave(event);
				if (app.optWaveB)
					drawWaveB(event);
				if (app.optHLine)
					drawHorizontalLine('h');
				;
				if (app.optVLine)
					drawVerticalLine();
				// if(app.optTrade) drawTrade();

			}

			@Override
			public void chartMouseMoved(final ChartMouseEvent event) {
				curMouseEvent = event;
				int mouseX = event.getTrigger().getX();
				int mouseY = event.getTrigger().getY();
				Point2D p = translateScreenToJava2D(new Point(mouseX, mouseY));
				XYPlot plot = (XYPlot) getChart().getPlot();
				ChartRenderingInfo info = getChartRenderingInfo();
				Rectangle2D dataArea = info.getPlotInfo().getDataArea();

				ValueAxis domainAxis = plot.getDomainAxis();
				RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();

				XYPlot subplot1 = (XYPlot) ((CombinedDomainXYPlot) plot)
						.getSubplots().get(0);

				NumberAxis rangeAxis = (NumberAxis) subplot1.getRangeAxis(0);
				RectangleEdge rangeAxisEdge = subplot1.getRangeAxisEdge();
				double chartX = domainAxis.java2DToValue(p.getX(), dataArea,
						domainAxisEdge);
				double chartY = rangeAxis.java2DToValue(p.getY(), dataArea,
						rangeAxisEdge);
				Date dd = new Date((long) chartX);
				app.setStatus(Utils.formatDate(dd, "MM/dd/yy"));
				showDay(dd);
			}
		});

	}

	void showSignal() {
		String sys = app.system.getText();
		if (sys == null || sys.isEmpty())
			return;

		int[] sigs = null;

		signalNotes = null;
		if (sys.equals("mac"))
			sigs = Scanner.checkForMac(quotes);
		else if (sys.equals("tail"))
			sigs = Scanner.checkForTail(quotes);
		else if (sys.equals("fireworks"))
			sigs = Scanner.checkForFireworks(quotes);
		else if (sys.startsWith("candle")) {
			// Usage: candle candlename1,candlename2,... [{up|down}]
			String[] whata = sys.split(" ");
			String what = null;
			int dir = 0;
			if (whata.length > 1)
				what = whata[1];
			if (whata.length > 2) {
				if (whata[2].equals("up")) {
					dir = Scanner.bullish;
				} else if (whata[2].equals("down")) {
					dir = Scanner.bearish;
				}
			}
			int size = quotes.size();
			sigs = new int[size];
			signalNotes = new String[size];
			Candle.check(quotes, sigs, signalNotes, what, dir);
			if (!signalNotes[0].isEmpty()) {
				app.setStatus(1, signalNotes[0]);
			}
		} else
			return;

		for (int i = 0; i < sigs.length; i++) {
			int s = sigs[i];
			if (s != 0) {
				long x = quotes.getDate(i).getTime();
				x += 12 * 60 * 60 * 1000;
				Color col = (s > 0) ? Color.cyan : Color.orange;
				switch (s) {
				case Scanner.buy:
					col = Color.green;
					break;
				case Scanner.sell:
					col = Color.red;
					break;
				case Scanner.bullish:
					col = Color.cyan;
					break;
				case Scanner.bearish:
					col = Color.orange;
					break;
				default:
					col = Color.black;
					break;
				}
				double y = (s > 0) ? quotes.getLow(i) : quotes.getHigh(i);
				double ang = Math.PI / 2 * s / Math.abs(s);
				XYPointerAnnotation annotation = new XYPointerAnnotation("", x,
						y, ang);
				annotation.setArrowPaint(col);
				annotation.setArrowLength(10);
				annotation.setArrowWidth(5);
				pricePlot.addAnnotation(annotation);
			}
		}
	}

	String[] signalNotes = null;

	void showDay(Date dd) {
		int qd = getQuoteDay(dd);
		if (qd < 0)
			return;
CandleChart.showCandle(quotes, qd);
		Quote q = quotes.get(qd);
		int qd1 = qd + 1;

		float priceChange = 0, priceChangePerc = 0;

		if (qd1 < quotes.size()) {
			Quote q1 = quotes.get(qd1);
			priceChange = q.close - q1.close;
			priceChangePerc = priceChange / q1.close * 100;
		}
		float[] av = Calc.calcSMA(quotes.getVolume(), 50);
		String volchg = (int) (q.volume / av[qd] * 100) + "%";
		String notes = "";
		if (signalNotes != null && signalNotes[qd] != null
				&& !signalNotes[qd].isEmpty())
			notes = signalNotes[qd];
		app.setTitle(Defaults.appName
				+ " - [ "
				+ qd
				+ " ] "
				+ Utils.formatDate(q.date, "MM/dd/yy")
				+ " : O - "
				+ q.open
				+ ": H - "
				+ q.high
				+ ": L - "
				+ q.low
				+ ": C - "
				+ q.close
				+ "; Range: "
				+ Utils.myFormat(q.high - q.low)
				+ "; Volume: "
				+ volchg
				+ "; "
				+ String.format("Chg: [ %.2f (%.2f%%)]", priceChange,
						priceChangePerc) + notes);

	}

	Quote getQuote(Date dd) {
		// String d1 = Utils.formatDate(dd, "ddMMyyyy");
		for (int i = 0; i < quotes.size(); i++) {
			// String d2 = Utils.formatDate(quotes.getDate(i), "yyyyMMdd");
			//
			// if (d1.equals(d2))
			// return quotes.get(i);
			if (quotes.getDate(i).getTime() < dd.getTime()) {
				return quotes.get(i);
			}
		}
		return null;
	}

	int getQuoteDay(Date dd) {
		if (quotes == null)
			return -1;
		for (int i = 0; i < quotes.size(); i++) {
			if (quotes.getDate(i).getTime() < dd.getTime()) {
				return i;
			}
		}
		return -1;
	}

	float getPoint(ChartMouseEvent event) {
		int mouseX = event.getTrigger().getX();
		int mouseY = event.getTrigger().getY();
		Point2D p = translateScreenToJava2D(new Point(mouseX, mouseY));
		XYPlot plot = (XYPlot) getChart().getPlot();
		ChartRenderingInfo info = getChartRenderingInfo();
		Rectangle2D dataArea = info.getPlotInfo().getDataArea();

		ValueAxis domainAxis = plot.getDomainAxis();
		// RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();

		XYPlot subplot1 = (XYPlot) ((CombinedDomainXYPlot) plot).getSubplots()
				.get(0);

		NumberAxis rangeAxis = (NumberAxis) subplot1.getRangeAxis(0);
		RectangleEdge rangeAxisEdge = subplot1.getRangeAxisEdge();
		// double chartX = domainAxis.java2DToValue(p.getX(), dataArea,
		// domainAxisEdge);
		double chartY = rangeAxis.java2DToValue(p.getY(), dataArea,
				rangeAxisEdge);

		return (float) chartY;
	}

	final CombinedDomainXYPlot mainPlot;
	XYPlot pricePlot;
	XYPlot lowerIndPlot1, lowerIndPlot2;

	boolean stopMouseMove = false;
	ChartMouseEvent savedMouseEvent = null, curMouseEvent = null;

	String upperInd = null;

	Color [] sig20 = null;
	
	public void showChart(String symbol, String st, String period, int start,
			int numdays, String upperInd, String lowerInd1, String lowerInd2,
			String label, String barType) throws Exception {

		this.upperInd = upperInd;

		// wave.reset();
		if (app.optViewWave)
			wave = Quotes.getWave(symbol);
		Trade trade = null;
		if (app.optTrade)
			trade = Quotes.getTrade(symbol);
		uiColors.clear();
		liColors.clear();

		// setCursor(Cursor.WAIT_CURSOR);
		quotes = Quotes.getQuotes(symbol, st, start, numdays, period);

		if (quotes == null || quotes.size() < 1) {
			ValTrader.msgBox("Symbol not found:" + symbol);
			return;
		}
		sig20 = calcSig20(quotes); 
		showStatus(quotes, st);
		CandleChart.showCandle(quotes, 0);
		OHLCDataset priceDS = createPriceDataset(quotes, period, barType);

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
		if (barType.equals("line")) {
			pricePlot.setRenderer(lineRenderer);
		} else if (barType.equals("candle")) {
			pricePlot.setRenderer(candleRenderer);
			// } else if (barType.equals("bar")) {
			// pricePlot.setRenderer(barRenderer);
		}

		removeAnnotations(subplot1);
		removeAnnotations(lowerIndPlot1);
		removeAnnotations(lowerIndPlot2);

		TimeSeriesCollection upperInds = new TimeSeriesCollection();

		String[][] inds = Utils.parseArgs(upperInd);

		rsTexts = null;
		if (inds != null) {

			MyPlot plot = null;
			for (String[] ind : inds) {
				String ui = ind[0];
				if (ui.equals("mac")) {
					plot = getMAC(ind, quotes, upperInds);
				} else if (ui.equals("turtle")) {
					plot = getTurtle(ind, quotes, upperInds);
				} else if (ui.equals("ema")) {
					plot = getEMA(ind, quotes, upperInds);
				} else if (ui.equals("rs")) {
					subplot1.setDataset(createRSDataset(quotes, period));
					plot = getRS(ind, quotes, upperInds);
				} else if (ui.equals("atr")) {
					plot = getUpperATR(ind, quotes, upperInds);
				} else if (ui.equals("band")) {
					plot = getBand(quotes, upperInds);
				} else if (ui.equals("line")) {
					plot = getLine(ind, quotes, upperInds);
				} else if (ui.equals("sma")) {
					plot = getSMA(ind, quotes, upperInds);
				}
				if (plot != null) {
					for (Color col : plot.colors)
						uiColors.add(col);
				}
			}

			subplot1.setDataset(1, upperInds);
		}
		// >>

		marker.setValue(quotes.max);
		try {
			String msg = quotes.getSymbol() + ":"
					+ Utils.formatDate(quotes.getDate(0), "MM/dd/yy") + ":"
					+ Utils.myFormat(quotes.getClose(0));
			if (label != null && label.split(" ").length <= 1)
				label = null;
			if (label != null) {
				msg += label.substring(label.indexOf(" "));
			}
			marker.setLabel(msg);
		} catch (Exception e) {
			e.printStackTrace();
			ValTrader.msgBox(e.getMessage());
		}
		subplot1.clearRangeMarkers();
		subplot1.addRangeMarker(marker);

		if (app.optViewText)
			loadNotes(subplot1, quotes);

		((AbstractSeriesDataset) subplot1.getDataset(0)).seriesChanged(null); // refresh

		mainPlot.remove(lowerIndPlot1);
		mainPlot.remove(lowerIndPlot2);
		if (lowerInd1 != null && !lowerInd1.isEmpty()) {
			drawLowerInd(lowerIndPlot1, quotes, app.lowerInd1.getText());
			mainPlot.add(lowerIndPlot1);
		}

		if (lowerInd2 != null && !lowerInd2.isEmpty()) {
			drawLowerInd(lowerIndPlot2, quotes, app.lowerInd2.getText());
			mainPlot.add(lowerIndPlot2);
		}

		this.requestFocusInWindow();

		if (rsTexts != null) {
			for (RsText t : rsTexts) {
				this.writeText(pricePlot,
						quotes.getDate(0).getTime() + 5 * DAY, t.pos, t.text,
						t.col);
			}
		}

		if (app.optViewWave)
			drawWave(wave);
		if (trade != null)
			drawTrade(trade);

		// Vector<String> mom = Scanner.getMOM(quotes);
		// msg = "";
		// for(String m: mom) {
		// msg += m + "\n";
		// }
		// int [] sig = Scanner.getMOM1(quotes);
		// msg = "<html><table><tr>";
		// int tmpcnt = 0;
		// for(int i = 0; i < sig.length; i++) {
		// if(sig[i] != 0) {
		// msg += "<td color=" + ((sig[i] == 1) ? "blue" : "red") + ">" + i +
		// "</td>";
		// tmpcnt++;
		// }
		// if(tmpcnt > 10) {
		// msg += "</tr><tr>";
		// tmpcnt = 0;
		// }
		//
		// }
		// msg += "</tr></table></html>";
		// ValTrader.msgBox(msg);

		showSignal();

//		Scanner.findTrade(symbol);
	}

	private Color[] calcSig20(Quotes q) {
		Color [] ret = new Color[q.size()];

		Color prev = Color.black;
		Color sig = Color.black;
		
		for (int col = q.size() - 20; col >= 0; col--) {
			float hh = quotes.getClose(col);
			float ll = hh;
			for (int i = col; i <= col + 10; i++) {
				if (hh < quotes.getClose(i))
					hh = quotes.getClose(i);
				if (ll > quotes.getClose(i))
					ll = quotes.getClose(i);
			}
			if (hh == quotes.getClose(col))
				sig = Color.cyan;
			else if (ll == quotes.getClose(col))
				sig = Color.magenta;
			else sig = prev;
			
			hh = quotes.getClose(col);
			ll = hh;
			for (int i = col; i <= col + 20; i++) {
				if (hh < quotes.getClose(i))
					hh = quotes.getClose(i);
				if (ll > quotes.getClose(i))
					ll = quotes.getClose(i);
			}
			if (hh == quotes.getClose(col))
				sig = Color.blue;
			else if (ll == quotes.getClose(col))
				sig = Color.red;
			else sig = prev;
			ret[col] = sig;
			prev = sig;
		}
		
		return ret;
	}

	Quotes quotes = null;

	void drawLowerInd(XYPlot plot, Quotes quotes, String ind) {
		if (plot == null || ind.isEmpty())
			return;

		MyPlot myplot = createLowerPlot(ind, quotes);
		if (myplot == null)
			return;
		plot.setDataset(myplot.dataset);
		if (myplot.renderer != null) {
			plot.setRenderer(myplot.renderer);
		} else {
			plot.setRenderer(lower1Renderer);
		}
		if (myplot.subplot != null) {
			MyPlot lowersub = myplot.subplot;
			plot.setDataset(1, lowersub.dataset);
			plot.setRenderer(1, lowersub.renderer);
		}

		((AbstractSeriesDataset) plot.getDataset(0)).seriesChanged(null); // refresh

	}

	private void loadNotes(XYPlot plot, Quotes quotes) {
		if (quotes.notes == null || quotes.notes.size() == 0)
			return;
		for (Notes n : quotes.notes) {
			writeText(plot, (long) n.getX(), (float) n.getY(),
					"[" + n.getDate() + "] " + n.getText());
		}
	}

	private void removeAnnotations(XYPlot plot) {
		List<XYTextAnnotation> list = plot.getAnnotations();
		for (XYAnnotation a : list)
			plot.removeAnnotation(a);

	}

	private void writeText(XYPlot plot, long x, float y, String text) {
		XYTextAnnotation annotation = new XYTextAnnotation(text, x, y);
		annotation.setFont(new Font("Times New Roman", Font.ITALIC, 12));
		annotation.setToolTipText(text);
		plot.addAnnotation(annotation);
	}

	private void writeText(XYPlot plot, long x, float y, String text, Color col) {
		XYTextAnnotation annotation = new XYTextAnnotation(text, x, y);
		annotation.setPaint(col);
		annotation.setFont(new Font("Times New Roman", Font.ITALIC, 12));
		annotation.setToolTipText(text);
		plot.addAnnotation(annotation);
	}

	void drawVerticalLine(XYPlot plot, long x, float y, Color col) {
		XYLineAnnotation a1 = new XYLineAnnotation(x, y + 1000, x, y - 1000,
				new BasicStroke(0.5f), col);
		plot.addAnnotation(a1);

	}

	void drawVerticalLine(XYPlot plot, long x, float y) {
		drawVerticalLine(plot, x, y, Color.black);
	}

	void drawHorizontalLine(XYPlot plot, long x, float y) {
		XYLineAnnotation a1 = new XYLineAnnotation(x, y, quotes.getDate(0)
				.getTime(), y, new BasicStroke(0.5f), Color.black);
		plot.addAnnotation(a1);
	}

	long DAY = (1 * 24 * 60 * 60 * 1000);

	void drawHorizontalLine(XYPlot plot, long x, float y, float stroke) {
		XYLineAnnotation a1 = new XYLineAnnotation(x, y, quotes.getDate(0)
				.getTime() + (5 * DAY), y, new BasicStroke(stroke), Color.black);
		plot.addAnnotation(a1);

	}

	void drawHorizontalLine(XYPlot plot, long x, float y, float stroke,
			Color col) {
		XYLineAnnotation a1 = new XYLineAnnotation(x, y, quotes.getDate(0)
				.getTime() + (5 * DAY), y, new BasicStroke(stroke), col);
		plot.addAnnotation(a1);

	}

	void drawLine1(XYPlot plot, long x1, float y1, long x2, float y2) {
		XYLineAnnotation a1 = new XYLineAnnotation(x1, y1, x2, y2,
				new BasicStroke(0.5f), Color.black);
		plot.addAnnotation(a1);

	}

	Color[] ind1col = null;

	private void showStatus(Quotes quotes, String type) {
		Quote q = quotes.get(0);
		float l = q.volume * q.close;
		String liq = l + "";
		String f = "%.0f";
		if (l > 1000000000) {
			liq = String.format(f, l / 1000000000) + "B";
		} else if (l > 1000000) {
			liq = String.format(f, l / 1000000) + "M";
		} else if (l > 1000) {
			liq = String.format(f, l / 1000) + "K";
		} else {
			liq = String.format(f, l);
		}
		Color liqCol = (l > 1000000) ? Color.blue : Color.red;

		double av = 0;
		if (quotes.size() >= 50)
			for (int i = 0; i < 50; i++) {
				av += quotes.get(i).volume;
			}
		av /= 50;
		double volchg = quotes.get(0).volume / av * (float) 100;
		Color volCol = (volchg > 100) ? Color.red : Color.blue;

		float[] atr = Calc.calcATR(quotes, 22, false);
		Color atrCol = (atr[0] > quotes.get(0).close * 0.03) ? Color.red
				: Color.black;

		String chgPerc = "";
		Quote q1 = quotes.get(1);
		Color chgCol = (q.close < q1.close) ? Color.red : Color.blue;
		chgPerc = String.format(" %.2f; %.2f%%", (q.close - q1.close),
				(q.close - q1.close) / q1.close * 100);
		app.setStatus(
				new String[] {
						Utils.formatDate(q.date, "MM/dd/yy"),
						"Liq: $" + liq,
						"C: " + chgPerc,
						"V:" + String.format("%.0f %%", volchg),
						"ATR: " + Utils.myFormat(atr[0]),
						Utils.myFormat(atr[0] / quotes.get(0).close * 100)
								+ "%" }, new Color[] { Color.black, liqCol,
						chgCol, volCol, atrCol, atrCol });

		app.symbolType.setText(quotes.getType());

	}

	static TimeSeries tl = new TimeSeries("MA Test");

	private OHLCDataset createPriceDataset(Quotes quotes, String period,
			String barType) {

		int merge = 0; // how many candles to merge
		String uind = app.upperInd.getText();
		if (uind.startsWith("merge")) {
			String[] xxx = uind.split(" ");
			merge = Integer.parseInt(xxx[1]);
		}

		OHLCSeries s1 = new OHLCSeries("");
		// t1.clear();

		RegularTimePeriod per;
		/**
		 * HA Interpretation - A long blue Heikin-Ashi candlestick shows strong
		 * buying pressure over a two day period. - Absence of a lower shadow
		 * also reflects strength. - A long red Heikin-Ashi candlestick shows
		 * strong selling pressure over a two day period. - Absence of an upper
		 * shadow also reflects selling pressure. - Small Heikin-Ashi
		 * candlesticks or those with long upper and lower shadows show
		 * indecision over the last two days. This often occurs when the two
		 * normal candlesticks are of opposite color.
		 */
		float prevOpen = 0 /* , prevHigh = 0, prevLow = 0 */, prevClose = 0;
		for (int i = quotes.size() - 1; i >= merge; i--) {
			Quote q = quotes.get(i);

			float open = q.open, high = q.high, low = q.low, close = q.close;
			if (barType.equalsIgnoreCase("ha")) {
				close = (open + high + low + close) / 4;
				if (i == quotes.size() - 1) {
					prevOpen = open;
					prevClose = close;
					// prevHigh = high;
					// prevLow = low;
				} else {
					open = (prevOpen + prevClose) / 2;
					high = max(high, open, close);
					low = min(low, open, close);
					prevOpen = open;
					prevClose = close;
					// prevHigh = high;
					// prevLow = low;
				}

				/*
				 * xClose = (Open+High+Low+Close)/4 o Average price of the
				 * current bar xOpen = [xOpen(Previous Bar) + Close(Previous
				 * Bar)]/2 o Midpoint of the previous bar xHigh = Max(High,
				 * xOpen, xClose) o Highest value in the set xLow = Min(Low,
				 * xOpen, xClose) o Lowest value in the set
				 */
			}
			// per = new Week(Utils.sql2util(q.date));
			if (period.equals("W")) {
				per = new Week(q.date);
			} else {
				per = new Day(q.date);
			}
			// s1.add(per, q.open, q.high, q.low, q.close);
			// System.err.println(per);
			try {
				s1.add(per, open, high, low, close);
			} catch (Exception e) {
				// e.printStackTrace();
				// for(int
				// x=0;x<s1.getItemCount();x++)System.err.println(x+":"+s1.getPeriod(x));
				// ValTrader.msgBox("Exception: " + e.getMessage() + ":"+per);
			}
//			 t1.add(per, close);
			// Day(q.date)
		}
		if (merge > 0) {
			merge--;
			int saved = merge;
			float o = quotes.getOpen(merge), h = quotes.getHigh(merge), l = quotes
					.getLow(merge), c = quotes.getClose(0);

			merge--;
			for (int i = merge; i >= 0; i--) {
				if (l > quotes.getLow(i))
					l = quotes.getLow(i);
				if (h < quotes.getHigh(i))
					h = quotes.getHigh(i);
			}
			try {
				s1.add(new Day(quotes.getDate(saved)), o, h, l, c);
			} catch (Exception e) {
			}
		}
		OHLCSeriesCollection dataset = new OHLCSeriesCollection();
		dataset.addSeries(s1);

		return dataset;
	}

	private static float max(float... values) {
		float ret = -9999999;
		for (float v : values) {
			if (v > ret)
				ret = v;
		}
		return ret;
	}

	private static float min(float... values) {
		float ret = 9999999;
		for (float v : values) {
			if (v < ret)
				ret = v;
		}
		return ret;
	}

	private static OHLCDataset createRSDataset(Quotes quotes, String period) {
		OHLCSeries s1 = new OHLCSeries("");

		RegularTimePeriod per;

		float base = quotes.getClose(quotes.size() - 1);
		for (int i = quotes.size() - 1; i >= 0; i--) {
			Quote q = quotes.get(i);

			if (period.equals("W")) {
				per = new Week(q.date);
			} else {
				per = new Day(q.date);
			}
			float val = (q.close - base) / base * 100;
			try {
				s1.add(per, val, val, val, val);
			} catch (Exception e) {
			}
//			 t1.add(per, val);
			// Day(q.date)
		}

		OHLCSeriesCollection dataset = new OHLCSeriesCollection();
		dataset.addSeries(s1);

		return dataset;
	}

	private MyPlot createLowerPlot(String lind, Quotes quotes) {

		String[][] inds = Utils.parseArgs(lind);
		if (inds == null || inds.length == 0)
			return null;

		liColors.clear();

		MyPlot plot = null;
		for (String[] ind : inds) {
			String li = ind[0];
			if (li.equals("rsi")) {
				plot = getRSI(ind, quotes);
			} else if (li.equals("nhnl")) {
				plot = getNHNL(ind, quotes);
			} else if (li.equals("control")) {
				plot = getControl(ind, quotes);
			} else if (li.equals("roc")) {
				plot = getROC(ind, quotes);
			} else if (li.equals("chg%")) {
				plot = getChangePerc(ind, quotes);
			} else if (li.equals("stoc")) {
				plot = getStoc(ind, quotes);
			} else if (li.equals("macd")) {
				plot = getMACD(ind, quotes);
			} else if (li.equals("tr")) {
				plot = getTR(ind, quotes);
			} else if (li.equals("trend")) {
				plot = getTrend(ind, quotes);
			} else if (li.equals("atr")) {
				plot = getLowerATR(ind, quotes);
			} else if (li.equals("candle")) {
				plot = getCandle(ind, quotes);
			} else if (li.equals("volume")) {
				plot = getVolume(ind, quotes);
			}
			if (plot != null) {
				for (Color col : plot.colors)
					liColors.add(col);
			}
			break; // only one indicator for now
		}

		return plot;

	}

	MyPlot getRSI(String[] ind, final Quotes quotes) {

		TimeSeriesCollection ts = new TimeSeriesCollection();
		int rsiLength = 20;
		final float[] indv = Calc.calcRSI(quotes.getClose(), rsiLength);

		final TimeSeries s1 = new TimeSeries("rsi");
		TimeSeries s2 = new TimeSeries("rsiu");
		TimeSeries s3 = new TimeSeries("rsil");

		for (int i = 0; i < quotes.size(); i++) {
			Quote q = quotes.get(i);
			try {
				s1.add(new Day(q.date), indv[i]);
				s2.add(new Day(q.date), 75);
				s3.add(new Day(q.date), 25);
			} catch (Exception e) {
			}
		}

		s1.setMaximumItemCount(quotes.size() - rsiLength);

		ts.addSeries(s1);
		ts.addSeries(s2);
		ts.addSeries(s3);

		StandardXYItemRenderer r = new StandardXYItemRenderer() {
			@Override
			public Paint getItemPaint(int row, int col) {
				if (row == 1)
					return Color.red;
				if (row == 2)
					return Color.blue;
				col = s1.getMaximumItemCount() - col - 1;
				if (indv[col] > 70) {
					return Color.red;
				} else if (indv[col] < 30) {
					return Color.blue;
				}
				return Color.gray;
			}
		};

		return new MyPlot(ts,
				new Color[] { Color.black, Color.red, Color.green }, r);
	}
	
	MyPlot getNHNL(String[] ind, final Quotes quotes) {

		TimeSeriesCollection ts = new TimeSeriesCollection();
		final NHNL [] nhnl = NHNLData.get(app.period.getText());

		final TimeSeries s1 = new TimeSeries("nhnl");
		TimeSeries s2 = new TimeSeries("nh");
		TimeSeries s3 = new TimeSeries("nl");

		for (int i = 0; i < 177; i++) {
			NHNL n = nhnl[i];
			try {
//				System.err.println(quotes.getDate(i)+"/"+n.nh+"/"+n.nl);
				s1.add(new Day(quotes.getDate(i)), (n.nh-n.nl));
				s2.add(new Day(quotes.getDate(i)), 0);
			} catch (Exception e) {
			}
		}

		s1.setMaximumItemCount(177);

		ts.addSeries(s1);
		ts.addSeries(s2);
//		ts.addSeries(s3);

		StandardXYItemRenderer r = new StandardXYItemRenderer() {
			@Override
			public Paint getItemPaint(int row, int col) {
				if(row == 1) return Color.green;
				col = s1.getMaximumItemCount() - col - 1;
				int val = nhnl[col].nh - nhnl[col].nl;
				if (val > 0) {
					return Color.blue;
				} else if (val < 0) {
					return Color.red;
				}
				return Color.black;
			}
		};

		return new MyPlot(ts,
				new Color[] { Color.black, Color.red, Color.green }, r);
	}

	MyPlot getWeekly(String[] ind, final Quotes quotes) {
		// get weekly chart to know the trend
		// convert daily to weekly
		// draw line chart
		// draw 10 ema
		// draw 20 ema
		
		TimeSeriesCollection ts = new TimeSeriesCollection();

		final TimeSeries s1 = new TimeSeries("price");
		TimeSeries s2 = new TimeSeries("ema10");
		TimeSeries s3 = new TimeSeries("ema20");

//		Quotes w = Quotes.toPeriod(quotes, 0, quotes.size(),  "w");
		Quotes w = null;
		try {
			w = Quotes.getQuotes("QQQ", "E", 0, quotes.size(), "w");
		} catch(Exception e) {
			return null;
		}
		float [] ema10 = Calc.calcEMA(w.getClose(), 10);
		float [] ema20 = Calc.calcEMA(w.getClose(), 20);

		
		for (int i = 0; i < quotes.size() / 5; i++) {
			Quote q = w.get(i);
			try {
				System.out.println(q.date + "/" + q.close + "/" + ema10[i] + "/" + ema20[i]);
				s1.add(new Day(q.date), q.close);
				if(i < w.size()-11)
				s2.add(new Day(q.date), ema10[i]);
				if(i < w.size()-21)
				s3.add(new Day(q.date), ema20[i]);
			} catch (Exception e) {
			}
		}

		s1.setMaximumItemCount(quotes.size() / 5);

		ts.addSeries(s1);
		ts.addSeries(s2);
		ts.addSeries(s3);

		StandardXYItemRenderer r = new StandardXYItemRenderer() {
			@Override
			public Paint getItemPaint(int row, int col) {
				if (row == 1)
					return Color.blue;
				if (row == 2)
					return Color.red;
				return Color.black;
			}
		};

		return new MyPlot(ts,
				new Color[] { Color.black, Color.red, Color.green }, r);
	}
	MyPlot getNewHL(String[] ind, final Quotes quotes) {
		TimeSeriesCollection ts = new TimeSeriesCollection();
		int [] nh = new int[200], nl = new int[200];

		try {
			Calc.calcNewHL("nas100", nh, nl);
		} catch (Exception e) {
			app.msgBox(e.getMessage());
			e.printStackTrace();
		}
		final TimeSeries s1 = new TimeSeries("nh");
		final TimeSeries s2 = new TimeSeries("nl");

		for (int i = 0; i < quotes.size(); i++) {
			Quote q = quotes.get(i);
			try {
				s1.add(new Day(q.date), nh[i]);
				s2.add(new Day(q.date), nl[i]);
			} catch (Exception e) {
			}
		}

		s1.setMaximumItemCount(200);

		ts.addSeries(s1);
		ts.addSeries(s2);

		StandardXYItemRenderer r = new StandardXYItemRenderer() {
			@Override
			public Paint getItemPaint(int row, int col) {
				if (row == 1)
					return Color.red;
				if (row == 2)
					return Color.blue;
				return Color.black;
			}
		};

		return new MyPlot(ts,
				new Color[] { Color.blue, Color.red}, r);
	}

	MyPlot getROC(String[] ind, final Quotes quotes) {

		TimeSeriesCollection ts = new TimeSeriesCollection();
		int rsiLength = 20;
		final float[] indv = Calc.calcROC(quotes.getClose(), rsiLength);

		final TimeSeries s1 = new TimeSeries("roc");
		TimeSeries s2 = new TimeSeries("roc0");

		for (int i = 0; i < quotes.size(); i++) {
			Quote q = quotes.get(i);
			try {
				s1.add(new Day(q.date), indv[i]);

				s2.add(new Day(q.date), 0);
			} catch (Exception e) {
			}

		}

		s1.setMaximumItemCount(quotes.size() - rsiLength);

		ts.addSeries(s1);
		ts.addSeries(s2);

		StandardXYItemRenderer r = new StandardXYItemRenderer() {
			@Override
			public Paint getItemPaint(int row, int col) {
				if (row == 1)
					return Color.red;
				// col = s1.getMaximumItemCount() - col - 1;
				// if (indv[col] > 70) {
				// return Color.red;
				// } else if (indv[col] < 30) {
				// return Color.blue;
				// }
				return Color.blue;
			}
		};

		return new MyPlot(ts,
				new Color[] { Color.black, Color.red, Color.green }, r);
	}

	// Who is in charge, bulls or bears?
	MyPlot getControl(String[] ind, final Quotes quotes) {

		TimeSeriesCollection ts = new TimeSeriesCollection();

		final TimeSeries s1 = new TimeSeries("bulls");
		final TimeSeries s2 = new TimeSeries("bears");

		float bulls = 0, bears = 0;
		for (int i = quotes.size() - 2; i >= 0; i--) {
			Quote q = quotes.get(i);
			Quote q1 = quotes.get(i + 1);
			// if up candle give one point to bulls
			// if down candle give one point to bears

			float chgPerc = Math.abs((q.close - q1.close) / q1.close * 100);
			// chgPerc *= (q.volume/1000);
			if (q.close > q.open) {
				bulls += chgPerc;
				bears -= chgPerc;
			} else {
				bears += chgPerc;
				bulls -= chgPerc;
			}
			try {
				s1.add(new Day(q.date), bulls);
				s2.add(new Day(q.date), bears);
			} catch (Exception e) {
			}
		}

		s1.setMaximumItemCount(quotes.size() - 1);

		ts.addSeries(s1);
		ts.addSeries(s2);

		StandardXYItemRenderer r = new StandardXYItemRenderer() {
			@Override
			public Paint getItemPaint(int row, int col) {
				if (row == 1)
					return Color.red;
				// col = s1.getMaximumItemCount() - col - 1;
				// if (indv[col] > 70) {
				// return Color.red;
				// } else if (indv[col] < 30) {
				// return Color.blue;
				// }
				return Color.blue;
			}
		};

		return new MyPlot(ts, new Color[] { Color.blue, Color.red }, r);
	}

	MyPlot getChangePerc(String[] ind, final Quotes quotes) {

		TimeSeriesCollection ts = new TimeSeriesCollection();
		int rsiLength = 2;

		final TimeSeries s1 = new TimeSeries("chg%");

		final float[] indv = new float[quotes.size()];
		float prevchg = 0;
		for (int i = quotes.size() - 1; i >= 0; i--) {
			Quote q = quotes.get(i);
			float chg = (quotes.getClose(i) - quotes.getClose(i + 1))
					/ quotes.getClose(i + 1) * 100;
			try {
				s1.add(new Day(q.date), chg + prevchg);
			} catch (Exception e) {
			}

			indv[i] = chg + prevchg;
			prevchg = chg;
		}

		s1.setMaximumItemCount(quotes.size() - rsiLength);

		ts.addSeries(s1);

		StandardXYItemRenderer r = new StandardXYItemRenderer() {
			@Override
			public Paint getItemPaint(int row, int col) {
				col = s1.getMaximumItemCount() - col - 1;
				return (indv[col] > 0) ? Color.blue : Color.red;
			}
		};

		return new MyPlot(ts,
				new Color[] { Color.black, Color.red, Color.green }, r);
	}

	MyPlot getStoc(String[] ind, final Quotes quotes) {

		TimeSeriesCollection ts = new TimeSeriesCollection();
		int stocWidth = 14;
		final float[][] indv = Calc.calcStoc(quotes, stocWidth);
		// indv = {slow, fast}

		final TimeSeries s1 = new TimeSeries("stoc-fast");
		TimeSeries s2 = new TimeSeries("stock-slow");
		TimeSeries s3 = new TimeSeries("stoc-upper");
		TimeSeries s4 = new TimeSeries("stock-lower");

		for (int i = quotes.size() - stocWidth - 1; i >= 0; i--) {
			Quote q = quotes.get(i);
			try {
				s1.add(new Day(q.date), indv[0][i]);
				s2.add(new Day(q.date), indv[1][i]);
				s3.add(new Day(q.date), 20);
				s4.add(new Day(q.date), 80);
			} catch (Exception e) {
			}

		}

		s1.setMaximumItemCount(quotes.size() - stocWidth);

		ts.addSeries(s1);
		ts.addSeries(s2);
		ts.addSeries(s3);
		ts.addSeries(s4);

		StandardXYItemRenderer r = new StandardXYItemRenderer() {
			@Override
			public Paint getItemPaint(int row, int col) {
				if (row == 0)
					return Color.red;
				if (row == 1)
					return Color.blue;
				if (row == 2)
					return Color.blue;
				if (row == 3)
					return Color.red;
				// col = s1.getMaximumItemCount() - col - 1;
				// if (indv[0][col] > 70) {
				// return Color.red;
				// } else if (indv[0][col] < 30) {
				// return Color.blue;
				// }
				return Color.gray;
			}
		};

		return new MyPlot(ts,
				new Color[] { Color.black, Color.red, Color.green }, r);
	}

	// ///////////////////
	MyPlot getMAC(String[] ind, Quotes quotes, TimeSeriesCollection ts) {

		TimeSeries s1 = new TimeSeries("ml8");
		TimeSeries s2 = new TimeSeries("mh10");

		for (int i = 0; i < quotes.size(); i++) {
			Quote q = quotes.get(i);
			try {
				s1.add(new Day(q.date), q.low);
				s2.add(new Day(q.date), q.high);
			} catch (Exception e) {
			}
		}

		int l8 = 8;
		int h10 = 10;
		if (ind.length > 1) {
			l8 = Integer.parseInt(ind[1]);
		}
		if (ind.length > 2) {
			h10 = Integer.parseInt(ind[2]);
		}

		TimeSeries ds1 = MovingAverage.createMovingAverage(s1, "LT", l8, l8);
		TimeSeries ds2 = MovingAverage.createMovingAverage(s2, "LT", h10, h10);

		ts.addSeries(ds1);
		ts.addSeries(ds2);

		MyPlot plot = new MyPlot(ts, new Color[] { Color.red, Color.blue });

		return plot;

	}
//
//	MyPlot getSMA1(String[] ind, Quotes quotes, TimeSeriesCollection ts) {
//
//		TimeSeries s1 = new TimeSeries("ml8");
////		TimeSeries s2 = new TimeSeries("mh10");
//
//		for (int i = 0; i < quotes.size(); i++) {
//			Quote q = quotes.get(i);
//			try {
//				s1.add(new Day(q.date), q.close);
////				s2.add(new Day(q.date), q.high);
//			} catch (Exception e) {
//			}
//		}
//
//		int l8 = 8;
//		int h10 = 10;
//		if (ind.length > 1) {
//			l8 = Integer.parseInt(ind[1]);
//			TimeSeries ds1 = MovingAverage.createMovingAverage(s1, "LT", l8, l8);
//			ts.addSeries(ds1);
//		}
//		if (ind.length > 2) {
//			h10 = Integer.parseInt(ind[2]);
//			TimeSeries ds2 = MovingAverage.createMovingAverage(s1, "LT", h10, h10);
//			ts.addSeries(ds2);
//		}
//
//
//
//		MyPlot plot = new MyPlot(ts, new Color[] { Color.red, Color.blue });
//
//		return plot;
//
//	}
//
	Date addDays(Date d, double nd) {
		return new Date(d.getTime() + (int) (nd * DAY));
	}

	MyPlot getLine(String[] ind, Quotes quotes, TimeSeriesCollection ts) {

		TimeSeries s1 = new TimeSeries("ml8");

		for (int i = 0; i < quotes.size(); i++) {
			Quote q = quotes.get(i);
			try {
				s1.add(new Day(addDays(q.date, 0.9050)), q.close);
			} catch (Exception e) {
			}
		}
		ts.addSeries(s1);

		MyPlot plot = new MyPlot(ts, new Color[] { Color.black });

		return plot;

	}

	MyPlot getTurtle(String[] ind, Quotes quotes, TimeSeriesCollection ts) {
		TimeSeries longEntry = new TimeSeries("le");
		TimeSeries longExit = new TimeSeries("lx");
		TimeSeries shortEntry = new TimeSeries("se");
		TimeSeries shortExit = new TimeSeries("sx");

		float[] lentry = new float[quotes.size()], lexit = new float[quotes
				.size()], sentry = new float[quotes.size()], sexit = new float[quotes
				.size()];
		Calc.calcTurtle(quotes, 20, 10, lentry, lexit, sentry, sexit);
		for (int i = 0; i < quotes.size() - 21; i++) {
			Quote q = quotes.get(i);
			try {
				Day d = new Day(q.date);
				longEntry.add(d, lentry[i]);
				longExit.add(d, lexit[i]);
				shortEntry.add(d, sentry[i]);
				shortExit.add(d, sexit[i]);
			} catch (Exception e) {
				ValTrader.msgBox(e.getMessage());
			}
		}

		longEntry.setMaximumItemCount(quotes.size() - 22);
		longExit.setMaximumItemCount(quotes.size() - 22);
		shortEntry.setMaximumItemCount(quotes.size() - 22);
		shortExit.setMaximumItemCount(quotes.size() - 22);

		String arg = null;
		if (ind.length > 1)
			arg = ind[1];

		Color col1 = Color.green, col2 = Color.magenta, col3 = Color.red, col4 = Color.blue;
		if (arg == null || arg.equals("long")) {
			ts.addSeries(longEntry);
			ts.addSeries(longExit);
			if (arg != null) {
				col1 = Color.green;
				col2 = Color.magenta;
			}
		}
		if (arg == null || arg.equals("short")) {
			ts.addSeries(shortEntry);
			ts.addSeries(shortExit);

			if (arg != null) {
				col1 = Color.red;
				col2 = Color.blue;
			}
		}

		return new MyPlot(ts, new Color[] { col1, col2, col3, col4 });
	}

	MyPlot getBand(Quotes quotes, TimeSeriesCollection ts) {

		float[] ub = new float[quotes.size()];
		float[] lb = new float[quotes.size()];

		Calc.calcBand(quotes.getClose(), ub, lb);

		TimeSeries ubs = new TimeSeries("ub");
		TimeSeries lbs = new TimeSeries("lb");

		for (int i = 0; i < quotes.size(); i++) {
			Quote q = quotes.get(i);
			try {
				ubs.add(new Day(q.date), ub[i]);
				lbs.add(new Day(q.date), lb[i]);
			} catch (Exception e) {
			}
		}

		ubs.setMaximumItemCount(quotes.size() - 22);
		lbs.setMaximumItemCount(quotes.size() - 22);

		ts.addSeries(ubs);
		ts.addSeries(lbs);

		return new MyPlot(ts, new Color[] { Color.magenta, Color.green });

	}

	MyPlot getEMA(String[] ind, Quotes quotes, TimeSeriesCollection ts) {

		int a1 = 13;
		int a2 = 26;

		if (ind.length > 1)
			a1 = Integer.parseInt(ind[1]);
		if (ind.length > 2)
			a2 = Integer.parseInt(ind[2]);

		float[] ema13 = Calc.calcEMA(quotes.getClose(), a1);
		float[] ema26 = Calc.calcEMA(quotes.getClose(), a2);

		TimeSeries s1 = new TimeSeries("ema13");
		TimeSeries s2 = new TimeSeries("ema26");

		for (int i = 0; i < quotes.size(); i++) {
			Quote q = quotes.get(i);
			try {
				s1.add(new Day(q.date), ema13[i]);
				s2.add(new Day(q.date), ema26[i]);
			} catch (Exception e) {
			}
		}

		s1.setMaximumItemCount(quotes.size() - a1 - 5);
		s2.setMaximumItemCount(quotes.size() - a2 - 5);

		ts.addSeries(s1);
		ts.addSeries(s2);

		return new MyPlot(ts, new Color[] { Color.green, Color.magenta });
	}

	// MyPlot getSMA(String[] ind, Quotes quotes, TimeSeriesCollection
	// collection) {
	// int a1 = 20;
	// int a2 = 0;
	//
	// if (ind.length > 1)
	// a1 = Integer.parseInt(ind[1]);
	// if (ind.length > 2)
	// a2 = Integer.parseInt(ind[2]);
	//
	// TimeSeries dataset1 = MovingAverage.createMovingAverage(t1, "LT", a1,
	// a1);
	// TimeSeries dataset2 = null;
	//
	// if (a2 > 1)
	// dataset2 = MovingAverage.createMovingAverage(t1, "LT", a2, a2);
	//
	// dataset1.setMaximumItemCount(quotes.size() - a1);
	// if (a2 > 1)
	// dataset2.setMaximumItemCount(quotes.size() - a2);
	//
	// collection.addSeries(dataset1);
	// if (a2 > 1)
	// collection.addSeries(dataset2);
	//
	// if (a2 > 1)
	// return new MyPlot(collection, new Color[] { Color.blue, Color.red });
	// else
	// return new MyPlot(collection, new Color[] { Color.blue });
	// }

	Color[] colorSet = { Color.black, Color.red, Color.green, Color.yellow,
			Color.yellow, Color.magenta, Color.gray, Color.lightGray };

	MyPlot getSMA(String[] ind, Quotes quotes, TimeSeriesCollection collection) {

		if (ind.length <= 1)
			ind = new String[] { "sma", "22" };

		Color[] cols = new Color[ind.length - 1];

		TimeSeries t1 = new TimeSeries("ml8");

		for (int i = 0; i < quotes.size(); i++) {
			Quote q = quotes.get(i);
			try {
				t1.add(new Day(q.date), q.close);
			} catch (Exception e) {
			}
		}

		for (int i = 1; i < ind.length; i++) {
			 int a1 = Integer.parseInt(ind[i]);
			 TimeSeries dataset = MovingAverage.createMovingAverage(t1, "LT",
			 a1, a1);
			 dataset.setMaximumItemCount(quotes.size() - a1);
			 collection.addSeries(dataset);
			 cols[i - 1] = colorSet[i - 1];
		}

		return new MyPlot(collection, cols);

	}

	class RsText {
		public RsText(String text, float pos, Color col) {
			super();
			this.pos = pos;
			this.text = text;
			this.col = col;
		}

		float pos;
		String text;
		Color col;
	}

	RsText[] rsTexts = null;

	// MyPlot getRS_good(String[] ind, Quotes quotes, TimeSeriesCollection ts)
	// throws Exception {
	//
	// if (ind.length <= 1)
	// ind = new String[] { "RS", "^GSPC" };
	//
	// int nd1 = quotes.size();
	// y2text = ind[1].toUpperCase();
	// Quotes quotes1 = Quotes.getQuotes(ind[1], "E", 0, nd1, "d");
	//
	// TimeSeries s1 = new TimeSeries("rs1");
	//
	// float factor = quotes1.getClose(nd1 - 1) / quotes.getClose(nd1 - 1);
	//
	// for (int i = 0; i < nd1; i++) {
	// Quote q1 = quotes1.get(i);
	// Quote q = quotes.get(i);
	// s1.add(new Day(q.date), q1.close / factor);
	// if (i == 0) {
	// y2 = q1.close / factor;
	// }
	// }
	//
	// // s1.setMaximumItemCount(nd1-11);
	//
	// ts.addSeries(s1);
	//
	// MyPlot plot = new MyPlot(ts, new Color[] { Color.red });
	// return plot;
	//
	// }
	//

	MyPlot getRS(String[] ind, Quotes quotes, TimeSeriesCollection ts)
			throws Exception {

		Color[] sysColors = { Color.blue, Color.red, Color.black, Color.cyan,
				Color.gray, Color.green, Color.magenta, Color.orange,
				Color.pink };

		if (ind.length <= 1)
			return null;
		// ind = new String[] { "RS", "^GSPC" };

		int nd1 = quotes.size();

		Color[] colors = new Color[ind.length - 1];
		colors[0] = sysColors[0];
		rsTexts = new RsText[ind.length];
		rsTexts[0] = new RsText(quotes.getSymbol(),
				(quotes.getClose(0) - quotes.getClose(quotes.size() - 1))
						/ quotes.getClose(quotes.size() - 1) * 100,
				sysColors[0]);

		for (int sym = 1; sym < ind.length; sym++) {
			Quotes quotes1 = Quotes.getQuotes(ind[sym], "E", 0, nd1 + 100, "d");

			TimeSeries s1 = new TimeSeries("rs:" + sym);

			float base = quotes1.getClose(quotes.size() - 1);

			for (int i = 0; i < nd1; i++) {
				Quote q1 = quotes1.get(i);
				float val = (q1.close - base) / base * 100;
				try {
					s1.add(new Day(q1.date), val);
				} catch (Exception e) {
				}
				if (i == 0) {
					rsTexts[sym] = new RsText(quotes1.getSymbol(), val,
							sysColors[sym]);
				}
			}

			// s1.setMaximumItemCount(nd1-11);

			ts.addSeries(s1);
			colors[sym - 1] = sysColors[sym];
		}

		MyPlot plot = new MyPlot(ts, colors);
		return plot;

	}

	MyPlot getUpperATR(String[] ind, Quotes quotes, TimeSeriesCollection ts) {

		if (ts == null)
			ts = new TimeSeriesCollection();

		float atrVal = 2;
		if (ind.length > 1)
			atrVal = Float.parseFloat(ind[1]);
		

		final TimeSeries s1 = new TimeSeries("uATR");
		final TimeSeries s2 = new TimeSeries("lATR");

		float[] atr = Calc.calcATR(quotes, 20, false); // 20 day ATR
		
		boolean along = (ind.length > 2) && (ind[2].equals("long"));
		boolean ashort = (ind.length > 2) && (ind[2].equals("short"));
				
//		for (int i = 0; i < quotes.size() - 20; i++) {
//			Quote q = quotes.get(i);
//			try {
//				s1.add(new Day(q.date), q.low - (atrVal * atr[i]));
//				s2.add(new Day(q.date), q.high + (atrVal * atr[i]));
//			} catch (Exception e) {
//			}
//		}
		float plval = 0, psval = 999999;
		for (int i = quotes.size() - 21; i >= 0; i--) {
			Quote q = quotes.get(i);
			try {
				float val;
				val = q.low - (atrVal * atr[i]);
				if(along && val < plval) val = plval; else plval = val;
				s1.add(new Day(q.date), val);
				val = q.high + (atrVal * atr[i]);
				if(ashort && val > psval) val = psval; else psval = val;
				s2.add(new Day(q.date), val);
			} catch (Exception e) {
			}
		}

		if(ind.length > 2) {
			if(along) ts.addSeries(s1);
				
			if(ashort) ts.addSeries(s2);
		} else {
			ts.addSeries(s1);
			ts.addSeries(s2);
		}
		return new MyPlot(ts, new Color[] { Color.magenta, Color.black });
	}

	MyPlot getLowerATR(String[] ind, final Quotes quotes) {

		TimeSeriesCollection ts = new TimeSeriesCollection();

		final TimeSeries s1 = new TimeSeries("uATR");
		TimeSeries s2 = new TimeSeries("atr-avg");

		// final float[] atr = Calc.calcATR(quotes, 20); // 20 day ATR
		boolean isavg = false;
		if (ind.length > 1) {
			isavg = (ind[1].equals("%"));
		}

		final float[] atr = Calc.calcATR(quotes, 20, isavg); // 20 day ATR

		int avglen = 22;
		float[] atravg = Calc.calcSMA(atr, avglen);

		for (int i = 0; i < quotes.size() - 20; i++) {
			Quote q = quotes.get(i);
			float val = atr[i];
			// if (ind.length > 1) {
			// if (ind[1].equals("%"))
			// val = atr[i] / quotes.getClose(i) * 100;
			// }
			try {
				s1.add(new Day(q.date), val);
			} catch (Exception e) {
			}
			if (i < atr.length - avglen) {
				s2.add(new Day(q.date), atravg[i]);
			}

		}

		s1.setMaximumItemCount(quotes.size() - 20);
		s2.setMaximumItemCount(atr.length - avglen);

		ts.addSeries(s1);
		ts.addSeries(s2);

		StandardXYItemRenderer r = new StandardXYItemRenderer() {
			@Override
			public Paint getItemPaint(int row, int col) {
				if (row == 1)
					return Color.black;
				col = s1.getMaximumItemCount() - col - 1;
				if (atr[col] / quotes.getClose(col) * 100 > 3) {
					return Color.red;
				}
				return Color.blue;
			}
		};

		return new MyPlot(ts, new Color[] { Color.blue }, r);
	}

	MyPlot getMACD(String[] ind, final Quotes quotes) {

		TimeSeriesCollection ts = new TimeSeriesCollection();

		final TimeSeries s1 = new TimeSeries("macd");

		final float[] macd = Calc.calcMACD(quotes.getClose());

		for (int i = 0; i < quotes.size() - 35; i++) {
			Quote q = quotes.get(i);
			try {
				s1.add(new Day(q.date), macd[i]);
			} catch (Exception e) {
			}

		}

		s1.setMaximumItemCount(quotes.size() - 36);

		ts.addSeries(s1);
		XYBarRenderer r = new XYBarRenderer(0.5) {
			@Override
			public Paint getItemPaint(int row, int col) {
				col = s1.getMaximumItemCount() - col - 1;
				if (macd[col] > macd[col + 1]) {
					return Color.blue;
				}
				return Color.red;
			}
		};

		r.setShadowVisible(false);

		return new MyPlot(ts, new Color[] { Color.blue }, r);
	}


	// get 3 day candle for each day
	MyPlot getCandle(String[] ind, final Quotes quotes) {

		TimeSeriesCollection ts = new TimeSeriesCollection();
		final TimeSeries s1 = new TimeSeries("candle");
		OHLCSeries ds = new OHLCSeries("");

		for (int i = 0; i < quotes.size() - 4; i++) {
			float o = quotes.getOpen(i+2);
			float c = quotes.getClose(i);
			float l = quotes.getLow(i);
			float h = quotes.getHigh(i);
			for(int j = i; j < i + 3; j++) {
				if(h < quotes.getHigh(j)) h = quotes.getHigh(j);
				if(l > quotes.getLow(j)) l = quotes.getLow(j);
			}
			
			Quote q = quotes.get(i);
			try {
				ds.add(new Day(q.date), o,h,l,c);
			} catch (Exception e) {
			}
		}

		OHLCSeriesCollection dataset = new OHLCSeriesCollection();
		dataset.addSeries(ds);


		ds.setMaximumItemCount(quotes.size() - 3);

		// 1. candle renderer
		CandlestickRenderer r = new CandlestickRenderer() {
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

		ts.addSeries(s1);

		return new MyPlot(ts, new Color[] { Color.blue }, r);
	}

	MyPlot getTR(String[] ind, final Quotes quotes) {

		TimeSeriesCollection ts = new TimeSeriesCollection();

		final TimeSeries s1 = new TimeSeries("TR");

		final float[] macd = Calc.calcTR(quotes);

		for (int i = 0; i < quotes.size() - 35; i++) {
			Quote q = quotes.get(i);
			try {
				s1.add(new Day(q.date), macd[i]);
			} catch (Exception e) {
			}

		}

		s1.setMaximumItemCount(quotes.size() - 36);

		ts.addSeries(s1);
		XYBarRenderer r = new XYBarRenderer(0.5) {
			@Override
			public Paint getItemPaint(int row, int col) {
				col = s1.getMaximumItemCount() - col - 1;
				if (macd[col] > macd[col + 1]) {
					return Color.blue;
				}
				return Color.red;
			}
		};

		r.setShadowVisible(false);

		return new MyPlot(ts, new Color[] { Color.blue }, r);
	}

	MyPlot getTrend(String[] ind, final Quotes quotes) {

		TimeSeriesCollection ts = new TimeSeriesCollection();

		final TimeSeries s1 = new TimeSeries("trend");

		final int[] macd = Scanner.getMOM1(quotes);

		for (int i = 0; i < quotes.size() - 5; i++) {
			Quote q = quotes.get(i);
			try {
				s1.add(new Day(q.date), macd[i]);
			} catch (Exception e) {
			}
		}

		s1.setMaximumItemCount(quotes.size() - 6);

		ts.addSeries(s1);
		XYBarRenderer r = new XYBarRenderer(0.5) {
			@Override
			public Paint getItemPaint(int row, int col) {
				col = s1.getMaximumItemCount() - col - 1;

				return (macd[col] > 0) ? Color.blue : Color.red;
			}
		};

		r.setShadowVisible(false);

		return new MyPlot(ts, new Color[] { Color.blue }, r);
	}

	MyPlot getVolume(String[] ind, final Quotes quotes) {

		TimeSeriesCollection ts1 = new TimeSeriesCollection();
		TimeSeriesCollection ts2 = new TimeSeriesCollection();

		TimeSeries s1 = new TimeSeries("vol");
		TimeSeries s2 = new TimeSeries("volavg");

		final float[] volavg = Calc.calcSMA(quotes.getVolume(), 50);

		for (int i = 0; i < quotes.size(); i++) {
			Quote q = quotes.get(i);
			try {
				s1.add(new Day(q.date), q.volume);
				s2.add(new Day(q.date), volavg[i]);
			} catch (Exception e) {
			}
		}

		// s2.setMaximumItemCount(quotes.size() - 50);

		ts1.addSeries(s1);
		ts2.addSeries(s2);

		XYBarRenderer volRenderer = new XYBarRenderer(0.5) {
			@Override
			public Paint getItemPaint(int row, int col) {
				// if(col <= 50) return Color.blue;
				col = quotes.size() - col - 1;
				if (col >= quotes.size() - 1)
					return Color.black;
				// if (quotes.getVolume(col) > volavg[col])
				return (quotes.getVolume(col) > quotes.getVolume(col + 1)) ? Color.blue
						: Color.red;
			}
		};

		volRenderer.setShadowVisible(false);
		StandardXYItemRenderer avgRenderer = new StandardXYItemRenderer() {
			@Override
			public Paint getItemPaint(int row, int col) {
				return Color.red;
			}
		};

		MyPlot volumePlot = new MyPlot(ts1, new Color[] { Color.blue },
				volRenderer);

		MyPlot avgVolumePlot = new MyPlot(ts2, new Color[] { Color.red },
				avgRenderer);
		volumePlot.subplot = avgVolumePlot;

		return volumePlot;
	}

	void setupPopup() {
		// JPopupMenu popup = new JPopupMenu();
		JPopupMenu popup = this.getPopupMenu();
		JMenuItem clearText = new JMenuItem();

		popup.insert(clearText, 0);
		clearText.setAction(new AbstractAction("Clear Text") {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFreeChart chart = getChart();
				CombinedDomainXYPlot cdplot = (CombinedDomainXYPlot) chart
						.getPlot();
				XYPlot subplot1 = (XYPlot) cdplot.getSubplots().get(0);

				removeAnnotations(subplot1);
				((AbstractSeriesDataset) subplot1.getDataset(0))
						.seriesChanged(null); // refresh

			}
		});

		JMenuItem addText = new JMenuItem();

		popup.insert(addText, 0);
		popup.insert(new JSeparator(), 2);
		addText.setAction(new AbstractAction("Add Text") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (savedMouseEvent == null) {
					ValTrader.msgBox("Left click first!");
					return;
				}
				String text = ValTrader.getInput("What do you want to write?",
						null);
				ChartMouseEvent event = savedMouseEvent;

				int mouseX = event.getTrigger().getX();
				int mouseY = event.getTrigger().getY();
				Point2D p = translateScreenToJava2D(new Point(mouseX, mouseY));
				XYPlot plot = (XYPlot) getChart().getPlot();
				ChartRenderingInfo info = getChartRenderingInfo();
				Rectangle2D dataArea = info.getPlotInfo().getDataArea();

				ValueAxis domainAxis = plot.getDomainAxis();
				RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();

				XYPlot subplot1 = (XYPlot) ((CombinedDomainXYPlot) plot)
						.getSubplots().get(0);

				NumberAxis rangeAxis = (NumberAxis) subplot1.getRangeAxis(0);
				RectangleEdge rangeAxisEdge = subplot1.getRangeAxisEdge();
				double chartX = domainAxis.java2DToValue(p.getX(), dataArea,
						domainAxisEdge);
				double chartY = rangeAxis.java2DToValue(p.getY(), dataArea,
						rangeAxisEdge);

				writeText(subplot1, (long) chartX, (float) chartY, text);
				Quotes.saveText(app.symbol.getText(), (long) chartX,
						(float) chartY, text);
				stopMouseMove = false;
			}

		});
	}

	class MyPlot {
		public MyPlot(TimeSeriesCollection dataset, Color[] colors) {
			super();
			this.dataset = dataset;
			this.colors = colors;
			renderer = null;
		}

		public MyPlot(TimeSeriesCollection dataset, Color[] colors,
				XYItemRenderer renderer) {
			super();
			this.dataset = dataset;
			this.colors = colors;
			this.renderer = renderer;
		}

		TimeSeriesCollection dataset;
		Color[] colors;
		XYItemRenderer renderer;

		MyPlot subplot;
	}

	// void drawTrade() {
	// // 1. Ask for size (+ve or -ve)
	// // 2. price = mid point of the day
	// // 3. drop stop
	// // 4. draw trailing stop
	//
	// if (curMouseEvent == null) {
	// return;
	// }
	//
	// String tradeSizeStr = app.getInput("Trade Size", "");
	// int tradeSize = Integer.parseInt(tradeSizeStr);
	// String stopStr = app.getInput("Stop price", "");
	// float stopPrice = Float.parseFloat(stopStr);
	//
	// ChartMouseEvent event = curMouseEvent;
	//
	// int mouseX = event.getTrigger().getX();
	// int mouseY = event.getTrigger().getY();
	// Point2D p = translateScreenToJava2D(new Point(mouseX, mouseY));
	// XYPlot plot = (XYPlot) getChart().getPlot();
	// ChartRenderingInfo info = getChartRenderingInfo();
	// Rectangle2D dataArea = info.getPlotInfo().getDataArea();
	//
	// ValueAxis domainAxis = plot.getDomainAxis();
	// RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();
	//
	// XYPlot subplot1 = (XYPlot) ((CombinedDomainXYPlot) plot).getSubplots()
	// .get(0);
	//
	// NumberAxis rangeAxis = (NumberAxis) subplot1.getRangeAxis(0);
	// RectangleEdge rangeAxisEdge = subplot1.getRangeAxisEdge();
	// double chartX = domainAxis.java2DToValue(p.getX(), dataArea,
	// domainAxisEdge);
	// double chartY = rangeAxis.java2DToValue(p.getY(), dataArea,
	// rangeAxisEdge);
	//
	// // drawVerticalLine(pricePlot, (long) chartX, (float) chartY);
	// Date dd = new Date((long) chartX);
	// long day = getQuoteDay(dd);
	// Quote q = getQuote(dd);
	//
	// if (q == null) {
	// ValTrader.msgBox("q == null");
	// return;
	// }
	//
	// float entryPrice = (q.low + q.high)/2;
	//
	// Color col = (tradeSize > 0) ? Color.blue : Color.red;
	// drawHorizontalLine(pricePlot, (long)chartX, entryPrice, (float)1, col);
	//
	// col = (tradeSize < 0) ? Color.blue : Color.red;
	// drawHorizontalLine(pricePlot, (long)chartX, stopPrice, (float)2.5, col);
	// // find trailing stop
	//
	//
	// if(tradeSize < 0) {
	// float ll = q.low;
	// for(int i = (int)day; i >= 0; i--) {
	// if(ll > quotes.getLow(i)) ll = quotes.getLow(i);
	// }
	// // protect 50% of profit
	// float profit = entryPrice - ll;
	// profit /= 2; // 50%
	// stopPrice = entryPrice - profit; // short position
	// } else {
	// float hh = q.high;
	// for(int i = (int)day; i >= 0; i--) {
	// if(hh < quotes.getHigh(i)) hh = quotes.getHigh(i);
	// }
	// // protect 50% of profit
	// float profit = hh - entryPrice;
	// profit /= 2; // 50%
	// stopPrice = entryPrice + profit; // long position
	//
	// }
	// drawHorizontalLine(pricePlot, (long)chartX, stopPrice, (float)1.5);
	//
	// }

	void drawTrade(Trade trade) {

		long chartX = trade.entryDate.getTime();
		Date dd = new Date(chartX);
		long day = getQuoteDay(dd);
		Quote q = getQuote(dd);

		if (q == null) {
			ValTrader.msgBox("q == null");
			return;
		}

		Color col = (trade.size > 0) ? Color.blue : Color.red;
		drawHorizontalLine(pricePlot, chartX, trade.entry, (float) 1, col);

		writeText(pricePlot, quotes.getDate(0).getTime() + (2 * DAY),
				trade.entry, "Entry " + trade.entry, col);
		drawHorizontalLine(pricePlot, chartX, trade.entry, (float) 1, col);

		col = (trade.size < 0) ? Color.blue : Color.red;
		float stopPrice;
		drawHorizontalLine(pricePlot, (long) chartX, trade.stop, (float) 2.5,
				col);
		writeText(pricePlot, quotes.getDate(0).getTime() + (2 * DAY),
				trade.stop, "Stop " + trade.stop, col);
		// find trailing stop

		if (trade.size < 0) {
			float ll = q.low;
			for (int i = (int) day; i >= 0; i--) {
				if (ll > quotes.getLow(i))
					ll = quotes.getLow(i);
			}
			// protect 50% of profit
			float profit = trade.entry - ll;
			profit *= 0.6; // 60%
			stopPrice = trade.entry - profit; // short position

//			float[] mac = Calc.calcSMA(quotes.getHigh(), 10);

		} else {
			float hh = q.high;
			for (int i = (int) day; i >= 0; i--) {
				if (hh < quotes.getHigh(i))
					hh = quotes.getHigh(i);
			}
			// protect 50% of profit
			float profit = hh - trade.entry;
			profit *= 0.6; // protect 60%
			stopPrice = trade.entry + profit; // long position

		}
		drawHorizontalLine(pricePlot, (long) chartX, stopPrice, (float) 1.5);
		writeText(pricePlot, quotes.getDate(0).getTime() + (2 * DAY),
				stopPrice, "Trailing (25% profit): " + stopPrice, col);

		drawHorizontalLine(pricePlot, (long) chartX, trade.target, (float) 1.5);
		writeText(pricePlot, quotes.getDate(0).getTime() + (2 * DAY),
				trade.target, "Target " + trade.target, col);

	}

	public void drawVerticalLine() {
		if (curMouseEvent == null) {
			return;
		}
		ChartMouseEvent event = curMouseEvent;

		int mouseX = event.getTrigger().getX();
		int mouseY = event.getTrigger().getY();
		Point2D p = translateScreenToJava2D(new Point(mouseX, mouseY));
		XYPlot plot = (XYPlot) getChart().getPlot();
		ChartRenderingInfo info = getChartRenderingInfo();
		Rectangle2D dataArea = info.getPlotInfo().getDataArea();

		ValueAxis domainAxis = plot.getDomainAxis();
		RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();

		XYPlot subplot1 = (XYPlot) ((CombinedDomainXYPlot) plot).getSubplots()
				.get(0);

		NumberAxis rangeAxis = (NumberAxis) subplot1.getRangeAxis(0);
		RectangleEdge rangeAxisEdge = subplot1.getRangeAxisEdge();
		double chartX = domainAxis.java2DToValue(p.getX(), dataArea,
				domainAxisEdge);
		double chartY = rangeAxis.java2DToValue(p.getY(), dataArea,
				rangeAxisEdge);

		drawVerticalLine(pricePlot, (long) chartX, (float) chartY);
		drawVerticalLine(lowerIndPlot1, (long) chartX, (float) 0);
		drawVerticalLine(lowerIndPlot2, (long) chartX, (float) 0);
	}

	public void drawHorizontalLine(char key) {
		if (curMouseEvent == null) {
			return;
		}
		ChartMouseEvent event = curMouseEvent;

		int mouseX = event.getTrigger().getX();
		int mouseY = event.getTrigger().getY();
		Point2D p = translateScreenToJava2D(new Point(mouseX, mouseY));
		XYPlot plot = (XYPlot) getChart().getPlot();
		ChartRenderingInfo info = getChartRenderingInfo();
		Rectangle2D dataArea = info.getPlotInfo().getDataArea();

		ValueAxis domainAxis = plot.getDomainAxis();
		RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();

		XYPlot subplot1 = (XYPlot) ((CombinedDomainXYPlot) plot).getSubplots()
				.get(0);

		// NumberAxis rangeAxis = (NumberAxis) subplot1.getRangeAxis(0);
		// RectangleEdge rangeAxisEdge = subplot1.getRangeAxisEdge();
		double chartX = domainAxis.java2DToValue(p.getX(), dataArea,
				domainAxisEdge);
		Date dd = new Date((long) chartX);
		Quote q = getQuote(dd);

		// double chartY = rangeAxis.java2DToValue(p.getY(), dataArea,
		// rangeAxisEdge);
		double chartY = (key == 'h') ? q.low : q.high;

		chartX -= 10 * DAY;

		drawHorizontalLine(pricePlot, (long) chartX, (float) chartY);

	}

	public void saveChart(String outdir, String symbol) {
		// String outdir = props.get("saveFolder");

		try {
			String dt = Utils.formatDate(new java.util.Date(), "yyyy-MM-dd");
			String filename = outdir + symbol + "-" + dt + ".png";
			FileOutputStream out = new FileOutputStream(filename);
			BufferedImage chartImage = chart.createBufferedImage(getWidth(),
					getHeight(), null);
			ImageIO.write(chartImage, "png", out);
			out.close();
			ValTrader.msgBox("Chart saved in " + filename);
		} catch (Exception e) {
			e.printStackTrace();
			ValTrader.msgBox(e.getMessage());
		}
	}

	public void waveBack() {
		wave.back();
		drawWave(wave);
	}

	public void saveWave(String symbol) {
		for (int i = 0; i < wave.size(); i++) {
			Quotes.addWave(symbol, i, wave.getDate(i), wave.getValue(i));
		}
	}

	public void addNotes() {
		String text = ValTrader.getInput("Enter Notes", null);
		if (text == null)
			return;
		long x = quotes.getDate(0).getTime();
		float y = quotes.getClose(0);
		String sym = quotes.getSymbol();
		Quotes.saveText(sym, x, y, text);
	}

	public void compare(String sym, String compSym) throws Exception {
		showChart(sym, "E", "d", 0, 177, "rs " + compSym, null, null, "",
				"line");

	}

	public void testTrade() {
		int [] sigs = Scanner.checkForMac(quotes);
		int pos = 0;
		float pnl = 0;
		float lastEntry = 0;
		int entryDay = 0;
		for(int i = sigs.length-1; i > 0; i--) {
			float op = quotes.getOpen(i - 1);
			if(pos != 0) {
				// check if stopped
				if(pos == 1) {
					// stop below low of the day
					if(quotes.getLow(i) < quotes.getLow(entryDay + 1)) {
						// long stopped out
						System.out.println("Entry:"+entryDay + " Pos:" + pos + " Entry: " + lastEntry + " Exit: " + op + " pnl:" + (op - lastEntry) * pos + " Exit:" + i);
						pos = 0;
					}
				} else if(pos == -1) {
					// stop above the high of the signal day
					if(quotes.getHigh(i) > quotes.getHigh(entryDay + 1)) {
						// long stopped out
						System.out.println("Entry:"+entryDay + " Pos:" + pos + " Entry: " + lastEntry + " Exit: " + op + " pnl:" + (op - lastEntry) * pos + " Exit:" + i);
						pos = 0;
					}
					
				}
			}
			if(sigs[i] == 1) {
				if(pos == 0) {
					pos = 1;
					lastEntry = op;
					entryDay = i - 1;
				} else if(pos == -1) {
					// close short
					System.out.println("Entry:"+entryDay + " Pos:" + pos + " Entry: " + lastEntry + " Exit: " + op + " pnl:" + (op - lastEntry) * pos + " Exit:" + (i-1));
					pnl += (op - lastEntry) * pos;
					pos = 1;
					lastEntry = op;
					entryDay = i - 1;
				}
			} else if(sigs[i] == -1) {
				if(pos == 0) {
					pos = -1;
					lastEntry = op;
					entryDay = i - 1;
				} else if(pos == 1) {
					// close short
					System.out.println("Entry:" + entryDay + " Pos:" + pos + " Entry: " + lastEntry + " Exit: " + op + " pnl:" + (op - lastEntry) * pos + " Exit:" + (i-1));
					pnl += (op - lastEntry) * pos;
					pos = -1;
					lastEntry = op;
					entryDay = i - 1;
				}
			}
		}
//		this.app.msgBox("PnL:"+pnl);
		System.out.println("====PnL:"+pnl + "Total move:" + (quotes.getClose(0) - quotes.getClose(quotes.size() - 1)));
		app.msgBox("====PnL:"+pnl + "Total move:" + (quotes.getClose(0) - quotes.getClose(quotes.size() - 1)));
	}
}

// class Wave {
// long[] wavePositions = new long[5];
// int waveIdx = 0;
//
// void add(long x) {
// wavePositions[waveIdx] = x;
// waveIdx++;
//
// }

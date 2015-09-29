/*
 * ValTrader - A Swing based trading research tool.
 * Uses Jfreechart.
 * 
 * Mar 15, 2013.
 *  
 * (c) VeeYesYes, 2013
 */

package com.valtrader.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;

import com.valtrader.data.MyDatabase;
import com.valtrader.data.MyStatement;
import com.valtrader.service.Quotes;
import com.valtrader.service.Scanner;
import com.valtrader.utils.Callback;
import com.valtrader.utils.Utils;

public class ValTrader extends JFrame {
	static Props props;

	static public void main(String[] args) throws Exception {
		props = new Props();
		new ValTrader();
	}

	static public ValTrader app;

	static public void setStatus1(String str) {
		app.setStatus(0, str);
	}
	ValTrader() throws Exception {
		super();
		app = this;
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setIconImage(msgicon.getImage());
		setTitle(props.get("appName", Defaults.appName));
		setJMenuBar(getMenu());

		// this is the main content panel. The container for the toolbar,
		// charts etc.

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		contentPanel.add(getToolBar(), BorderLayout.PAGE_START);
		contentPanel.add(chartPanel = new ValChart(this), BorderLayout.CENTER);
		contentPanel.add(getStatusBar(), BorderLayout.SOUTH);
		setContentPane(contentPanel);

		pack();
		setExtendedState(MAXIMIZED_BOTH);

		loadDefaults(props);

		showChart();

		setVisible(true);
		chartPanel.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				// to handle arrow keys
				app.keyPressed(arg0);
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				// to handle keyboard shortcut
				app.keyTyped(arg0.getKeyChar());
			}
		});

		chartPanel.requestFocus();

		Quotes.loadSymbolsMap();

	}

	private void loadDefaults(Props props2) {
		symbol.setText(props.get("symbol", Defaults.symbol));
		symbolType.setText(props.get("symbolType", Defaults.symbolType));
	}

	protected void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_UP:
			if (symbolsList.getSelectedIndex() < 1) {
				setProgress(0);
				msgBox("That's all folks!");
				return;
			}
			symbolsList.setSelectedIndex(symbolsList.getSelectedIndex() - 1);
			progressBar.setMaximum(symbolsList.getItemCount() - 1);
			setProgress(symbolsList.getSelectedIndex() + 1);
			String sym = getCombo(symbolsList);
			if (sym.trim().isEmpty())
				return;
			symbol.setText(sym.split(" ")[0]);
			showChart(getCombo(symbolsList));

			break;
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_DOWN:
			if (symbolsList.getSelectedIndex() >= symbolsList.getItemCount() - 1) {
				setProgress(symbolsList.getItemCount());
				msgBox("That's all folks!");
				return;
			}
			symbolsList.setSelectedIndex(symbolsList.getSelectedIndex() + 1);
			progressBar.setMaximum(symbolsList.getItemCount());
			setProgress(symbolsList.getSelectedIndex() + 1);
			symbol.setText(getCombo(symbolsList).split(" ")[0]);
			showChart(getCombo(symbolsList));
			break;
		}
	}

	ValChart chartPanel;
	static String prevDef = "";

	// get input string from user
	public static String getInput(String prompt, String def) {
		if (def == null)
			def = prevDef;
		
		
		String tmp = (String) JOptionPane.showInputDialog(null, prompt,
				props.get("appName", Defaults.appName),
				JOptionPane.OK_CANCEL_OPTION, msgicon, null, def);
		if(tmp == null) return null;
		prevDef = tmp;
		return prevDef;
	}

	// save setup values.
	void saveSetup() {
		try {
			MyDatabase db = new MyDatabase();
			String setup = getInput("Setup Name", getCombo(systems));

			if (setup == null)
				return;

			db.execute(String.format("delete from setup where name = '%s'",
					setup));

			String insert = "insert into setup(name, param, value) values('%s', '%s', '%s')";

			db.execute(String.format(insert, setup, "watchlist",
					getCombo(watchlists)));
			db.execute(String.format(insert, setup, "uind", upperInd.getText()));
			db.execute(String.format(insert, setup, "ind1", lowerInd1.getText()));
			db.execute(String.format(insert, setup, "ind2", lowerInd2.getText()));
			db.execute(String.format(insert, setup, "period",
					this.period.getText()));
			db.execute(String.format(insert, setup, "scan",
					this.system.getText()));
			db.execute(String.format(insert, setup, "numdays",
					this.numdays.getText()));
			db.execute(String.format(insert, setup, "bartype",
					this.barType.getText()));

		} catch (Exception e) {
			msgBox(e.getMessage());
		}
	}

	public boolean optHLine = false, optVLine = false, optTrade = false;

	JMenuBar getMenu() {
		JMenuBar menubar = new JMenuBar();
		JMenu file = new JMenu("ValTrader");
		menubar.add(file);

		file.add(getMenuItem(new AbstractAction("Save Setup") {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveSetup();
			}
		}));
		file.addSeparator();
		file.add(getMenuItem(new AbstractAction("Save Wave") {
			@Override
			public void actionPerformed(ActionEvent e) {
				chartPanel.saveWave(symbol.getText());
			}
		}));
		file.add(getMenuItem(new AbstractAction("Drop Wave") {
			@Override
			public void actionPerformed(ActionEvent e) {
				Quotes.dropWave(symbol.getText());
			}
		}));
		file.addSeparator();
		file.add(getMenuItem(new AbstractAction("Refresh Cache") {
			@Override
			public void actionPerformed(ActionEvent e) {
				Quotes.refreshCache();
			}
		}));

		file.add(getMenuItem(new AbstractAction("Remove from Cache") {
			@Override
			public void actionPerformed(ActionEvent e) {
				Quotes.refreshCache(symbol.getText());
				showChart();
			}
		}));

		file.addSeparator();

		file.add(getMenuItem(KeyEvent.VK_K, new AbstractAction("Browse") {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (!symbolType.getText().equals("E"))
						return;
					URI uri = new URI(String.format(
							"http://finance.yahoo.com/q?s=%s&ql=1",
							symbol.getText()));
					Desktop.getDesktop().browse(uri);
				} catch (Exception exc) {
					msgBox(exc.getMessage());
				}
			}
		}));
		file.add(getMenuItem(KeyEvent.VK_O, new AbstractAction("Option") {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (!symbolType.getText().equals("E"))
						return;
					
//					URI uri = new URI(String.format(
//							"http://finance.yahoo.com/q/os?s=%s&m=%s",
//							symbol.getText(),
//							Utils.formatDate(dt, "yyyy-MM-dd")));
					URI uri = new URI(String.format(
							"http://finance.yahoo.com/q/os?s=%s&date=%d&straddle=true",
							symbol.getText(),
							getNextMonthOptionDate()));
					Desktop.getDesktop().browse(uri);
				} catch (Exception exc) {
					msgBox(exc.getMessage());
				}
			}

			private long getNextMonthOptionDate() {
				// return 3rd Friday of next month
				Calendar c = Calendar.getInstance();
				c.add(Calendar.MONTH, 1);
				for(int i = 1; i <= 28; i++) {
					c.set(Calendar.DAY_OF_MONTH, i);
					if(c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY){
						c.set(Calendar.DAY_OF_MONTH, i + 13);
						c.set(Calendar.HOUR_OF_DAY, 17);
						c.set(Calendar.MINUTE, 0);
						c.set(Calendar.SECOND, 0);
						c.set(Calendar.MILLISECOND, 0);
						return (long)(c.getTimeInMillis() / 1000L);
					}
				}
				return 0;
			}
		}));

		file.addSeparator();
		file.add(getMenuItem(new AbstractAction("Exit") {

			@Override
			public void actionPerformed(ActionEvent e) {

			}

		}));
		JMenu view = new JMenu("View");
		menubar.add(view);
		final JCheckBoxMenuItem viewText = new JCheckBoxMenuItem();
		view.add(viewText);
		viewText.setAction(new AbstractAction("Text") {

			@Override
			public void actionPerformed(ActionEvent e) {
				optViewText = viewText.getState();
			}

		});

		viewText.setState(optViewText);

		view.addSeparator();

		JCheckBoxMenuItem opt = new JCheckBoxMenuItem();
		opt.setAction(new AbstractAction("Wave") {
			@Override
			public void actionPerformed(ActionEvent e) {
				optViewWave = !optViewWave;
			}
		});
		view.add(opt);

		opt = new JCheckBoxMenuItem();
		opt.setAction(new AbstractAction("Wave B") {
			@Override
			public void actionPerformed(ActionEvent e) {
				optWaveB = !optWaveB;
			}
		});
		view.add(opt);
		
		view.addSeparator();

		opt = new JCheckBoxMenuItem();
		opt.setAction(new AbstractAction("H Line") {
			@Override
			public void actionPerformed(ActionEvent e) {
				optHLine = !optHLine;
			}

		});
		view.add(opt);

		opt = new JCheckBoxMenuItem();
		opt.setAction(new AbstractAction("V Line") {
			@Override
			public void actionPerformed(ActionEvent e) {
				optVLine = !optVLine;
			}

		});
		view.add(opt);
		view.addSeparator();
		opt = new JCheckBoxMenuItem();
		opt.setAction(new AbstractAction("Trade") {
			@Override
			public void actionPerformed(ActionEvent e) {
				optTrade = !optTrade;
			}

		});
		view.add(opt);

		JMenu watchlist = new JMenu("Watchlist");
		menubar.add(watchlist);
		watchlist.add(getMenuItem(new AbstractAction("Add") {
			@Override
			public void actionPerformed(ActionEvent e) {
				String list = getInput("Enter Watchlist", getCombo(watchlists));
				Quotes.addSymbol(list, symbol.getText(), symbolType.getText());
				symbolsList.addItem(symbol.getText());
			}
		}));

		watchlist.add(getMenuItem(new AbstractAction("Add Symbols") {
			@Override
			public void actionPerformed(ActionEvent e) {
				String list = getInput("Enter Watchlist", getCombo(watchlists));
				String symlist = getInput("Enter Symbols", "");
				Quotes.addSymbols(list, symlist);
				loadLists();
				
			}
		}));
		watchlist.add(getMenuItem(new AbstractAction("Export") {
			@Override
			public void actionPerformed(ActionEvent e) {
				String myString = Quotes.getSymbols(getCombo(watchlists));
				StringSelection stringSelection = new StringSelection (myString);
				Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard ();
				clpbrd.setContents (stringSelection, null);
				msgBox("List copied to the clipboard.");
			}
		}));
		watchlist.add(getMenuItem(new AbstractAction("Archive") {
			@Override
			public void actionPerformed(ActionEvent e) {
				Quotes.archiveList(getCombo(watchlists));
			}
		}));

		watchlist.addSeparator();
		watchlist.add(getMenuItem(new AbstractAction("Drop Symbol") {
			@Override
			public void actionPerformed(ActionEvent e) {
				String wl = getCombo(watchlists);
				String sym = getCombo(symbolsList);
				Quotes.dropSymbol(wl, sym);
				msgBox(sym + " deleted from " + wl);
			}
		}));
		watchlist.add(getMenuItem(new AbstractAction("Drop Weatchlist") {
			@Override
			public void actionPerformed(ActionEvent e) {
				String wl = getCombo(watchlists);
				if(!getConfirm("Are you sure you want to drop watchlist: " + wl)) return;
				Quotes.dropList(wl);
				msgBox("Watchlist '" +wl + "' dropped.");
				loadLists();
			}
		}));

		watchlist.addSeparator();

		watchlist.add(getMenuItem(new AbstractAction("Copy") {
			@Override
			public void actionPerformed(ActionEvent e) {
				copyList();
			}
		}));

		watchlist.add(getMenuItem(new AbstractAction("Refresh") {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadLists();
			}
		}));

		watchlist.add(getMenuItem(new AbstractAction("Rm dups") {
			@Override
			public void actionPerformed(ActionEvent e) {
				Quotes.removeDups(getCombo(watchlists));
			}
		}));

//		JMenu tools = new JMenu("Tools");
//		menubar.add(tools);
//		tools.add(getMenuItem(new AbstractAction("Slide Show") {
//
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				// TODO Auto-generated method stub
//
//			}
//
//		}));
//
//		tools.add(getMenuItem(new AbstractAction("Wave Calc") {
//
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				// TODO Auto-generated method stub
//
//			}
//
//		}));
//
//		tools.add(getMenuItem(new AbstractAction("Position Size Calc") {
//
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				// TODO Auto-generated method stub
//
//			}
//
//		}));

		JMenu scanner = new JMenu("Search");
		menubar.add(scanner);
		scanner.add(getMenuItem(KeyEvent.VK_F, new AbstractAction("Find") {
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread t = new Thread() {
					public void run() {
						// progressBar.setValue(percent);
						String sys = system.getText();
						String wl = getCombo(watchlists);
						symbolsList.removeActionListener(symbolsListListener);
						symbolsList.removeAllItems();
						Vector<String> res = Scanner.scan(app, sys, wl, 0, 5,
								"both", true, new Callback() {

									@Override
									public void onOk(Object data) {
										symbolsList.addItem(data);
										symbolsList.setToolTipText("Symbols: "
												+ symbolsList.getItemCount() + " ...");
									}

									@Override
									public void onEnd() {
										symbolsList
												.addActionListener(symbolsListListener);
										symbolsList.setToolTipText("Symbols: "
												+ symbolsList.getItemCount());
									}

								});

					}
				};
				t.start();
			}
		}));

		// default scan
		scanner.add(getMenuItem(KeyEvent.VK_D, new AbstractAction("Default") {
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread t = new Thread() {
					public void run() {
						// progressBar.setValue(percent);
						symbolsList.removeActionListener(symbolsListListener);
						symbolsList.removeAllItems();
						String scan = props.get("defaultScan",
								Defaults.defaultScanSystem);
						String wl = props.get("scanList", Defaults.scanList);

						app.setTitle(Defaults.appName + ": Scanning - " + scan
								+ " " + wl);

						Scanner.scan(app, scan, wl, 0, 5, "both", true,
								new Callback() {
									@Override
									public void onOk(Object data) {
										symbolsList.addItem(data);
									}

									@Override
									public void onEnd() {
										symbolsList
												.addActionListener(symbolsListListener);
									}
								});
					}
				};
				t.start();
			}

		}));
		scanner.addSeparator();
		scanner.add(getMenuItem(KeyEvent.VK_A,
				new AbstractAction("Adv. Search") {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (advSearch == null)
							advSearch = new AdvSearch(app);
						advSearch.start();
					}
				}));
		scanner.addSeparator();
		scanner.add(getMenuItem(new AbstractAction("Save") {
			@Override
			public void actionPerformed(ActionEvent e) {
				int cnt = app.symbolsList.getItemCount();
				for(int i = 0; i < cnt; i++) {
					app.setProgress((int)(i/cnt*100), i + " / " + cnt);
					Quotes.saveScan(app.system.getText(), app.symbolsList.getItemAt(i).toString().split(" ")[0]);
				}
			}
		}));

		scanner.add(getMenuItem(new AbstractAction("Scan result") {
			@Override
			public void actionPerformed(ActionEvent e) {
				app.loadSymbols("scan result", Scanner.getScanResult());
				getQuotes();
			}
		}));

		JMenu tools = new JMenu("Tools");
		menubar.add(tools);
		tools.add(getMenuItem(new AbstractAction("Perf") {
			@Override
			public void actionPerformed(ActionEvent e) {
				calcPerf();
			}
		}));
		tools.add(getMenuItem(new AbstractAction("Split") {
			@Override
			public void actionPerformed(ActionEvent e) {
				splitSymbol();
			}
		}));

		JMenu alert = new JMenu("Alert");
		menubar.add(alert);
		alert.add(getMenuItem(new AbstractAction("Add") {
			@Override
			public void actionPerformed(ActionEvent e) {
				Alerts.add(app);
			}
		}));

		alert.add(getMenuItem(new AbstractAction("Remove") {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		}));

		alert.add(getMenuItem(new AbstractAction("Run") {
			@Override
			public void actionPerformed(ActionEvent e) {
				symbolsList.removeActionListener(symbolsListListener);
				symbolsList.removeAllItems();
				symbolsList.addItem("");
				setCursor(Cursor.WAIT_CURSOR);

				try {
					Alerts.run(symbolsList);
				} catch (Exception exc) {
					msgBox(exc.getMessage());
				}
				// symbolsList.repaint();
				symbolsList.addActionListener(symbolsListListener);
				setCursor(Cursor.DEFAULT_CURSOR);
			}
		}));

		alert.add(getMenuItem(new AbstractAction("List") {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		}));

		JMenu chart = new JMenu("Chart");
		menubar.add(chart);
		chart.add(getMenuItem(KeyEvent.VK_L, new AbstractAction("Clear") {
			@Override
			public void actionPerformed(ActionEvent e) {
				showChart();
			}
		}));
		chart.add(getMenuItem(KeyEvent.VK_M, new AbstractAction("Compare") {
			@Override
			public void actionPerformed(ActionEvent e) {
				compareChart();
			}
		}));
		chart.add(getMenuItem(new AbstractAction("Remove Annotations") {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeAnnotations();
			}
		}));
		JMenu trade = new JMenu("Trade");
		menubar.add(trade);
		trade.add(getMenuItem(new AbstractAction("Add/Edit") {
			@Override
			public void actionPerformed(ActionEvent e) {
				new TradeDialog(app);
			}
		}));
		trade.add(getMenuItem(new AbstractAction("Delete") {
			@Override
			public void actionPerformed(ActionEvent e) {
				Quotes.dropTrade(symbol.getText());
			}
		}));

		JMenu help = new JMenu("Help");
		menubar.add(help);
//		help.add(getMenuItem(new AbstractAction("Help") {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				msgBox("Will be available soon.");
//			}
//		}));
		help.add(getMenuItem(new AbstractAction("Reversal") {
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = "Gap to new high and fell\n"
						+ "Tail\n"
						+ "Elephant\n"
						+ "Wave C/3/5\n"
						+ "High Volume Reversal\n"
						+ "MACD Divergence\n"
						+ "ROC divergence\n"
						+ "NRB & reversal\n"
						;
				MsgBox.showMessage("Reversals", s);
			}
		}));

		help.add(getMenuItem(new AbstractAction("Analysis") {
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = "Liquid: Yes | No\n"
						+ "Trend Up | Down | Flat \n"
						+ "Volatility Low | Medium | High\n"
						+ "Current (1) | (2) | (3) | (4)\n\n"
						+ "Action ==>\n"
						;
				MsgBox.showMessage("Market Analysis", s);
			}
		}));

		help.addSeparator();
		help.add(getMenuItem(new AbstractAction("About") {
			@Override
			public void actionPerformed(ActionEvent e) {
				int year = (new Date()).getYear();
				year += 1900;
				String s = Defaults.appName + " " + Defaults.version + "\n"
						+ "Market Analysis Tool for Traders\n\n"
						+ "(C) Copyright 2002-" + year
						+ ", Logu Venkatachalam\n";
				msgBox(s);
			}
		}));

		return menubar;
	}

	void copyList() {
		String newlist = "Copy " + getCombo(this.watchlists);
		
		newlist = getInput("Enter new watchlist", newlist);
		if(newlist == null) return;
		
		Quotes.copyList(getCombo(watchlists), newlist);
		loadLists();
		watchlists.setSelectedItem(newlist);
		if (!watchlists.getSelectedItem().equals(newlist)) {
			msgBox("List not found");
			return;
		}
		this.loadWatchList(newlist);
		getQuotes();
		
	}

	void splitSymbol() {
		String sym = this.symbol.getText();
		String dt = getInput("Date of split for " + sym + " [MM/dd/yyyy]:", "");
		if(dt == null) return;
		String factor = getInput("Split factor " + sym, "");
		if(factor == null) return;
		Quotes.split(sym, dt, factor);
	}
	JCheckBoxMenuItem viewWave1;

	static AdvSearch advSearch = null;

	protected String getCombo(JComboBox list) {
		return list.getSelectedItem().toString();
	}

	void showChart(String symbol, String st, int start, int numdays,
			String label) {

		if (chartPanel == null || symbol == null)
			return;
		setCursor(Cursor.WAIT_CURSOR);

		try {
			chartPanel.showChart(symbol, st, period.getText(), start, numdays,
					upperInd.getText(), lowerInd1.getText(), lowerInd2.getText(), label,
					barType.getText());

		} catch (Exception e) {
			e.printStackTrace();

			msgBox(e.getMessage());
		}

		setCursor(Cursor.DEFAULT_CURSOR);
	}

	static ImageIcon msgicon = new ImageIcon("icons/bulls-bears.png");

	public static void msgBox(String msg) {
		JOptionPane.showMessageDialog(null, msg,
				props.get("appName", Defaults.appName), JOptionPane.OK_OPTION,
				msgicon);
	}

	public static boolean getConfirm(String msg) {
		int ret = JOptionPane.showConfirmDialog(null, msg);
		return ret == JOptionPane.YES_OPTION;
	}

	JMenuItem getMenuItem(AbstractAction action) {
		JMenuItem menuItem = new JMenuItem("");
		menuItem.setAction(action);
		return menuItem;
	}

	JMenuItem getMenuItem(int acc, AbstractAction action) {
		JMenuItem menuItem = new JMenuItem("");
		menuItem.setAction(action);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(acc, Event.CTRL_MASK));
		return menuItem;
	}

	JToolBar getToolBar() {
		JToolBar toolbar = new JToolBar();

		toolbar.setRollover(true);

		addButtons(toolbar);

		toolbar.addSeparator();

		return toolbar;
	}

	final JTextField symbol = new JTextField(Defaults.symbol, 15);
	final JTextField symbolType = new JTextField(Defaults.symbolType, 6);
	final JTextField startday = new JTextField(Defaults.startDay + "", 8);
	final JTextField numdays = new JTextField(Defaults.numDays + "", 8);
	final JTextField period = new JTextField("d", 8);
	JButton btnchart = new JButton("Chart");
	JComboBox systems = new JComboBox();

	void addButtons(JToolBar toolbar) {

		JButton slideshow = new JButton(new ImageIcon("icons/play.png"));
		slideshow.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				SlideShow ss = new SlideShow(app);
				ss.setVisible(true);
			}

		});
		slideshow.setToolTipText("Slideshow");
		slideshow.setPreferredSize(new Dimension(30, 15));
		JButton waveCalc = new JButton(new ImageIcon("icons/wave1.png"));
		waveCalc.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
//				WaveCalc ss = new WaveCalc(app);
				CandleChart ss = new CandleChart(app);
				ss.setVisible(true);
			}

		});
		waveCalc.setPreferredSize(new Dimension(30, 15));
		waveCalc.setToolTipText("Wave Calculator");

		JButton posCalc = new JButton(new ImageIcon("icons/poscalc.png"));
		posCalc.setToolTipText("Position sizing calculator");
		posCalc.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				PosSizeCalc ss = new PosSizeCalc(app);
//				Decision ss = new Decision(app);
				ss.setVisible(true);
			}

		});
		posCalc.setPreferredSize(new Dimension(30, 15));

		ActionListener act = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					startday.setText(savedStartDay + "");
					showChart();
					Object o = e.getSource();
					if (o instanceof JTextField) {
						((JTextField) o).selectAll();
					}
				} catch (Exception exc) {
					msgBox(exc.getMessage());
				}
			}
		};

		symbol.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Object o = e.getSource();
				if (o instanceof JTextField) {
					((JTextField) o).selectAll();
				}
			}
		});

		symbol.addActionListener(act);
		symbolType.addActionListener(act);
		startday.addActionListener(act);
		numdays.addActionListener(act);
		btnchart.addActionListener(act);
		lowerInd1.addActionListener(act);
		lowerInd2.addActionListener(act);
		upperInd.addActionListener(act);

		loadWatchList(props.get("watchlist", Defaults.list));

		symbolsListListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String sym = symbolsList.getItemAt(
						symbolsList.getSelectedIndex()).toString();
				if (sym.isEmpty())
					return;
				String label = sym;
				sym = sym.split(" ")[0];
				symbol.setText(sym);
				try {
					progressBar.setMaximum(symbolsList.getItemCount() - 1);
					progressBar.setValue(symbolsList.getSelectedIndex());
					showChart(label);
				} catch (Exception e) {
					msgBox("Error:" + e.getMessage());
				}

			}

		};

		symbolsList.addActionListener(symbolsListListener);

		watchlists = new JComboBox();
		loadLists();

		watchlists.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				int idx = watchlists.getSelectedIndex();
				if (idx < 0)
					return;
				String wl = watchlists.getItemAt(watchlists.getSelectedIndex())
						.toString();
				loadWatchList(wl);
			}

		});
		systems.setSize(new Dimension(1, 20));
		systems.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				loadSystem(getCombo(systems));

				try {
					showChart();
				} catch (Exception e) {
				}
			}

		});

		Vector<String> ss = Scanner.getSystems();
		for (String s : ss) {
			systems.addItem(s);
		}
		systems.setSelectedItem(props.get("system", Defaults.system));

		watchlists.setSelectedItem(props.get("watchlist", Defaults.list));

		toolbar.add(slideshow);
		toolbar.add(waveCalc);
		toolbar.add(posCalc);
		// toolbar.add(new JLabel("System:  "));
		toolbar.add(systems);
		// toolbar.add(new JLabel("  Lists:  "));
		toolbar.add(watchlists);
		// toolbar.add(new JLabel("  Symbols:  "));
		toolbar.add(symbolsList);
		// toolbar.add(new JLabel("  Symbol:  "));
		toolbar.add(symbol);
		toolbar.add(symbolType);
		toolbar.add(new JLabel("  Start Day:  "));
		toolbar.add(startday);
		toolbar.add(new JLabel("  Num Days:  "));
		toolbar.add(numdays);
		toolbar.add(period);
		toolbar.add(new JLabel("  Upper:  "));
		toolbar.add(upperInd);
		toolbar.add(new JLabel("  Lower1:  "));
		toolbar.add(lowerInd1);
		// toolbar.add(new JLabel("  Lower2:  "));
		toolbar.add(lowerInd2);
		toolbar.add(new JLabel("  Scan:  "));
		toolbar.add(system);
		toolbar.add(barType);
		toolbar.add(btnchart);

		symbol.setToolTipText("Symbol");
		systems.setToolTipText("Setup");
		watchlists.setToolTipText("Watch lists");
		system.setToolTipText("Scan system");
		period.setToolTipText("Period");

		DocumentFilter filter = new UpperCaseFilter();

		((AbstractDocument) symbol.getDocument()).setDocumentFilter(filter);
		((AbstractDocument) symbolType.getDocument()).setDocumentFilter(filter);

		getQuotes();

	}

	private void loadLists() {
		// ActionListener [] l = watchlists.getActionListeners();
		watchlists.removeAllItems();
		Vector<String> defaultWatchList = Scanner.getLists();
		for (String l : defaultWatchList) {
			watchlists.addItem(l);
		}
	}

	JTextField system = new JTextField(Defaults.scanSystem, 20);
	JTextField barType = new JTextField("candle", 20);

	private void loadSystem(String system) {

		try {
			MyDatabase db = new MyDatabase();

			String sql = String.format("select * from setup where name = '%s'",
					system);
			MyStatement st = db.execute(sql);
			{
				// clear
				upperInd.setText("");
				lowerInd1.setText("");
				lowerInd2.setText("");
			}

			while (st.next()) {
				String p = st.getString("param");
				String v = st.getString("value");

				if (p.equals("watchlist")) {
					// watchlists.setSelectedItem(v);
				} else if (p.equals("uind")) {
					upperInd.setText(v);
				} else if (p.equals("ind1")) {
					lowerInd1.setText(v);
				} else if (p.equals("ind2")) {
					lowerInd2.setText(v);
				} else if (p.equals("period")) {
					period.setText(v);
				} else if (p.equals("scan")) {
					this.system.setText(v);
				} else if (p.equals("bartype")) {
					this.barType.setText(v);
				} else if (p.equals("numdays")) {
					this.numdays.setText(v);
				}
			}
			st.close();
		} catch (Exception e) {
			msgBox(e.getMessage());
		}

	}

	JTextField upperInd = new JTextField(Defaults.upperInd, 20),
			lowerInd1 = new JTextField(Defaults.lowerInd1, 15),
			lowerInd2 = new JTextField(Defaults.lowerInd2, 15);

	JLabel chartInfo = new JLabel("");

	JComboBox symbolsList = new JComboBox();
	ActionListener symbolsListListener;
	JComboBox watchlists;

	void loadSymbols(String what, Vector<String> symbols) {
		symbolsList.removeActionListener(symbolsListListener);
		symbolsList.removeAllItems();
		symbolsList.addItem("");
		for (String s : symbols) {
			symbolsList.addItem(s);
		}
		symbolsList.setToolTipText(what);
		symbolsList.repaint();
		symbolsList.addActionListener(symbolsListListener);

		symbolsList.setToolTipText("Symbols: " + symbolsList.getItemCount());
	}

	void loadWatchList(String wl) {
		Vector<String> res = Scanner.getSymbols(wl);
		loadSymbols(wl, res);
	}

	JLabel statusLabel = new JLabel("Status");

	JLabel[] statusLabels = new JLabel[10];

	JPanel getStatusBar() {
		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));

		statusPanel.setLayout(new GridLayout(1, 10));
		for (int i = 0; i < statusLabels.length; i++) {
			JLabel l = new JLabel();
			l.setForeground(Color.red);
			l.setHorizontalAlignment(SwingConstants.LEFT);
			statusPanel.add(l);
			statusLabels[i] = l;
		}

		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		// statusPanel.add(new JButton("Stop"));
		statusPanel.add(progressBar);

		return statusPanel;
	}

	public JProgressBar progressBar = new JProgressBar(0, 100);

	public void setProgress(int val, String ttip) {
		progressBar.setValue(val);
		progressBar.setToolTipText(ttip);
	}

	public void setProgress(int val) {
		progressBar.setValue(val);
	}

	public void setStatus(String[] sts, Color[] cols) {
		for (int i = 0; i < statusLabels.length; i++) {
			if (sts.length <= i)
				break;
			statusLabels[i].setText(sts[i]);
			if (cols.length >= i)
				statusLabels[i].setForeground(cols[i]);
		}
	}

	public void setStatus(String string) {
		setStatus(new String[] { string }, new Color[] { Color.black });
	}

	public void setStatus(int idx, String string) {
		statusLabels[idx].setText(string);
	}

	int savedStartDay = 0;

	class PerfData implements Comparable {

		String sym;
		float perf;
		public PerfData(String sym, float perf) {
			this.sym = sym;
			this.perf = perf;
		}

		@Override
		public int compareTo(Object arg0) {
			PerfData cmp = (PerfData)arg0;
			return (cmp.perf < this.perf) ? -1 : 1;
		}
		
	}

	void calcPerf() {
		final int nd = Integer.parseInt(getInput("Enter number of days", "200"));
		Thread t = new Thread() {
			public void run() {
				String per = "";
				Vector<PerfData> res = new Vector<PerfData>();
				for(int i = 0; i < symbolsList.getItemCount(); i++) {
					String sym = "";
					try {
					sym = symbolsList.getItemAt(i).toString().split(" ")[0];
					if(sym == null || sym.isEmpty()) continue;
					
						Quotes q = Quotes.getQuotes(sym, "E", 0, nd + 50, "d");
//						float perf = (q.getClose(0) - q.getClose(q.size()-1))/ q.getClose(q.size()-1)*100;
						int perf = (int)((q.getClose(0) - q.getClose(nd-1))/ q.getClose(nd-1)*100);
						res.add(new PerfData(sym, perf));
						System.err.println(i + "/" + symbolsList.getItemCount() + ":" + sym + " -- " + perf + "%");
					} catch(Exception e) {
						System.err.println(sym + " :" + e.getMessage());
					}
					
				}
//				Arrays.sort(res);
				Vector<String> res1 = new Vector<String>();
				for(PerfData p: res) {
					res1.add(p.sym + " " + p.perf + "%");
				}
				loadSymbols("Peformance", res1);
			}
		};
		t.start();
	}	

	public void keyTyped(char key) {
		String sys;
		int sd;

		switch (key) {
		case ' ':
			if (symbolsList.getSelectedIndex() >= symbolsList.getItemCount() - 1) {
				setProgress(symbolsList.getItemCount());
				msgBox("That's all folks!");
				symbolsList.setSelectedIndex(1);
				return;

			}
			symbolsList.setSelectedIndex(symbolsList.getSelectedIndex() + 1);
			progressBar.setMaximum(symbolsList.getItemCount());
			setProgress(symbolsList.getSelectedIndex() + 1);
			symbol.setText(getCombo(symbolsList).split(" ")[0]);
			startday.setText(savedStartDay + "");
//			showChart(getCombo(symbolsList));
			break;
		case '=': {
			int nd = Integer.parseInt(app.numdays.getText());
			if(nd >= 177) return;
			nd++;
			app.numdays.setText(nd+"");
			showChart();
			break;
		}
		case '-':
			int nd = Integer.parseInt(app.numdays.getText());
			nd--;
			app.numdays.setText(nd+"");
			showChart();
			break;

		case '0':
			sys = "one-stop";
			this.systems.setSelectedItem(sys);
			loadSystem(sys);
			showChart();
			break;
		case '1':
			sys = "market";
			this.systems.setSelectedItem(sys);
			loadSystem(sys);
			showChart();
			break;
		case '2':
			sys = "entry";
			this.systems.setSelectedItem(sys);
			loadSystem(sys);
			showChart();
			break;
		case '3':
			sys = "exit";
			this.systems.setSelectedItem(sys);
			loadSystem(sys);
			showChart();
			break;
		case '4':
			sys = "reversal";
			this.systems.setSelectedItem(sys);
			loadSystem(sys);
			showChart();
			break;
		case 'c':
			this.compareChart();
			break;
		case 'x':
			// launch position sizing calculator
			PosSizeCalc ss = new PosSizeCalc(app);
			ss.setVisible(true);
			break;
		case '5':
			sys = "sell-options";
			this.systems.setSelectedItem(sys);
			loadSystem(sys);
			showChart();
			break;
//		case 'r': {
//			// run slideshow for the given watchlist
//			String wl = getInput("Enter watchlist", getCombo(watchlists));
//			if (wl == null) return;
//			watchlists.setSelectedItem(wl);
//			SlideShow ss = new SlideShow(app);
//			ss.setVisible(true);
//		}
//			break;
		case 'd':
			// delete from watchlist
			String wl = getCombo(watchlists);
			String sym = this.symbol.getText();
			Quotes.dropSymbol(wl, sym);
//			msgBox(sym + " deleted from " + wl);
			nextSymbol();
			break;
		case 'W':
			period.setText("w");
			showChart();
			break;
		case 'D':
			period.setText("d");
			showChart();
			break;

		case 's':
//			ValReport report = new ValReport(this);
//			report.setVisible(true);

			sym = getInput("Enter symbol", null);
			if (sym == null)
				return;
			symbol.setText(sym);
			startday.setText(savedStartDay + "");
			showChart();
			break;
		case 'S':
			sys = getInput("Enter setup name", null);
			if (sys == null)
				return;
			this.systems.setSelectedItem(sys);
			loadSystem(sys);
			break;
		case 'w':
			wl = getInput("Enter watchlist", null);
			if (wl == null)
				return;
			watchlists.setSelectedItem(wl);
			if (!watchlists.getSelectedItem().equals(wl)) {
				msgBox("List not found");
				return;
			}
			this.loadWatchList(wl);
			getQuotes();
			break;
		case ',':
			sd = Integer.parseInt(startday.getText());
			sd++;
			startday.setText(sd + "");
			showChart();
			break;
		case '>':
			sd = Integer.parseInt(startday.getText());
			sd -= 5;
			if (sd == 0)
				return;
			startday.setText(sd + "");
			showChart();
			break;
		case '<':
			sd = Integer.parseInt(startday.getText());
			sd += 5;
			startday.setText(sd + "");
			showChart();
			break;
		case '.':
			sd = Integer.parseInt(startday.getText());
			if (sd == 0)
				return;
			sd--;
			startday.setText(sd + "");
			showChart();
			break;
		case 'm':
			String merge = this.upperInd.getText();
			if(merge.startsWith("merge")) {
				String [] xxx = merge.split(" ");
				merge = "merge " + (Integer.parseInt(xxx[1]) + 1);
			} else {
				merge = "merge 2";
			}
			this.upperInd.setText(merge);
			showChart();
			break;
		case 'M':
			merge = this.upperInd.getText();
			if(!merge.startsWith("merge")) return;
			String [] xxx = merge.split(" ");
			int mc = Integer.parseInt(xxx[1]);
			if(mc <= 1 ) return;
			merge = "merge " + (Integer.parseInt(xxx[1]) - 1);
			this.upperInd.setText(merge);
			showChart();
			break;
		case'p':
			// toggle between 55 & 177 days
			if(this.numdays.getText().equals("55")) numdays.setText("177");
			else this.numdays.setText("55");
			this.showChart();
			
			break;
		case 'q':
			// add this symbol to trenders watchlist
			// add to watch list
			Quotes.addSymbol("trenders", symbol.getText(), symbolType.getText());
			nextSymbol();
			break;
		case 'T':
			// test trade this symbol
			this.chartPanel.testTrade();
			break;
		case 't':
			this.barType.setText(this.getInput("Ener bar type", barType.getText()));
			showChart();
			break;
		case 'r':
			String bt = barType.getText();
			if(bt.equals("candle")) bt = "ha";
			else if(bt.equals("ha")) bt = "line";
			else if(bt.equals("line")) bt = "candle";
			this.barType.setText(bt);
			showChart();
			break;
		case 'a':
			// add to watch list
			String list = getInput("Enter Watchlist", null);
			if (list == null)
				return;
			Quotes.addSymbol(list, symbol.getText(), symbolType.getText());
			for(int i = 0; i < this.watchlists.getItemCount(); i++) {
				if(watchlists.getItemAt(i).equals(list)) return;
			}
			this.watchlists.addItem(list);
			break;
		case 'b':
			chartPanel.waveBack();
			break;
		case 'H':
		case 'h':
			chartPanel.drawHorizontalLine(key);
			break;
		case 'i':
			// upper indicator
			String uind = getInput("Enter upper indicator", upperInd.getText());
			if (uind == null)
				return;
			upperInd.setText(uind);
			showChart();
			break;

		case 'j':
			// Lower indicator
			String lind1 = getInput("Enter lower indicator1",
					lowerInd1.getText());
			if (lind1 == null)
				return;
			lowerInd1.setText(lind1);
			showChart();
			break;
		case 'k':
			// Lower indicator
			String lind2 = getInput("Enter lower indicator 2",
					lowerInd2.getText());
			if (lind2 == null)
				return;
			lowerInd2.setText(lind2);
			showChart();
			break;
		case 'n': // add notes
			chartPanel.addNotes();
			break;
		case 'v':
			chartPanel.drawVerticalLine();
			break;
		case 'z':
			chartPanel.saveChart(props.get("saveFolder", "c:/users/logu/trading/savedCharts/"), symbol.getText());
			break;
		}
	}

	private void nextSymbol() {
		if (symbolsList.getSelectedIndex() >= symbolsList.getItemCount() - 1) {
			setProgress(symbolsList.getItemCount());
			msgBox("That's all folks!");
			symbolsList.setSelectedIndex(1);
			return;

		}
		symbolsList.setSelectedIndex(symbolsList.getSelectedIndex() + 1);
		progressBar.setMaximum(symbolsList.getItemCount());
		setProgress(symbolsList.getSelectedIndex() + 1);
		symbol.setText(getCombo(symbolsList).split(" ")[0]);
		startday.setText(savedStartDay + "");
//		showChart(getCombo(symbolsList));
		
	}
	private void getQuotes() {
//		 if(true)return; // TODO:
		// get quotes in the background
		Thread t = new Thread() {
			public void run() {
//				for (int i = 0; i < symbolsList.getItemCount(); i++) {
//					try {
//						String sym = symbolsList.getItemAt(i).toString().split(" ")[0];
//						setStatus(0, sym);
//						Quotes.getQuotes(sym, "E", 0, 500, "d");
//					} catch (Exception e) {
//					}
//				}
				
				Quotes.loadList(watchlists.getItemAt(watchlists.getSelectedIndex()).toString());

			}
		};
		t.start();
	}

	void compareChart() {
		setCursor(Cursor.WAIT_CURSOR);
		try {
			String compSym = getInput("Enter Symbol(s) to compare", "");
			if(compSym == null) return;
			chartPanel.compare(this.symbol.getText(), compSym);
		} catch(Exception e) {
			msgBox(e.getMessage());
		}

		setCursor(Cursor.DEFAULT_CURSOR);

	}

	void showChart() {

		showChart(null);
	}

	void showChart(String label) {
		showChart(symbol.getText(), symbolType.getText(),
				Integer.parseInt(startday.getText()),
				Integer.parseInt(numdays.getText()), label);
	}

	boolean optViewText = true, optViewWave = false, optWaveB = false;

	void removeAnnotations() {
		MyDatabase db = new MyDatabase();
		String sym = symbol.getText().toUpperCase();
		db.execute("delete from notes where symbol = '" + sym + "'");
		Quotes.removeAnnotations(sym);
		showChart();
	}

}

/*
 * 
 * 1. how are my positions doing; r pos 2. how are my markets doing; r markets
 * 3. how are major indexes doing; r indexes 4. how are etf leaders doing; r
 * etf-leaders 5. scan for opportunities; scanner.default, .scan
 */

// http://finance.yahoo.com/q/op?s=AEP&date=1431648000
// earnings date

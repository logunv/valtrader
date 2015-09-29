package com.valtrader.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.valtrader.service.Scanner;
import com.valtrader.utils.Callback;

public class AdvSearch extends JDialog {

	JDialog me;
	JButton btnNew = null;
	public AdvSearch(final ValTrader app) {
		super(app);
		me = this;
		setTitle("Valtrader - Advanced Search");
		setLocationRelativeTo(app);

		int LEN = 10;

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		final JTextField scan = new JTextField(app.system.getText(), LEN), 
				start = new JTextField("0", LEN), end = new JTextField(
				"1", LEN) /*,  dir = new JTextField(
				"both", LEN)*/;
		
		final JComboBox dir = new JComboBox();
		dir.setEditable(true);
		dir.addItem("up");
		dir.addItem("down");
		dir.addItem("both");
		dir.setSelectedItem("both");

		final JComboBox watchlist = new JComboBox();
		watchlist.setEditable(true);
//		watchlist.addItem("            ");
		for(int i = 0; i < app.watchlists.getItemCount(); i++) {
			watchlist.addItem(app.watchlists.getItemAt(i));
		}
		watchlist.setSelectedItem(app.getCombo(app.watchlists));
		
		final JCheckBox stop = new JCheckBox();
		stop.setSelected(true);
//		ButtonGroup dir1 = new ButtonGroup();
//		dir1.add(new JRadioButton("Up"));
//		dir1.add(new JRadioButton("Down"));
//		dir1.add(new JRadioButton("Both"));

		btnNew = new JButton("Search");
		btnNew.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				btnNew.setText("Searching...");
				Thread t = new Thread() {
					public void run() {
						// progressBar.setValue(percent);
						app.symbolsList.removeActionListener(app.symbolsListListener);
						app.symbolsList.removeAllItems();
						Scanner.scan(app, scan.getText(), watchlist.getSelectedItem().toString(),
								Integer.parseInt(start.getText()), Integer.parseInt(end.getText()),
								app.getCombo(dir), stop.isSelected(),
								new Callback() {
									@Override
									public void onOk(Object data) {
										app.symbolsList.addItem(data);
										app.symbolsList.setToolTipText("Symbols: "
												+ app.symbolsList.getItemCount() + " ...");

									}

									@Override
									public void onEnd() {
										app.symbolsList.addActionListener(app.symbolsListListener);
										me.setVisible(false);
										app.symbolsList.setToolTipText("Symbols: "
												+ app.symbolsList.getItemCount());

									}
								});
					}
				};
				t.start();
			}

		});

		mainPanel.add(btnNew, BorderLayout.SOUTH);

		JPanel calcPanel = new JPanel();
		calcPanel.setLayout(new MigLayout());
		mainPanel.add(calcPanel, BorderLayout.NORTH);

		calcPanel.add(new JLabel("Scan Name"), "gap para");
		calcPanel.add(scan, "wrap");
		calcPanel.add(new JLabel("Watchlist"), "gap para");
		calcPanel.add(watchlist, "wrap");

		calcPanel.add(new JLabel("Start"), "gap para");
		calcPanel.add(start, "wrap");

		calcPanel.add(new JLabel("End"), "gap para");
		calcPanel.add(end, "wrap");

		calcPanel.add(new JLabel("Direction"), "gap para");
		calcPanel.add(dir, "wrap");


		calcPanel.add(new JLabel("Stop At First"), "gap para");
		calcPanel.add(stop, "wrap");

		add(mainPanel);

		setPreferredSize(new Dimension(275, 230));
		setLocation(300, 175);

		pack();
	}

	float getValue(JTextField tf) {
		if (tf.getText().isEmpty())
			return 0;
		return Float.parseFloat(tf.getText());
	}

	public void start() {
		btnNew.setText("Search");
		setVisible(true);
	}

}


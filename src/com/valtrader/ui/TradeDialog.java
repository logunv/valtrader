package com.valtrader.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.valtrader.service.Quotes;
import com.valtrader.utils.Utils;

public class TradeDialog extends JDialog {

	JDialog me;
	JButton btnNew = null;
	public TradeDialog(final ValTrader app) {
		super(app);
		me = this;
		setTitle("Valtrader - New Trade");
		setLocationRelativeTo(app);

		int LEN = 16;

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		Trade trade = Quotes.getTrade(app.symbol.getText());

		final JTextField symbol = new JTextField(app.symbol.getText(), LEN), 
				csize = new JTextField("1", LEN), size = new JTextField(
				"1", LEN),  entryDate = new JTextField(
				Utils.formatDate(new Date(), "MM/dd/yyyy"), LEN),
				entryPrice = new JTextField("", LEN),
				stopPrice = new JTextField("", LEN),
				targetPrice = new JTextField("", LEN);
		
		if(trade != null) {
			symbol.setText(trade.symbol);
			csize.setText(trade.csize + "");
			size.setText(trade.size + "");
			entryDate.setText(trade.entryDate.toString());
			entryPrice.setText(trade.entry + "");
			stopPrice.setText(trade.stop + "");
			targetPrice.setText(trade.target + "");
		}
		
		btnNew = new JButton("Add");
		btnNew.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Quotes.addTrade(symbol.getText(), Integer.parseInt(csize.getText()), 
						Integer.parseInt(size.getText()), entryDate.getText(), 
						Float.parseFloat(entryPrice.getText()),
						Float.parseFloat(stopPrice.getText()),
						Float.parseFloat(targetPrice.getText())
						);
				ValTrader.msgBox("Added to trades.");
			}

		});

		mainPanel.add(btnNew, BorderLayout.SOUTH);

		JPanel calcPanel = new JPanel();
		calcPanel.setLayout(new MigLayout());
		mainPanel.add(calcPanel, BorderLayout.NORTH);

		calcPanel.add(new JLabel("Symbol"), "gap para");
		calcPanel.add(symbol, "wrap");
		calcPanel.add(new JLabel("C Size"), "gap para");
		calcPanel.add(csize, "wrap");

		calcPanel.add(new JLabel("Size"), "gap para");
		calcPanel.add(size, "wrap");

		calcPanel.add(new JLabel("Entry Date"), "gap para");
		calcPanel.add(entryDate, "wrap");

		calcPanel.add(new JLabel("Entry Price"), "gap para");
		calcPanel.add(entryPrice, "wrap");

		calcPanel.add(new JLabel("Stop Price"), "gap para");
		calcPanel.add(stopPrice, "wrap");

		calcPanel.add(new JLabel("Target Price"), "gap para");
		calcPanel.add(targetPrice, "wrap");

		add(mainPanel);

		setPreferredSize(new Dimension(325, 275));

		setLocation(300, 175);

		pack();
		setVisible(true);
	}

	float getValue(JTextField tf) {
		if (tf.getText().isEmpty())
			return 0;
		return Float.parseFloat(tf.getText());
	}


}


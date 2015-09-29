package com.valtrader.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

public class Decision extends JDialog {

	public Decision(final ValTrader app) {
		super(app);
		setTitle("Valtrader - Market Analysis");
		setLocationRelativeTo(app);

		int LEN = 5;

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		final JTextField liquid = new JTextField("y", LEN), 
				trend = new JTextField("u", LEN), 
				volatility = new JTextField("l", LEN), 
				current = new JTextField("l", LEN), 
				decision = new JTextField("", 20); 

		JButton btnNew = new JButton("Go");
		btnNew.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				decision.setText(
						check(
								liquid.getText().charAt(0),
								trend.getText().charAt(0),
								volatility.getText().charAt(0),
								current.getText().charAt(0)
						));
			}

		});

		FocusListener action = new FocusListener() {

			@Override
			public void focusGained(FocusEvent arg0) {
				JTextField tf = (JTextField)arg0.getSource();
				tf.selectAll();

			}

			@Override
			public void focusLost(FocusEvent arg0) {
//				riskD.setText(getValue(capital) * getValue(riskP) / 100 + "");

			}

		};

		mainPanel.add(btnNew, BorderLayout.SOUTH);

		JPanel calcPanel = new JPanel();
		calcPanel.setLayout(new MigLayout());
		mainPanel.add(calcPanel, BorderLayout.NORTH);

		calcPanel.add(new JLabel("Liquid [y/n]"), "gap para");
		calcPanel.add(liquid, "wrap");
		liquid.addFocusListener(action);
		calcPanel.add(new JLabel("Trend [u/d/f]"), "gap para");
		calcPanel.add(trend, "wrap");
		trend.addFocusListener(action);

		calcPanel.add(new JLabel("Volatility [l/m/h]"), "gap para");
		calcPanel.add(volatility, "wrap");
		current.addFocusListener(action);
		calcPanel.add(new JLabel("Current [l/m/h]"), "gap para"); // l - near lower band; h - near upper band; m - in the middle
		calcPanel.add(current, "wrap");
		volatility.addFocusListener(action);

		calcPanel.add(new JLabel("Action"), "gap para");
		calcPanel.add(decision, "wrap");
		decision.setEditable(false);

		add(mainPanel);

		setPreferredSize(new Dimension(275, 220));
		setLocation(300, 175);

		pack();
	}

	float getValue(JTextField tf) {
		if (tf.getText().isEmpty())
			return 0;
		return Float.parseFloat(tf.getText());
	}
	
	String check(char liq, char trend, char vol, char current)	{
	  if(liq == 'n') return "pass";
	  
	  switch(trend) {
	    case 'u':
	      switch(vol) {
	      case 'l':
	        switch(current) {
	        case 'l':
	          return "Buy long";
	        case 'm':
	          return "Hold long";
	        case 'h':
	          return "Hold long; Sell long";
	        }
	      case 'm':
	        switch(current) {
	        case 'l':
	          return "Buy call; Buylong";
	        case 'm':
	          return "Hold call; Hold long";
	        case 'h':
	          return "Sell call; Sell long";
	        }
	      case 'h':
	        switch(current) {
	        case 'l': return "Sell puts";
	        case 'm': return "Hold short puts";
	        case 'h': return "Cover puts";
	        }
	      }
	      break;
	    case 'd':
	      switch(vol) {
	      case 'l':
	        switch(current) {
	        case 'l':
	          return "Cover short; sell long puts";
	        case 'm':
	          return "Hold short/Put";
	        case 'h':
	          return "Short; Buy puts";
	        }
	        break;
	      case 'm':
	        switch(current) {
	        case 'l':
	          return "Cover Short; Close long puts";
	        case 'm':
	          return "hold";
	        case 'h':
	          return "Short; Buy Puts";
	        }
	        break;
	      case 'h':
	        switch(current) {
	        case 'l': return "Cover short call;";
	        case 'm': return "Hold short call";
	        case 'h': return "Short call";
	        }
	        break;
	      }
	      break;
	    case 'f':
	      switch(vol) {
	      case 'l': return "pass";
	      case 'm':
	      case 'h':
	        switch(current) {
	        case 'l':
	          return "Buy";
	        case 'm':
	          return "hold";
	        case 'h':
	          return "sell";
	        }
	        break;
	      }
	      break;
	    }
	    
	    return "Invalid data";

	}

}

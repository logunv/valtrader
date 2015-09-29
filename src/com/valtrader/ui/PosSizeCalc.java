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

import com.valtrader.service.Quotes;
import com.valtrader.utils.Utils;

public class PosSizeCalc extends JDialog {

	public PosSizeCalc(final ValTrader app) {
		super(app);
		setTitle("Valtrader - Position Sizing Calculator");
		setLocationRelativeTo(app);

		int LEN = 15;

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		final JTextField capital = new JTextField(app.props.get("capital", "10000"), LEN), 
				riskP = new JTextField(app.props.get("risk%", "2"), LEN), 
				riskD = new JTextField("200", LEN), 
				curRiskD = new JTextField("0", LEN), 
				csize = new JTextField("1", LEN), 
				entry = new JTextField("", LEN), 
				stop = new JTextField("", LEN), 
				possize = new JTextField("0", LEN), 
				posrisk = new JTextField("0", LEN),
				invest = new JTextField("", LEN);

		try {
		Quotes quotes = Quotes.getQuotes(app.symbol.getText(), app.symbolType.getText(), 0, 50, "d");
		entry.setText(quotes.getClose(0) + "");
		} catch(Exception e) {
			
		}


		JButton btnNew = new JButton("Calculate");
		btnNew.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
//				riskD.setText(getValue(capital) * getValue(riskP) / 100 + "");
				riskD.setText(getValue(capital) * getValue(riskP) / 100 - getValue(curRiskD) + "");
				float riskdollar = getValue(riskD);
				float riskpoints = getValue(csize)
						* (getValue(entry) - getValue(stop));
//				riskdollar -= Float.parseFloat(arg0)
				int pos = (int)(riskdollar / riskpoints);
				possize.setText(pos + "");
				String posR = Math.abs(getValue(entry) - getValue(stop))
						+ " , "
						+ Math.abs(getValue(csize)
								* (getValue(entry) - getValue(stop)));
				posrisk.setText(posR);
			}

		});

		FocusListener action = new FocusListener() {

			@Override
			public void focusGained(FocusEvent arg0) {
				JTextField tf = (JTextField)arg0.getSource();
				tf.selectAll();
//				riskD.setText(getValue(capital) * getValue(riskP) / 100 + "");
//				float riskdollar = getValue(riskD);
//				float riskpoints = getValue(csize)
//						* (getValue(entry) - getValue(stop));
//				float pos = riskdollar / riskpoints;
//				possize.setText(pos + "");
//				String posR = Math.abs(getValue(entry) - getValue(stop))
//						+ " , "
//						+ Math.abs(getValue(csize)
//								* (getValue(entry) - getValue(stop)));
//				posrisk.setText(posR);

			}

			@Override
			public void focusLost(FocusEvent arg0) {
//				riskD.setText(getValue(capital) * getValue(riskP) / 100 + "");
				double availRisk = getValue(capital) * 0.1; // available risk = 10 % of capital
				// wrong
				availRisk -= getValue(curRiskD);
				System.err.println(getValue(curRiskD));
				double thisRisk = getValue(capital) * getValue(riskP) / 100;
				if(thisRisk > availRisk) thisRisk = availRisk;
				riskD.setText(thisRisk + "");
				float riskdollar = getValue(riskD);
				float riskpoints = getValue(csize)
						* (getValue(entry) - getValue(stop));
				int pos = (int)(riskdollar / riskpoints);
				possize.setText(pos + "");
				String posR = "";
				if(Float.parseFloat(csize.getText()) > 1) {
					posR = Utils.myFormat(Math.abs(getValue(entry) - getValue(stop))) + ";";
				}
				posR += " $" + Utils.myFormat(Math.abs(getValue(csize)
								* (getValue(entry) - getValue(stop))));
				posrisk.setText(posR);
				float invDollars = Float.parseFloat(possize.getText()) * Float.parseFloat(entry.getText());
				invest.setText(invDollars + "");

			}

		};

		mainPanel.add(btnNew, BorderLayout.SOUTH);

		JPanel calcPanel = new JPanel();
		calcPanel.setLayout(new MigLayout());
		mainPanel.add(calcPanel, BorderLayout.NORTH);

		calcPanel.add(new JLabel("Capital"), "gap para");
		calcPanel.add(capital, "wrap");
		capital.addFocusListener(action);
		calcPanel.add(new JLabel("Risk%"), "gap para");
		calcPanel.add(riskP, "wrap");
		riskP.addFocusListener(action);

		calcPanel.add(new JLabel("Current Risk$"), "gap para");
		calcPanel.add(curRiskD, "wrap");
		curRiskD.addFocusListener(action);
		calcPanel.add(new JLabel("Risk$"), "gap para");
		calcPanel.add(riskD, "wrap");
		riskD.setEditable(false);
		riskD.addFocusListener(action);

		calcPanel.add(new JLabel("Market"), "gap para");
		final JTextField market = new JTextField(app.symbol.getText(), LEN);
		String m = market.getText();
		if(!m.isEmpty()) {
			csize.setText(Quotes.getCSize(m) + "");
		}
		{
			float capV = Float.parseFloat(capital.getText());
			float riskV = Float.parseFloat(riskP.getText());
			riskD.setText((capV * riskV / 100) + "");
		}
		market.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent arg0) {				
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				String m = market.getText();
				if(m.isEmpty()) return;
				csize.setText(Quotes.getCSize(m) + "");
			}
			
		});

		calcPanel.add(market, "wrap");

		calcPanel.add(new JLabel("CSize"), "gap para");
		calcPanel.add(csize, "wrap");
		csize.addFocusListener(action);

		calcPanel.add(new JLabel("Entry"), "gap para");
		calcPanel.add(entry, "wrap");
		entry.addFocusListener(action);

		calcPanel.add(new JLabel("Stop"), "gap para");
		calcPanel.add(stop, "wrap");
		stop.addFocusListener(action);

		calcPanel.add(new JLabel("Pos Risk"), "gap para");
		calcPanel.add(posrisk, "wrap");
		posrisk.setEditable(false);

		calcPanel.add(new JLabel("Position Size"), "gap para");
		calcPanel.add(possize, "wrap");
		possize.setEditable(false);
		calcPanel.add(new JLabel("Investment$"), "gap para");
		calcPanel.add(invest, "wrap");
		invest.setEditable(false);
		
		

		add(mainPanel);

		setPreferredSize(new Dimension(275, 350));
		setLocation(300, 175);

		pack();
		stop.requestFocus();
	}

	float getValue(JTextField tf) {
		if (tf.getText().isEmpty())
			return 0;
		return Float.parseFloat(tf.getText());
	}

}

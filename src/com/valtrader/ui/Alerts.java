package com.valtrader.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;

import net.miginfocom.swing.MigLayout;

import com.valtrader.data.MyDatabase;
import com.valtrader.data.MyStatement;
import com.valtrader.service.Quotes;

public class Alerts {
	static MyDatabase db = new MyDatabase();

	static void add(ValTrader app) {
		(new AlertDialog(app, db)).setVisible(true);
	}

	static void remove() {

	}

	static void show() {

	}

	static void run(JComboBox result) throws Exception {
		// for all alerts
		// check alert
		// if true add to list
		
		String sql = "select * from alerts";
		
		MyStatement st = db.execute(sql);
		while(st.next()) {
			String symbol = st.getString("symbol");
			String type = st.getString("type");
			String what = st.getString("what");
			String when = st.getString("when1");
			float where = st.getFloat("where1");
			String notes = st.getString("note");
			
			Quotes q = Quotes.getQuotes(symbol, type, 0, 50, "d");
			boolean res = false;
			if(what.equalsIgnoreCase("price")) {
				if(when.equalsIgnoreCase("below")) {
					res = q.getClose(0) < where;
				}
				if(when.equalsIgnoreCase("above")) {
					res = q.getClose(0) > where;
				}
			}

			if(res) {
				String txt = symbol + " " + notes;
				result.addItem(txt);
			}
		}

		st.close();
	}
}

class AlertDialog extends JDialog {

	public AlertDialog(final ValTrader app, final MyDatabase db) {
		super(app);
		setTitle("Valtrader - Alert");
		setLocationRelativeTo(app);

		int LEN = 10;

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		final JTextField symbol = new JTextField(app.symbol.getText(), LEN), 
				symbolType = new JTextField(
				app.symbolType.getText(), LEN), what = new JTextField("price", LEN), when = new JTextField(
				"below", LEN), where = new JTextField("", LEN), notes = new JTextField(
				"", LEN);

	       DocumentFilter filter = new UpperCaseFilter();

	        ((AbstractDocument) symbol.getDocument()).setDocumentFilter(filter);
	        ((AbstractDocument) symbolType.getDocument()).setDocumentFilter(filter);

		
		JButton btnGo = new JButton("Go");
		btnGo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String sql = "insert into alerts(symbol, type, what, when1, where1, note) " +
						"values('%s', '%s', '%s', '%s', %s, '%s')";

				sql = String.format(sql, symbol.getText(),
						symbolType.getText(), what.getText(), when.getText(),
						where.getText(), notes.getText());
				db.execute(sql);
			}

		});

		mainPanel.add(btnGo, BorderLayout.SOUTH);

		JPanel calcPanel = new JPanel();
		calcPanel.setLayout(new MigLayout());
		mainPanel.add(calcPanel, BorderLayout.NORTH);

		calcPanel.add(new JLabel("Symbol"), "gap para");
		calcPanel.add(symbol, "wrap");
		calcPanel.add(new JLabel("Symbl Type"), "gap para");
		calcPanel.add(symbolType, "wrap");

		calcPanel.add(new JLabel("What"), "gap para");
		calcPanel.add(what, "wrap");

		calcPanel.add(new JLabel("When"), "gap para");
		calcPanel.add(when, "wrap");

		calcPanel.add(new JLabel("Where"), "gap para");
		calcPanel.add(where, "wrap");

		calcPanel.add(new JLabel("Notes"), "gap para");
		calcPanel.add(notes, "wrap");

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

}

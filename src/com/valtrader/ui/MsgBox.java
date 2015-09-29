package com.valtrader.ui;

import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class MsgBox {

	public static void showMessage(String title, String msg1) {
	
		JDialog dlg = new JDialog(ValTrader.app);
		dlg.setLocationRelativeTo(ValTrader.app);

		JTextArea msg = new JTextArea();
		int wid = 300;
		int ht = 300;
		msg.setPreferredSize(new Dimension(wid-25, ht-25));
		dlg.setTitle(title);

		msg.setText(msg1);
		JPanel mainPanel = new JPanel();
		mainPanel.add(msg);

		dlg.add(mainPanel);
		dlg.setPreferredSize(new Dimension(wid, ht));

		dlg.setLocation(15, 85);

		dlg.pack();
		dlg.setVisible(true);

	}

}


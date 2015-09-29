package com.valtrader.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;


public class ValReport extends JDialog {

	JTable table;
	public ValReport(final ValTrader app) {
		super(app);
		setTitle("ValReport");
		setLocationRelativeTo(app);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(table = getTable(), BorderLayout.CENTER);

		addRow(new String[] {"Logu", "Vivek"});
		add(mainPanel);

		setPreferredSize(new Dimension(600, 500));
//		RefineryUtilities.centerFrameOnScreen(this);
		setLocation(300, 200);

		pack();

	}
	
	void addRow(Object[] o) {
		model.addRow(o);
		
	}
	DefaultTableModel model;
	JTable getTable() {
		model = new DefaultTableModel();
		
		JTable table = new JTable(model);
		model.addColumn("Co11");
		model.addColumn("Co12");
		model.addColumn("Co13");
		
		
		return table;
	}
}

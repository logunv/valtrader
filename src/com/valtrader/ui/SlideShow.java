package com.valtrader.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SlideShow extends JDialog {
	public SlideShow(final ValTrader app) {
		super(app);
		setLocationRelativeTo(app);

		JPanel panel = new JPanel();
		setUndecorated(true);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		add(panel);

		String[] all = { "rewind", "pause", "stop", "forward" };

		final SpinnerModel spinModel = new SpinnerNumberModel(5000, 1000, 10000, 500); // default, min, max, step
		JSpinner spinner = new JSpinner(spinModel);
		spinner.setSize(55, 16);

		final MyThread thread = new MyThread(app, spinner);

		for (final String s : all) {
			JButton b = new JButton(new ImageIcon("icons/" + s + ".png"));
			b.putClientProperty("key", s);
			b.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {

					JButton b1 = (JButton) arg0.getSource();
					String val = (String) b1.getClientProperty("key");
					if (val.equals("pause")) {
						b1.setIcon(new ImageIcon("icons/play.png"));
						b1.putClientProperty("key", "play");
						thread.suspend();
					}

					if (val.equals("play")) {
						b1.setIcon(new ImageIcon("icons/pause.png"));
						b1.putClientProperty("key", "pause");
						thread.resume();
					}
					if (val.equals("stop")) {
						thread.cmd = MyThread.stop;
						dispose();
					}
					if (val.equals("rewind")) {
						thread.goPrev();
					}
					if (val.equals("forward")) {
						thread.goNext();
					}
				}

			});
			b.setPreferredSize(new Dimension(30, 30));
			panel.add(b);
		}
		panel.add(spinner);
		setLocation(5, 75);
		pack();

		thread.start();
	}
}

class MyThread extends Thread {
	ValTrader app;
	public static final int cont = 0;
	public static final int stop = 1;

	volatile int cmd = cont;
	JSpinner sleep;

	MyThread(ValTrader app, JSpinner spinner3) {
		this.app = app;
		this.sleep = spinner3;
	}

	int curidx = 1;

	void goPrev() {
		if (curidx <= 1)
			return;
		curidx--;
		showChart();
	}

	void goNext() {
		if (curidx >= app.symbolsList.getItemCount())
			return;
		curidx++;
		showChart();
	}

	void showChart() {
		String sym = app.symbolsList.getItemAt(curidx).toString().split(" ")[0];
		app.symbol.setText(sym);
		app.showChart(app.symbolsList.getItemAt(curidx).toString());
	}

	public void run() {
		while(true) {
			if (cmd == stop) {
				return;
			}
			int cnt = app.symbolsList.getItemCount();
			if(curidx < cnt) {
				app.setProgress((int)((float)curidx/cnt * 100));
				showChart();
			}
			xsleep();
			
			if(++curidx >= cnt) curidx = 1;
		}
	}

	void xsleep() {
		int milli = Integer.parseInt(sleep.getValue().toString());
		if (milli < 100)
			milli = 100;
		try {
			Thread.sleep(milli);
		} catch (Exception e) {

		}
	}
}

package net.sf.jclec.sbse.discovery.imo.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * A graphic panel to show a count down during evaluation
 * @author Aurora Ramirez 
 * */
public class CountDown {

	JFrame frame;
	TimeCounterPanel panel;
	long duration;

	public static void main(String[] args) {
		CountDown cd = new CountDown(10000);
		cd.start();
	}

	public CountDown(long duration) {
		this.duration = duration;
	}

	public void start(){
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame = new JFrame("Remaining time...");
				panel = new TimeCounterPanel(duration);
				frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				frame.add(panel);
				frame.pack();
				frame.setVisible(true);
			}
		});
	}

	public void stopAndClose(){
		panel.stopCounter();
		frame.setVisible(false);
	}

	public class TimeCounterPanel extends JPanel {

		private static final long serialVersionUID = -6688527014821747348L;
		private Timer timer;
		private long startTime = -1;
		private long duration;

		private JLabel label;

		public TimeCounterPanel(long d) {
			this.duration = d;
			setLayout(new GridBagLayout());
			timer = new Timer(0, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (startTime < 0) {
						startTime = System.currentTimeMillis();
					}
					long now = System.currentTimeMillis();
					long clockTime = now - startTime;
					if (clockTime >= duration) {
						clockTime = duration;
						timer.stop();
					}
					SimpleDateFormat df = new SimpleDateFormat("mm:ss:SSS");
					label.setText(df.format(duration - clockTime));
				}
			});
			
			label = new JLabel("Starting counter...");
			label.setFont(new Font("Sans Serif",Font.BOLD,32));
			label.setForeground(Color.RED);
			add(label);
			timer.start();
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(300, 50);
		}

		public void stopCounter(){
			timer.stop();
		}

	}
}
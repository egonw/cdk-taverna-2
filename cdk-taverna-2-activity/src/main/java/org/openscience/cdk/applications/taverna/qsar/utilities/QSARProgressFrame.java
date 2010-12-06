package org.openscience.cdk.applications.taverna.qsar.utilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;

public class QSARProgressFrame extends JFrame {

	private static final long serialVersionUID = -8762430249244207244L;
	private JLabel[] stateLabels = null;
	private JProgressBar progressBar = null;
	private JButton closeButton = null;
	private JLabel progressLabel = null;
	private ActionListener closeListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			QSARProgressFrame.this.setVisible(false);
		}
	};

	public QSARProgressFrame(int numberOfThreads) {
		this.setResizable(false);
		this.setLayout(new GridLayout(numberOfThreads + 2, 1));
		this.stateLabels = new JLabel[numberOfThreads];
		for (int i = 0; i < numberOfThreads; i++) {
			this.stateLabels[i] = new JLabel();
			this.stateLabels[i].setOpaque(true);
			this.stateLabels[i].setBackground(Color.RED);
			this.stateLabels[i].setText("Worker " + (i + 1) + ": Initializing...");
			this.stateLabels[i].setPreferredSize(new Dimension(300, 0));
			this.stateLabels[i].setBorder(new LineBorder(Color.BLACK));
			this.add(this.stateLabels[i]);
		}
		JPanel progressPanel = new JPanel();
		progressPanel.setLayout(new GridLayout());
		progressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		this.progressBar = new JProgressBar();
		this.progressBar.setStringPainted(true);
		progressPanel.add(this.progressBar);
		this.add(progressPanel);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		this.progressLabel = new JLabel();
		this.progressLabel.setText("");
		buttonPanel.add(this.progressLabel, BorderLayout.WEST);
		this.closeButton = new JButton("Close");
		this.closeButton.addActionListener(this.closeListener);
		buttonPanel.add(this.closeButton, BorderLayout.EAST);
		this.setTitle("Progress...");
		this.add(buttonPanel);
		this.pack();
	}

	public JLabel getProgressLabel() {
		return progressLabel;
	}

	public JLabel[] getStateLabels() {
		return stateLabels;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}
}

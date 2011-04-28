package org.openscience.cdk.applications.taverna.ui.weka;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EtchedBorder;

public class WekaLearningConfigurationPanelView extends JPanel {

	private static final long serialVersionUID = 6788886457794959717L;
	private JComboBox learnerComboBox;
	private JTextField threadsTextField;

	/**
	 * Create the frame.
	 */
	public WekaLearningConfigurationPanelView() {
		setLayout(new BorderLayout(0, 0));
		// setBounds(100, 100, 450, 300);

		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.setPreferredSize(new Dimension(10, 35));
		add(panel, BorderLayout.NORTH);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);

		learnerComboBox = new JComboBox();
		sl_panel.putConstraint(SpringLayout.SOUTH, learnerComboBox, -7, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.EAST, learnerComboBox, -10, SpringLayout.EAST, panel);
		panel.add(learnerComboBox);

		JLabel lblNewLabel = new JLabel("Select Algorithm:");
		sl_panel.putConstraint(SpringLayout.WEST, learnerComboBox, 6, SpringLayout.EAST, lblNewLabel);
		sl_panel.putConstraint(SpringLayout.SOUTH, lblNewLabel, -10, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.WEST, lblNewLabel, 10, SpringLayout.WEST, panel);
		panel.add(lblNewLabel);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		add(panel_1, BorderLayout.SOUTH);

		JLabel lblNewLabel_1 = new JLabel("Number of Threads:");
		panel_1.add(lblNewLabel_1);

		threadsTextField = new JTextField();
		threadsTextField.setSize(new Dimension(40, 0));
		threadsTextField.setPreferredSize(new Dimension(40, 20));
		panel_1.add(threadsTextField);
		threadsTextField.setColumns(10);
	}

	public JComboBox getLearnerComboBox() {
		return learnerComboBox;
	}

	public JTextField getThreadsTextField() {
		return threadsTextField;
	}
}

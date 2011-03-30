package org.openscience.cdk.applications.taverna.ui.weka;

import javax.swing.JPanel;
import java.awt.Dimension;
import javax.swing.SpringLayout;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.JSeparator;

public class AttributeEvaluationConfigurationPanelView extends JPanel {
	private JLabel lblSelectAlgorithm;
	private JComboBox algorithmComboBox;
	private JButton btnConfigure;
	private JLabel lblNumberOfThreads;
	private JTextField threadsTextField;
	public AttributeEvaluationConfigurationPanelView() {
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setPreferredSize(new Dimension(445, 85));
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		lblSelectAlgorithm = new JLabel("Select algorithm:");
		springLayout.putConstraint(SpringLayout.NORTH, lblSelectAlgorithm, 13, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, lblSelectAlgorithm, 10, SpringLayout.WEST, this);
		add(lblSelectAlgorithm);
		
		algorithmComboBox = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, algorithmComboBox, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, algorithmComboBox, 6, SpringLayout.EAST, lblSelectAlgorithm);
		springLayout.putConstraint(SpringLayout.EAST, algorithmComboBox, -95, SpringLayout.EAST, this);
		add(algorithmComboBox);
		
		btnConfigure = new JButton("Configure");
		springLayout.putConstraint(SpringLayout.NORTH, btnConfigure, -4, SpringLayout.NORTH, lblSelectAlgorithm);
		springLayout.putConstraint(SpringLayout.WEST, btnConfigure, 6, SpringLayout.EAST, algorithmComboBox);
		add(btnConfigure);
		
		lblNumberOfThreads = new JLabel("Number of threads:");
		springLayout.putConstraint(SpringLayout.WEST, lblNumberOfThreads, 0, SpringLayout.WEST, lblSelectAlgorithm);
		springLayout.putConstraint(SpringLayout.SOUTH, lblNumberOfThreads, -11, SpringLayout.SOUTH, this);
		add(lblNumberOfThreads);
		
		threadsTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, threadsTextField, -3, SpringLayout.NORTH, lblNumberOfThreads);
		springLayout.putConstraint(SpringLayout.WEST, threadsTextField, 6, SpringLayout.EAST, lblNumberOfThreads);
		springLayout.putConstraint(SpringLayout.EAST, threadsTextField, -281, SpringLayout.EAST, this);
		add(threadsTextField);
		threadsTextField.setColumns(10);
	}
	
	public JComboBox getAlgorithmComboBox() {
		return algorithmComboBox;
	}
	
	public JButton getBtnConfigure() {
		return btnConfigure;
	}

	public JTextField getThreadsTextField() {
		return threadsTextField;
	}
}
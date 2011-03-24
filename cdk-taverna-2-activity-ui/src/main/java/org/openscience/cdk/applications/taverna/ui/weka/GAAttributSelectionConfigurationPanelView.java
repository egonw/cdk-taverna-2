package org.openscience.cdk.applications.taverna.ui.weka;

import javax.swing.JPanel;
import java.awt.Dimension;
import javax.swing.SpringLayout;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JButton;

public class GAAttributSelectionConfigurationPanelView extends JPanel {
	private JLabel lblSelectAlgorithm;
	private JComboBox algorithmComboBox;
	private JButton btnConfigure;
	public GAAttributSelectionConfigurationPanelView() {
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
	}
	public JComboBox getAlgorithmComboBox() {
		return algorithmComboBox;
	}
	public JButton getBtnConfigure() {
		return btnConfigure;
	}
}

package org.openscience.cdk.applications.taverna.ui.weka.panels.regression;

import java.awt.Dimension;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;

import org.openscience.cdk.applications.taverna.ui.UITools;
import org.openscience.cdk.applications.taverna.ui.weka.panels.AbstractRegressionConfigurationFrame;

import weka.classifiers.functions.LinearRegression;

public class MultipleLinearRegressionFrame extends AbstractRegressionConfigurationFrame {

	private static final long serialVersionUID = 2805928191315603424L;
	private final static String[] SELECTION_METHODS = new String[] { "M5", "None", "Greedy" };
	private JTextField ridgeParameterTextField;
	private JComboBox comboBox;
	private JCheckBox eliminateCheckBox;

	/**
	 * Create the panel.
	 */
	public MultipleLinearRegressionFrame() {
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setPreferredSize(new Dimension(410, 275));
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);

		JLabel lblAttributeSelectionMethod = new JLabel("Attribute selection method:");
		springLayout.putConstraint(SpringLayout.NORTH, lblAttributeSelectionMethod, 13, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, lblAttributeSelectionMethod, 10, SpringLayout.WEST, this);
		add(lblAttributeSelectionMethod);

		DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel(SELECTION_METHODS);
		comboBox = new JComboBox(comboBoxModel);
		springLayout.putConstraint(SpringLayout.NORTH, comboBox, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, comboBox, 15, SpringLayout.EAST, lblAttributeSelectionMethod);
		springLayout.putConstraint(SpringLayout.EAST, comboBox, -12, SpringLayout.EAST, this);
		add(comboBox);

		eliminateCheckBox = new JCheckBox("Eliminate colinear attributes");
		springLayout.putConstraint(SpringLayout.NORTH, eliminateCheckBox, 18, SpringLayout.SOUTH,
				lblAttributeSelectionMethod);
		springLayout.putConstraint(SpringLayout.WEST, eliminateCheckBox, 10, SpringLayout.WEST, this);
		add(eliminateCheckBox);

		JLabel lblNewLabel = new JLabel("Ridge parameter:");
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel, 20, SpringLayout.SOUTH, eliminateCheckBox);
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel, 0, SpringLayout.WEST, lblAttributeSelectionMethod);
		add(lblNewLabel);

		ridgeParameterTextField = new JTextField();
		ridgeParameterTextField.setText("0.00000008");
		springLayout.putConstraint(SpringLayout.NORTH, ridgeParameterTextField, -3, SpringLayout.NORTH, lblNewLabel);
		springLayout.putConstraint(SpringLayout.WEST, ridgeParameterTextField, 6, SpringLayout.EAST, lblNewLabel);
		add(ridgeParameterTextField);
		ridgeParameterTextField.setColumns(10);

	}

	@Override
	public Class<?> getConfiguredClass() {
		return LinearRegression.class;
	}

	@Override
	public String getName() {
		return "Multiple Linear Regression";
	}

	@Override
	public boolean checkValues() {
		return (UITools.checkTextFieldValueDouble(this, "Ridge parameter", this.ridgeParameterTextField, 0,
				Double.MAX_VALUE) && Double.parseDouble(this.ridgeParameterTextField.getText()) != 0);
	}

	@Override
	public String[] getOptions() {
		String option = "-S " + this.comboBox.getSelectedIndex() + " ";
		option += "-R " + ridgeParameterTextField.getText() + " ";
		if (!eliminateCheckBox.isSelected()) {
			option += "-C ";
		}
		return new String[] { option };
	}

	@Override
	public void setOptions(String[] options) {
		String[] optMin = options[0].split(" ");
		this.comboBox.setSelectedIndex(Integer.parseInt(optMin[1]));
		this.ridgeParameterTextField.setText(optMin[3]);
		if (optMin.length > 4) {
			this.eliminateCheckBox.setSelected(false);
		} else {
			this.eliminateCheckBox.setSelected(true);
		}
	}

	@Override
	public void makeSingleOption() {
		// Nothing
	}

	@Override
	public boolean useThreading() {
		return true;
	}
}

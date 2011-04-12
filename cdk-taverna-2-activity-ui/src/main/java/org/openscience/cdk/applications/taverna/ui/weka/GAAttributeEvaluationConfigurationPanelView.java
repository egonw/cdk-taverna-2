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
import javax.swing.JCheckBox;

public class GAAttributeEvaluationConfigurationPanelView extends JPanel {
	private JLabel lblSelectAlgorithm;
	private JComboBox algorithmComboBox;
	private JButton btnConfigure;
	private JLabel lblNewLabel;
	private JTextField attrRateTextField;
	private JLabel lblCrossoverRate;
	private JLabel lblNumberOfIndividuals;
	private JLabel lblOfIterations;
	private JTextField iterationsTextField;
	private JTextField numIndTextField;
	private JTextField coRateTextField;
	private JLabel lblMinOf;
	private JTextField minAttrTextField;
	private JLabel lblMaxOf;
	private JTextField maxAttrTextField;
	private JLabel lblStepSize;
	private JTextField stepSizeTextField;
	private JLabel lblNumberOfThreads;
	private JTextField threadsTextField;
	private JCheckBox cvCheckBox;
	private JTextField cvTextField;
	private JLabel lblForFree;
	public GAAttributeEvaluationConfigurationPanelView() {
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setPreferredSize(new Dimension(445, 195));
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
		
		lblNewLabel = new JLabel("Attribute mutation rate:");
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel, 9, SpringLayout.SOUTH, algorithmComboBox);
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel, 0, SpringLayout.WEST, lblSelectAlgorithm);
		add(lblNewLabel);
		
		attrRateTextField = new JTextField();
		attrRateTextField.setText("0.05");
		springLayout.putConstraint(SpringLayout.NORTH, attrRateTextField, 6, SpringLayout.SOUTH, algorithmComboBox);
		springLayout.putConstraint(SpringLayout.WEST, attrRateTextField, 6, SpringLayout.EAST, lblNewLabel);
		springLayout.putConstraint(SpringLayout.EAST, attrRateTextField, 56, SpringLayout.EAST, lblNewLabel);
		add(attrRateTextField);
		attrRateTextField.setColumns(10);
		
		lblCrossoverRate = new JLabel("Cross-over rate:");
		springLayout.putConstraint(SpringLayout.NORTH, lblCrossoverRate, 0, SpringLayout.NORTH, lblNewLabel);
		springLayout.putConstraint(SpringLayout.WEST, lblCrossoverRate, 12, SpringLayout.EAST, attrRateTextField);
		add(lblCrossoverRate);
		
		lblNumberOfIndividuals = new JLabel("Number of individuals:");
		springLayout.putConstraint(SpringLayout.WEST, lblNumberOfIndividuals, 0, SpringLayout.WEST, lblSelectAlgorithm);
		add(lblNumberOfIndividuals);
		
		lblOfIterations = new JLabel("Number of iterations:");
		springLayout.putConstraint(SpringLayout.NORTH, lblOfIterations, 0, SpringLayout.NORTH, lblNumberOfIndividuals);
		springLayout.putConstraint(SpringLayout.WEST, lblOfIterations, 0, SpringLayout.WEST, lblCrossoverRate);
		add(lblOfIterations);
		
		iterationsTextField = new JTextField();
		iterationsTextField.setText("500");
		springLayout.putConstraint(SpringLayout.WEST, iterationsTextField, 6, SpringLayout.EAST, lblOfIterations);
		springLayout.putConstraint(SpringLayout.EAST, iterationsTextField, -94, SpringLayout.EAST, this);
		add(iterationsTextField);
		iterationsTextField.setColumns(10);
		
		numIndTextField = new JTextField();
		numIndTextField.setText("25");
		springLayout.putConstraint(SpringLayout.NORTH, lblNumberOfIndividuals, 3, SpringLayout.NORTH, numIndTextField);
		springLayout.putConstraint(SpringLayout.NORTH, numIndTextField, 6, SpringLayout.SOUTH, attrRateTextField);
		springLayout.putConstraint(SpringLayout.WEST, numIndTextField, 0, SpringLayout.WEST, attrRateTextField);
		springLayout.putConstraint(SpringLayout.EAST, numIndTextField, 0, SpringLayout.EAST, attrRateTextField);
		add(numIndTextField);
		numIndTextField.setColumns(10);
		
		coRateTextField = new JTextField();
		coRateTextField.setText("0.05");
		springLayout.putConstraint(SpringLayout.NORTH, iterationsTextField, 6, SpringLayout.SOUTH, coRateTextField);
		springLayout.putConstraint(SpringLayout.NORTH, coRateTextField, 4, SpringLayout.SOUTH, btnConfigure);
		springLayout.putConstraint(SpringLayout.WEST, coRateTextField, 28, SpringLayout.EAST, lblCrossoverRate);
		springLayout.putConstraint(SpringLayout.EAST, coRateTextField, 78, SpringLayout.EAST, lblCrossoverRate);
		add(coRateTextField);
		coRateTextField.setColumns(10);
		
		lblMinOf = new JLabel("Min # of attributes:");
		springLayout.putConstraint(SpringLayout.NORTH, lblMinOf, 12, SpringLayout.SOUTH, lblNumberOfIndividuals);
		springLayout.putConstraint(SpringLayout.WEST, lblMinOf, 0, SpringLayout.WEST, lblSelectAlgorithm);
		add(lblMinOf);
		
		minAttrTextField = new JTextField();
		minAttrTextField.setText("-1");
		springLayout.putConstraint(SpringLayout.NORTH, minAttrTextField, 6, SpringLayout.SOUTH, numIndTextField);
		springLayout.putConstraint(SpringLayout.WEST, minAttrTextField, 6, SpringLayout.EAST, lblMinOf);
		springLayout.putConstraint(SpringLayout.EAST, minAttrTextField, 56, SpringLayout.EAST, lblMinOf);
		add(minAttrTextField);
		minAttrTextField.setColumns(10);
		
		lblMaxOf = new JLabel("Max # of attributes:");
		springLayout.putConstraint(SpringLayout.NORTH, lblMaxOf, 0, SpringLayout.NORTH, lblMinOf);
		springLayout.putConstraint(SpringLayout.WEST, lblMaxOf, 6, SpringLayout.EAST, minAttrTextField);
		add(lblMaxOf);
		
		maxAttrTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, maxAttrTextField, -3, SpringLayout.NORTH, lblMinOf);
		springLayout.putConstraint(SpringLayout.WEST, maxAttrTextField, 6, SpringLayout.EAST, lblMaxOf);
		springLayout.putConstraint(SpringLayout.EAST, maxAttrTextField, 56, SpringLayout.EAST, lblMaxOf);
		add(maxAttrTextField);
		maxAttrTextField.setColumns(10);
		
		lblStepSize = new JLabel("Step size:");
		springLayout.putConstraint(SpringLayout.NORTH, lblStepSize, 0, SpringLayout.NORTH, lblMinOf);
		springLayout.putConstraint(SpringLayout.WEST, lblStepSize, 6, SpringLayout.EAST, maxAttrTextField);
		add(lblStepSize);
		
		stepSizeTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, stepSizeTextField, 56, SpringLayout.SOUTH, btnConfigure);
		springLayout.putConstraint(SpringLayout.WEST, stepSizeTextField, 6, SpringLayout.EAST, lblStepSize);
		springLayout.putConstraint(SpringLayout.EAST, stepSizeTextField, 56, SpringLayout.EAST, lblStepSize);
		add(stepSizeTextField);
		stepSizeTextField.setColumns(10);
		
		lblNumberOfThreads = new JLabel("Number of threads:");
		springLayout.putConstraint(SpringLayout.WEST, lblNumberOfThreads, 0, SpringLayout.WEST, lblSelectAlgorithm);
		springLayout.putConstraint(SpringLayout.SOUTH, lblNumberOfThreads, -11, SpringLayout.SOUTH, this);
		add(lblNumberOfThreads);
		
		threadsTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, threadsTextField, -3, SpringLayout.NORTH, lblNumberOfThreads);
		springLayout.putConstraint(SpringLayout.WEST, threadsTextField, 6, SpringLayout.EAST, lblNumberOfThreads);
		springLayout.putConstraint(SpringLayout.EAST, threadsTextField, 0, SpringLayout.EAST, minAttrTextField);
		add(threadsTextField);
		threadsTextField.setColumns(10);
		
		cvCheckBox = new JCheckBox("Use Cross-Validation. Number of folds:");
		springLayout.putConstraint(SpringLayout.WEST, cvCheckBox, 0, SpringLayout.WEST, lblSelectAlgorithm);
		add(cvCheckBox);
		
		cvTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, cvTextField, 1, SpringLayout.NORTH, cvCheckBox);
		springLayout.putConstraint(SpringLayout.WEST, cvTextField, 2, SpringLayout.EAST, cvCheckBox);
		springLayout.putConstraint(SpringLayout.EAST, cvTextField, 0, SpringLayout.EAST, lblCrossoverRate);
		cvTextField.setText("10");
		add(cvTextField);
		cvTextField.setColumns(10);
		
		lblForFree = new JLabel("-1 for free attribute selection.");
		springLayout.putConstraint(SpringLayout.NORTH, cvCheckBox, 6, SpringLayout.SOUTH, lblForFree);
		lblForFree.setEnabled(false);
		springLayout.putConstraint(SpringLayout.NORTH, lblForFree, 6, SpringLayout.SOUTH, lblMinOf);
		springLayout.putConstraint(SpringLayout.WEST, lblForFree, 0, SpringLayout.WEST, lblSelectAlgorithm);
		add(lblForFree);
	}
	public JComboBox getAlgorithmComboBox() {
		return algorithmComboBox;
	}
	public JButton getBtnConfigure() {
		return btnConfigure;
	}
	public JTextField getAttrRateTextField() {
		return attrRateTextField;
	}
	public JTextField getCoRateTextField() {
		return coRateTextField;
	}
	public JTextField getNumIndTextField() {
		return numIndTextField;
	}
	public JTextField getIterationsTextField() {
		return iterationsTextField;
	}
	public JTextField getMaxAttrTextField() {
		return maxAttrTextField;
	}
	public JTextField getStepSizeTextField() {
		return stepSizeTextField;
	}
	public JTextField getMinAttrTextField() {
		return minAttrTextField;
	}
	public JTextField getThreadsTextField() {
		return threadsTextField;
	}
	public JCheckBox getCvCheckBox() {
		return cvCheckBox;
	}
	public JTextField getCvTextField() {
		return cvTextField;
	}
}

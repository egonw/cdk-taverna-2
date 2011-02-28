package org.openscience.cdk.applications.taverna.ui.weka.panels.learning;

import javax.swing.JPanel;

import org.openscience.cdk.applications.taverna.ui.weka.panels.AbstractLearningConfigurationFrame;

import weka.classifiers.functions.MultilayerPerceptron;

import javax.swing.JLabel;
import javax.swing.border.BevelBorder;
import javax.swing.SpringLayout;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import java.awt.Font;
import java.awt.Dimension;
import java.util.ArrayList;

public class MultilayerPerceptronFrame extends AbstractLearningConfigurationFrame {
	private JTextField startTextField;
	private JTextField endTextField;
	private JTextField stepSizeTextField;
	private JTextPane annotationTextPane;

	public MultilayerPerceptronFrame() {
		setPreferredSize(new Dimension(408, 140));
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);

		JLabel lblNewLabel = new JLabel("Number Of Hidden Neurons:");
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel, 12, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, lblNewLabel, 148, SpringLayout.WEST, this);
		add(lblNewLabel);

		startTextField = new JTextField();
		startTextField.setText("a");
		springLayout.putConstraint(SpringLayout.NORTH, startTextField, 9, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, startTextField, 8, SpringLayout.EAST, lblNewLabel);
		springLayout.putConstraint(SpringLayout.EAST, startTextField, 59, SpringLayout.EAST, lblNewLabel);
		add(startTextField);
		startTextField.setColumns(10);

		endTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, endTextField, 9, SpringLayout.NORTH, this);
		add(endTextField);
		endTextField.setColumns(10);

		JLabel lblNewLabel_1 = new JLabel("-");
		springLayout.putConstraint(SpringLayout.EAST, lblNewLabel_1, 12, SpringLayout.EAST, startTextField);
		springLayout.putConstraint(SpringLayout.WEST, endTextField, 8, SpringLayout.EAST, lblNewLabel_1);
		springLayout.putConstraint(SpringLayout.EAST, endTextField, 58, SpringLayout.EAST, lblNewLabel_1);
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel_1, 8, SpringLayout.EAST, startTextField);
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel_1, 12, SpringLayout.NORTH, this);
		add(lblNewLabel_1);

		JLabel lblNewLabel_2 = new JLabel("Step Size:");
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel_2, 12, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel_2, 10, SpringLayout.EAST, endTextField);
		springLayout.putConstraint(SpringLayout.SOUTH, lblNewLabel_2, 0, SpringLayout.SOUTH, lblNewLabel);
		add(lblNewLabel_2);

		stepSizeTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, stepSizeTextField, 9, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, stepSizeTextField, 8, SpringLayout.EAST, lblNewLabel_2);
		springLayout.putConstraint(SpringLayout.EAST, stepSizeTextField, 58, SpringLayout.EAST, lblNewLabel_2);
		add(stepSizeTextField);
		stepSizeTextField.setColumns(10);

		annotationTextPane = new JTextPane();
		springLayout.putConstraint(SpringLayout.SOUTH, annotationTextPane, 105, SpringLayout.SOUTH, startTextField);
		annotationTextPane.setFont(new Font("Courier New", Font.PLAIN, 11));
		springLayout.putConstraint(SpringLayout.EAST, annotationTextPane, 0, SpringLayout.EAST, stepSizeTextField);
		annotationTextPane.setEnabled(false);
		springLayout.putConstraint(SpringLayout.NORTH, annotationTextPane, 7, SpringLayout.SOUTH, startTextField);
		springLayout.putConstraint(SpringLayout.WEST, annotationTextPane, 14, SpringLayout.WEST, this);
		add(annotationTextPane);

		annotationTextPane.setText("Leave the second field free for only one step.\n"
				+ "Wildcard values (first field only):\n'a' = (attribs + classes) / 2\n'i' = attribs\n'o' = classes\n"
				+ "'t' = attribs .+ classes ");
	}

	@Override
	public String getName() {
		return "Three-Layer Perceptron Neural Network";
	}

	@Override
	public String[] getOptions() {
		if (this.startTextField.getText().equals("a") || this.startTextField.getText().equals("i")
				|| this.startTextField.getText().equals("o") || this.startTextField.getText().equals("t")) {
			return new String[] { "-H " + this.startTextField.getText() };
		} else if (this.endTextField.getText().trim().equals("")) {
			return new String[] { "-H " + this.startTextField.getText() };
		} else {
			int stepSize = Integer.parseInt(this.stepSizeTextField.getText());
			int current = Integer.parseInt(this.startTextField.getText());
			int goal = Integer.parseInt(this.endTextField.getText());
			ArrayList<String> tempOpt = new ArrayList<String>();
			do {
				tempOpt.add("-H " + current);
				current += stepSize;
			} while (current <= goal);
			String[] options = new String[tempOpt.size()];
			return tempOpt.toArray(options);
		}
	}

	@Override
	public boolean checkValues() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isConfigurationChanged() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Class<?> getConfiguredClass() {
		return MultilayerPerceptron.class;
	}

	public JTextField getStartTextField() {
		return startTextField;
	}

	public JTextField getEndTextField() {
		return endTextField;
	}

	public JTextField getStepSizeTextField() {
		return stepSizeTextField;
	}
}

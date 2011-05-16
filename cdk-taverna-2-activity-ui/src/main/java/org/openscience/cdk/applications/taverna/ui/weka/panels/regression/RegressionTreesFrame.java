package org.openscience.cdk.applications.taverna.ui.weka.panels.regression;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;

import org.openscience.cdk.applications.taverna.ui.UITools;
import org.openscience.cdk.applications.taverna.ui.weka.panels.AbstractRegressionConfigurationFrame;

import weka.classifiers.trees.M5P;

public class RegressionTreesFrame extends AbstractRegressionConfigurationFrame {

	private static final long serialVersionUID = 7779779302832150699L;
	private JTextField higherLimitTextField;
	private JTextField lowerLimitTextField;
	private JTextField stepSizeTextField;
	private JTextField txtLeaveTheSecond;
	private JCheckBox unprunedCheckBox;
	private JCheckBox unsmoothedCheckBox;

	public RegressionTreesFrame() {
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setPreferredSize(new Dimension(410, 275));
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);

		JLabel lblMinimumNumberOf = new JLabel("Minimum # of instances per leaf:");
		springLayout.putConstraint(SpringLayout.NORTH, lblMinimumNumberOf, 12, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, lblMinimumNumberOf, -240, SpringLayout.EAST, this);
		add(lblMinimumNumberOf);

		higherLimitTextField = new JTextField();
		springLayout
				.putConstraint(SpringLayout.NORTH, higherLimitTextField, -3, SpringLayout.NORTH, lblMinimumNumberOf);
		higherLimitTextField.setColumns(10);
		add(higherLimitTextField);

		JLabel label = new JLabel("Step Size:");
		springLayout.putConstraint(SpringLayout.NORTH, label, 0, SpringLayout.NORTH, lblMinimumNumberOf);
		springLayout.putConstraint(SpringLayout.WEST, label, 21, SpringLayout.EAST, higherLimitTextField);
		add(label);

		lowerLimitTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, lowerLimitTextField, -3, SpringLayout.NORTH, lblMinimumNumberOf);
		springLayout.putConstraint(SpringLayout.WEST, lowerLimitTextField, 6, SpringLayout.EAST, lblMinimumNumberOf);
		springLayout.putConstraint(SpringLayout.EAST, lowerLimitTextField, -194, SpringLayout.EAST, this);
		lowerLimitTextField.setText("4");
		lowerLimitTextField.setColumns(10);
		add(lowerLimitTextField);

		JLabel label_1 = new JLabel("-");
		springLayout.putConstraint(SpringLayout.WEST, higherLimitTextField, 6, SpringLayout.EAST, label_1);
		springLayout.putConstraint(SpringLayout.EAST, higherLimitTextField, 46, SpringLayout.EAST, label_1);
		springLayout.putConstraint(SpringLayout.NORTH, label_1, 14, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, label_1, 8, SpringLayout.EAST, lowerLimitTextField);
		springLayout.putConstraint(SpringLayout.EAST, label_1, 224, SpringLayout.WEST, this);
		add(label_1);

		stepSizeTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, stepSizeTextField, -3, SpringLayout.NORTH, lblMinimumNumberOf);
		springLayout.putConstraint(SpringLayout.WEST, stepSizeTextField, 6, SpringLayout.EAST, label);
		springLayout.putConstraint(SpringLayout.EAST, stepSizeTextField, 385, SpringLayout.WEST, this);
		add(stepSizeTextField);
		stepSizeTextField.setColumns(10);

		unprunedCheckBox = new JCheckBox("Use unpruned tree/rules");
		springLayout.putConstraint(SpringLayout.WEST, unprunedCheckBox, 0, SpringLayout.WEST, lblMinimumNumberOf);
		add(unprunedCheckBox);

		unsmoothedCheckBox = new JCheckBox("Use unsmoothed predictions");
		springLayout.putConstraint(SpringLayout.NORTH, unsmoothedCheckBox, 6, SpringLayout.SOUTH, unprunedCheckBox);
		springLayout.putConstraint(SpringLayout.WEST, unsmoothedCheckBox, 0, SpringLayout.WEST, lblMinimumNumberOf);
		add(unsmoothedCheckBox);

		txtLeaveTheSecond = new JTextField();
		txtLeaveTheSecond.setEnabled(false);
		springLayout.putConstraint(SpringLayout.NORTH, unprunedCheckBox, 16, SpringLayout.SOUTH, txtLeaveTheSecond);
		springLayout.putConstraint(SpringLayout.NORTH, txtLeaveTheSecond, 10, SpringLayout.SOUTH, higherLimitTextField);
		springLayout.putConstraint(SpringLayout.WEST, txtLeaveTheSecond, 0, SpringLayout.WEST, lblMinimumNumberOf);
		springLayout.putConstraint(SpringLayout.EAST, txtLeaveTheSecond, -12, SpringLayout.EAST, this);
		txtLeaveTheSecond.setBorder(null);
		txtLeaveTheSecond.setEditable(false);
		txtLeaveTheSecond.setText(" Leave the second field free to perform only one step.");
		add(txtLeaveTheSecond);
		txtLeaveTheSecond.setColumns(10);
	}

	@Override
	public Class<?> getConfiguredClass() {
		return M5P.class;
	}

	@Override
	public String getName() {
		return "M5Base Regression Tree";
	}

	@Override
	public boolean checkValues() {
		if (!UITools.checkTextFieldValueInt(this, "Lower limit", this.lowerLimitTextField, 1, Integer.MAX_VALUE)) {
			return false;
		}
		if (!higherLimitTextField.getText().trim().equals("")) {
			if (!UITools.checkTextFieldValueInt(this, "Higher limit", this.higherLimitTextField, 1, Integer.MAX_VALUE)) {
				return false;
			}
			int lower = Integer.parseInt(this.lowerLimitTextField.getText());
			int higher = Integer.parseInt(this.higherLimitTextField.getText());
			if (higher <= lower) {
				JOptionPane.showMessageDialog(this, "The higher limit has to be greater than the lower limit!",
						"Illegal Argument", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
	}

	@Override
	public String[] getOptions() {
		ArrayList<String> tempOpt = new ArrayList<String>();
		if (this.higherLimitTextField.getText().trim().equals("")) {
			String option = "-M " + this.lowerLimitTextField.getText() + " ";
			option += "-R ";
			if (this.unprunedCheckBox.isSelected()) {
				option += "-N ";
			}
			if (this.unsmoothedCheckBox.isSelected()) {
				option += "-U ";
			}
			tempOpt.add(option);
		} else {
			int stepSize = Integer.parseInt(this.stepSizeTextField.getText());
			int current = Integer.parseInt(this.lowerLimitTextField.getText());
			int goal = Integer.parseInt(this.higherLimitTextField.getText());
			do {
				String option = "-M " + current + " ";
				option += "-R ";
				if (this.unprunedCheckBox.isSelected()) {
					option += "-N ";
				}
				if (this.unsmoothedCheckBox.isSelected()) {
					option += "-U ";
				}
				tempOpt.add(option);
				current += stepSize;
			} while (current <= goal);
		}
		String[] options = new String[tempOpt.size()];
		return tempOpt.toArray(options);
	}

	@Override
	public void setOptions(String[] options) {
		this.unprunedCheckBox.setSelected(false);
		this.unsmoothedCheckBox.setSelected(false);
		String[] optMin = options[0].split(" ");
		this.lowerLimitTextField.setText(optMin[1]);
		for (int i = 0; i < optMin.length; i++) {
			if (optMin[i].equals("-N")) {
				this.unprunedCheckBox.setSelected(true);
			}
			if (optMin[i].equals("-U")) {
				this.unsmoothedCheckBox.setSelected(true);
			}
		}
		if (options.length > 1) {
			String[] optMax = options[options.length - 1].split(" ");
			this.stepSizeTextField.setText(""
					+ (Integer.parseInt(options[1].split(" ")[1]) - Integer.parseInt(options[0].split(" ")[1])));
			this.higherLimitTextField.setText(optMax[1]);
		}
	}

	@Override
	public void makeSingleOption() {
		this.higherLimitTextField.setEnabled(false);
		this.stepSizeTextField.setEnabled(false);
	}

	@Override
	public boolean useThreading() {
		return true;
	}
}

package org.openscience.cdk.applications.taverna.ui.weka.panels.learning;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;

import org.openscience.cdk.applications.taverna.ui.weka.panels.AbstractLearningConfigurationFrame;

import weka.classifiers.functions.LibSVM;

public class SupportVectorMachinesFrame extends AbstractLearningConfigurationFrame {

	private static final long serialVersionUID = 5685630159412977072L;
	private static final String[] SVM_TYPES = new String[] { "epsilon-SVR", "nu-SVR" };
	private static final String[] KERNEL_TYPES = new String[] { "Linear: u'*v",
			"Polynomial: (gamma*u'*v + coef0)^degree", "Radial basis function: exp(-gamma*|u-v|^2)",
			"Sigmoid: tanh(gamma*u'*v + coef0)" };
	private JTextField degreeTextField;
	private JTextField gammaTextField;
	private JTextField coefTextField;
	private JTextField startTextField;
	private JTextField endTextField;
	private JTextField stepSizeTextField;
	private JComboBox svmTypeComboBox;
	private JComboBox kernelComboBox;

	public SupportVectorMachinesFrame() {
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setPreferredSize(new Dimension(410, 275));
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);

		JLabel lblNewLabel = new JLabel("SVM type:");
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel, 10, SpringLayout.WEST, this);
		add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Kernel function:");
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel_1, 10, SpringLayout.WEST, this);
		add(lblNewLabel_1);

		DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel(SVM_TYPES);
		svmTypeComboBox = new JComboBox(comboBoxModel);
		springLayout.putConstraint(SpringLayout.NORTH, svmTypeComboBox, 7, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel, 3, SpringLayout.NORTH, svmTypeComboBox);
		springLayout.putConstraint(SpringLayout.EAST, svmTypeComboBox, -10, SpringLayout.EAST, this);
		add(svmTypeComboBox);

		comboBoxModel = new DefaultComboBoxModel(KERNEL_TYPES);
		kernelComboBox = new JComboBox(comboBoxModel);
		springLayout.putConstraint(SpringLayout.NORTH, kernelComboBox, 12, SpringLayout.SOUTH, svmTypeComboBox);
		springLayout.putConstraint(SpringLayout.WEST, svmTypeComboBox, 0, SpringLayout.WEST, kernelComboBox);
		springLayout.putConstraint(SpringLayout.WEST, kernelComboBox, 6, SpringLayout.EAST, lblNewLabel_1);
		springLayout.putConstraint(SpringLayout.EAST, kernelComboBox, -10, SpringLayout.EAST, this);
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel_1, 3, SpringLayout.NORTH, kernelComboBox);
		add(kernelComboBox);

		degreeTextField = new JTextField();
		degreeTextField.setText("3");
		springLayout.putConstraint(SpringLayout.NORTH, degreeTextField, 6, SpringLayout.SOUTH, kernelComboBox);
		springLayout.putConstraint(SpringLayout.WEST, degreeTextField, 0, SpringLayout.WEST, svmTypeComboBox);
		add(degreeTextField);
		degreeTextField.setColumns(10);

		gammaTextField = new JTextField();
		gammaTextField.setText("1/k");
		springLayout.putConstraint(SpringLayout.NORTH, gammaTextField, 6, SpringLayout.SOUTH, degreeTextField);
		springLayout.putConstraint(SpringLayout.WEST, gammaTextField, 0, SpringLayout.WEST, svmTypeComboBox);
		add(gammaTextField);
		gammaTextField.setColumns(10);

		coefTextField = new JTextField();
		coefTextField.setText("0");
		springLayout.putConstraint(SpringLayout.NORTH, coefTextField, 6, SpringLayout.SOUTH, gammaTextField);
		springLayout.putConstraint(SpringLayout.WEST, coefTextField, 0, SpringLayout.WEST, svmTypeComboBox);
		add(coefTextField);
		coefTextField.setColumns(10);

		startTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, startTextField, 6, SpringLayout.SOUTH, coefTextField);
		springLayout.putConstraint(SpringLayout.EAST, startTextField, 40, SpringLayout.WEST, svmTypeComboBox);
		startTextField.setText("1");
		springLayout.putConstraint(SpringLayout.WEST, startTextField, 0, SpringLayout.WEST, svmTypeComboBox);
		add(startTextField);
		startTextField.setColumns(10);

		JLabel lblNewLabel_2 = new JLabel("Degree:");
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel_2, 3, SpringLayout.NORTH, degreeTextField);
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel_2, 0, SpringLayout.WEST, lblNewLabel);
		add(lblNewLabel_2);

		JLabel lblNewLabel_3 = new JLabel("Gamma:");
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel_3, 3, SpringLayout.NORTH, gammaTextField);
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel_3, 0, SpringLayout.WEST, lblNewLabel);
		add(lblNewLabel_3);

		JLabel lblNewLabel_4 = new JLabel("Coef0:");
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel_4, 3, SpringLayout.NORTH, coefTextField);
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel_4, 0, SpringLayout.WEST, lblNewLabel);
		add(lblNewLabel_4);

		JLabel lblCostParameter = new JLabel("Cost parameter:");
		springLayout.putConstraint(SpringLayout.NORTH, lblCostParameter, 3, SpringLayout.NORTH, startTextField);
		springLayout.putConstraint(SpringLayout.WEST, lblCostParameter, 0, SpringLayout.WEST, lblNewLabel);
		add(lblCostParameter);

		JLabel lblNewLabel_5 = new JLabel("Default: 1/k otherwise numeric value!");
		lblNewLabel_5.setEnabled(false);
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel_5, 3, SpringLayout.NORTH, gammaTextField);
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel_5, 6, SpringLayout.EAST, gammaTextField);
		add(lblNewLabel_5);

		JLabel lblNewLabel_6 = new JLabel("-");
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel_6, 3, SpringLayout.NORTH, startTextField);
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel_6, 6, SpringLayout.EAST, startTextField);
		add(lblNewLabel_6);

		endTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, endTextField, 0, SpringLayout.NORTH, startTextField);
		springLayout.putConstraint(SpringLayout.WEST, endTextField, 6, SpringLayout.EAST, lblNewLabel_6);
		springLayout.putConstraint(SpringLayout.EAST, endTextField, 46, SpringLayout.EAST, lblNewLabel_6);
		add(endTextField);
		endTextField.setColumns(10);

		JLabel lblStepsize = new JLabel("Stepsize:");
		springLayout.putConstraint(SpringLayout.NORTH, lblStepsize, 3, SpringLayout.NORTH, startTextField);
		springLayout.putConstraint(SpringLayout.WEST, lblStepsize, 6, SpringLayout.EAST, endTextField);
		add(lblStepsize);

		stepSizeTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, stepSizeTextField, 37, SpringLayout.SOUTH, lblNewLabel_5);
		springLayout.putConstraint(SpringLayout.WEST, stepSizeTextField, 8, SpringLayout.EAST, lblStepsize);
		springLayout.putConstraint(SpringLayout.EAST, stepSizeTextField, 48, SpringLayout.EAST, lblStepsize);
		add(stepSizeTextField);
		stepSizeTextField.setColumns(10);

		JLabel lblNewLabel_7 = new JLabel(" Leave the second field free to perform only one step.");
		lblNewLabel_7.setEnabled(false);
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel_7, 10, SpringLayout.SOUTH, stepSizeTextField);
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel_7, 0, SpringLayout.WEST, lblNewLabel);
		add(lblNewLabel_7);
	}

	@Override
	public Class<?> getConfiguredClass() {
		return LibSVM.class;
	}

	@Override
	public String getName() {
		return "Support Vector Machines";
	}

	@Override
	public boolean checkValues() {
		return true;
	}

	@Override
	public String[] getOptions() {
		ArrayList<String> tempOpt = new ArrayList<String>();
		int svmType = this.svmTypeComboBox.getSelectedIndex() + 3;
		int kernelType = this.kernelComboBox.getSelectedIndex();
		int degree = Integer.parseInt(this.degreeTextField.getText());
		double coef = Double.parseDouble(this.coefTextField.getText());
		if (this.endTextField.getText().trim().equals("")) {
			String option = "";
			option += "-S " + svmType + " ";
			option += "-K " + kernelType + " ";
			option += "-D " + degree + " ";
			option += "-R " + coef + " ";
			option += "-C " + this.startTextField.getText() + " ";
			if (!this.gammaTextField.getText().equals("1/k")) {
				double gamma = Double.parseDouble(this.degreeTextField.getText());
				option += "-G " + gamma + " ";
			}
			tempOpt.add(option);
		} else {
			int stepSize = Integer.parseInt(this.stepSizeTextField.getText());
			int current = Integer.parseInt(this.startTextField.getText());
			int goal = Integer.parseInt(this.endTextField.getText());
			do {
				String option = "";
				option += "-S " + svmType + " ";
				option += "-K " + kernelType + " ";
				option += "-D " + degree + " ";
				option += "-R " + coef + " ";
				option += "-C " + current + " ";
				if (!this.gammaTextField.getText().equals("1/k")) {
					double gamma = Double.parseDouble(this.degreeTextField.getText());
					option += "-G " + gamma + " ";
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
		String[] optMin = options[0].split(" ");
		this.svmTypeComboBox.setSelectedIndex(Integer.parseInt(optMin[1]) - 3);
		this.kernelComboBox.setSelectedIndex(Integer.parseInt(optMin[3]));
		this.degreeTextField.setText(optMin[5]);
		this.coefTextField.setText(optMin[7]);
		this.startTextField.setText(optMin[9]);
		if (optMin.length > 10) {
			this.gammaTextField.setText(optMin[11]);
		} else {
			this.gammaTextField.setText("1/k");
		}
		if (options.length > 1) {
			String[] optMax = options[options.length - 1].split(" ");
			this.stepSizeTextField.setText(""
					+ (Integer.parseInt(options[1].split(" ")[9]) - Integer.parseInt(options[0].split(" ")[9])));
			this.endTextField.setText(optMax[9]);
		}
	}

	@Override
	public void makeSingleOption() {
		this.endTextField.setEnabled(false);
		this.stepSizeTextField.setEnabled(false);
	}
}

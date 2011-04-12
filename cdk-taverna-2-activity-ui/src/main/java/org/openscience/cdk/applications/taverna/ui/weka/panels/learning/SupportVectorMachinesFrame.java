package org.openscience.cdk.applications.taverna.ui.weka.panels.learning;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;

import org.openscience.cdk.applications.taverna.ui.UITools;
import org.openscience.cdk.applications.taverna.ui.weka.panels.AbstractLearningConfigurationFrame;

import weka.classifiers.functions.LibSVM;

public class SupportVectorMachinesFrame extends AbstractLearningConfigurationFrame {

	private static final long serialVersionUID = 5685630159412977072L;
	private static final String[] SVM_TYPES = new String[] { "epsilon-SVR", "nu-SVR" };
	private static final String[] KERNEL_TYPES = new String[] { "Linear: u'*v",
			"Polynomial: (gamma*u'*v + coef0)^degree", "Radial basis function: exp(-gamma*|u-v|^2)",
			"Sigmoid: tanh(gamma*u'*v + coef0)" };
	private JTextField degreeLowTextField;
	private JTextField gammaLowTextField;
	private JTextField coefTextField;
	private JTextField startTextField;
	private JTextField endTextField;
	private JTextField stepSizeTextField;
	private JComboBox svmTypeComboBox;
	private JComboBox kernelComboBox;
	private JLabel lblNewLabel_8;
	private JTextField gammaHighTextField;
	private JLabel lblStepSize;
	private JTextField gammaStepsizeTextField;

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

		degreeLowTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, degreeLowTextField, 16, SpringLayout.SOUTH, kernelComboBox);
		springLayout.putConstraint(SpringLayout.EAST, degreeLowTextField, 40, SpringLayout.WEST, svmTypeComboBox);
		degreeLowTextField.setText("3");
		springLayout.putConstraint(SpringLayout.WEST, degreeLowTextField, 0, SpringLayout.WEST, svmTypeComboBox);
		add(degreeLowTextField);
		degreeLowTextField.setColumns(10);

		gammaLowTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, gammaLowTextField, 4, SpringLayout.SOUTH, degreeLowTextField);
		springLayout.putConstraint(SpringLayout.EAST, gammaLowTextField, 40, SpringLayout.WEST, svmTypeComboBox);
		gammaLowTextField.setText("1/k");
		springLayout.putConstraint(SpringLayout.WEST, gammaLowTextField, 0, SpringLayout.WEST, svmTypeComboBox);
		add(gammaLowTextField);
		gammaLowTextField.setColumns(10);

		coefTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, coefTextField, 30, SpringLayout.SOUTH, gammaLowTextField);
		springLayout.putConstraint(SpringLayout.EAST, coefTextField, 40, SpringLayout.WEST, svmTypeComboBox);
		coefTextField.setText("0.0");
		springLayout.putConstraint(SpringLayout.WEST, coefTextField, 0, SpringLayout.WEST, svmTypeComboBox);
		add(coefTextField);
		coefTextField.setColumns(10);

		startTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, startTextField, 4, SpringLayout.SOUTH, coefTextField);
		springLayout.putConstraint(SpringLayout.EAST, startTextField, 40, SpringLayout.WEST, svmTypeComboBox);
		startTextField.setText("1");
		springLayout.putConstraint(SpringLayout.WEST, startTextField, 0, SpringLayout.WEST, svmTypeComboBox);
		add(startTextField);
		startTextField.setColumns(10);

		JLabel lblNewLabel_2 = new JLabel("Degree:");
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel_2, 3, SpringLayout.NORTH, degreeLowTextField);
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel_2, 0, SpringLayout.WEST, lblNewLabel);
		add(lblNewLabel_2);

		JLabel lblNewLabel_3 = new JLabel("Gamma:");
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel_3, 3, SpringLayout.NORTH, gammaLowTextField);
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
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel_5, 6, SpringLayout.SOUTH, gammaLowTextField);
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel_5, 0, SpringLayout.WEST, svmTypeComboBox);
		lblNewLabel_5.setEnabled(false);
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

		JLabel lblStepsize = new JLabel("Step size:");
		springLayout.putConstraint(SpringLayout.NORTH, lblStepsize, 3, SpringLayout.NORTH, startTextField);
		springLayout.putConstraint(SpringLayout.WEST, lblStepsize, 6, SpringLayout.EAST, endTextField);
		add(lblStepsize);

		stepSizeTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, stepSizeTextField, 0, SpringLayout.NORTH, startTextField);
		springLayout.putConstraint(SpringLayout.WEST, stepSizeTextField, 6, SpringLayout.EAST, lblStepsize);
		springLayout.putConstraint(SpringLayout.EAST, stepSizeTextField, 46, SpringLayout.EAST, lblStepsize);
		add(stepSizeTextField);
		stepSizeTextField.setColumns(10);

		JLabel lblNewLabel_7 = new JLabel(" Leave the second field free to perform only one step.");
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel_7, 13, SpringLayout.SOUTH, startTextField);
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel_7, 10, SpringLayout.WEST, this);
		lblNewLabel_7.setEnabled(false);
		add(lblNewLabel_7);

		lblNewLabel_8 = new JLabel("-");
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel_8, 3, SpringLayout.NORTH, gammaLowTextField);
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel_8, 6, SpringLayout.EAST, gammaLowTextField);
		add(lblNewLabel_8);

		gammaHighTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, gammaHighTextField, 0, SpringLayout.NORTH, gammaLowTextField);
		springLayout.putConstraint(SpringLayout.WEST, gammaHighTextField, 0, SpringLayout.WEST, endTextField);
		springLayout.putConstraint(SpringLayout.EAST, gammaHighTextField, 46, SpringLayout.EAST, lblNewLabel_8);
		add(gammaHighTextField);
		gammaHighTextField.setColumns(10);

		lblStepSize = new JLabel("Step size:");
		springLayout.putConstraint(SpringLayout.NORTH, lblStepSize, 3, SpringLayout.NORTH, gammaLowTextField);
		springLayout.putConstraint(SpringLayout.WEST, lblStepSize, 0, SpringLayout.WEST, lblStepsize);
		add(lblStepSize);

		gammaStepsizeTextField = new JTextField();
		springLayout
				.putConstraint(SpringLayout.NORTH, gammaStepsizeTextField, 0, SpringLayout.NORTH, gammaLowTextField);
		springLayout.putConstraint(SpringLayout.WEST, gammaStepsizeTextField, 0, SpringLayout.WEST, stepSizeTextField);
		springLayout.putConstraint(SpringLayout.EAST, gammaStepsizeTextField, 46, SpringLayout.EAST, lblStepSize);
		add(gammaStepsizeTextField);
		gammaStepsizeTextField.setColumns(10);
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
		if (!UITools.checkTextFieldValueInt(this, "Degree", this.degreeLowTextField, 1, Integer.MAX_VALUE)) {
			return false;
		}
		if (!this.gammaLowTextField.getText().equals("1/k")) {
			if (UITools.checkTextFieldValueDouble(this, "Gamma lower limit", this.gammaLowTextField, 0,
					Double.MAX_VALUE)) {
				if (!this.gammaHighTextField.getText().trim().equals("")) {
					if (!UITools.checkTextFieldValueDouble(this, "Gamma higher limit", this.gammaHighTextField, 0,
							Double.MAX_VALUE)
							|| !UITools.checkTextFieldValueDouble(this, "Gamma step size", this.gammaStepsizeTextField,
									0, Double.MAX_VALUE)) {
						return false;
					}
				}
			} else {
				return false;
			}
		}
		if (!this.gammaLowTextField.getText().equals("1/k") && !this.gammaHighTextField.getText().trim().equals("")) {
			double a = Double.parseDouble(this.gammaLowTextField.getText()) ;
			double b = Double.parseDouble(this.gammaHighTextField.getText()) ;
			if(a >= b) {
				JOptionPane.showMessageDialog(this, "The higher gamma limit has to be greater than the lower limit!",
						"Illegal Argument", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		if (UITools.checkTextFieldValueInt(this, "Cost lower limit", this.startTextField, 1, Integer.MAX_VALUE)) {
			if (!this.endTextField.getText().trim().equals("")) {
				if (!UITools.checkTextFieldValueInt(this, "Cost higher limit", this.endTextField, 1, Integer.MAX_VALUE)
						|| !UITools.checkTextFieldValueInt(this, "Cost step size", this.stepSizeTextField, 1,
								Integer.MAX_VALUE)) {
					return false;
				}
			}
		} else {
			return false;
		}
		if (!this.endTextField.getText().trim().equals("")) {
			double a = Double.parseDouble(this.startTextField.getText()) ;
			double b = Double.parseDouble(this.endTextField.getText()) ;
			if(a >= b) {
				JOptionPane.showMessageDialog(this, "The higher cost limit has to be greater than the lower limit!",
						"Illegal Argument", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		if (!UITools.checkTextFieldValueDouble(this, "Coef", this.coefTextField, -Double.MAX_VALUE, Double.MAX_VALUE)) {
			return false;
		}
		return true;
	}

	@Override
	public String[] getOptions() {
		ArrayList<String> tempOpt = new ArrayList<String>();
		int svmType = this.svmTypeComboBox.getSelectedIndex() + 3;
		int kernelType = this.kernelComboBox.getSelectedIndex();
		int degree = Integer.parseInt(this.degreeLowTextField.getText());
		double coef = Double.parseDouble(this.coefTextField.getText());
		// Get gammas
		LinkedList<String> gammas = new LinkedList<String>();
		if (this.gammaHighTextField.getText().trim().equals("")
				|| this.gammaHighTextField.getText().trim().equals("1/k")) {
			gammas.add("-G " + this.gammaLowTextField.getText() + " ");
		} else {
			double stepSize = Double.parseDouble(this.gammaStepsizeTextField.getText());
			double current = Double.parseDouble(this.gammaLowTextField.getText());
			double goal = Double.parseDouble(this.gammaHighTextField.getText());
			do {
				gammas.add("-G " + current + " ");
				current += stepSize;
			} while (current <= goal);
		}
		// Get costs
		LinkedList<String> costs = new LinkedList<String>();
		if (this.endTextField.getText().trim().equals("")) {
			costs.add("-C " + this.startTextField.getText() + " ");
		} else {
			int stepSize = Integer.parseInt(this.stepSizeTextField.getText());
			int current = Integer.parseInt(this.startTextField.getText());
			int goal = Integer.parseInt(this.endTextField.getText());
			do {
				costs.add("-C " + current + " ");
				current += stepSize;
			} while (current <= goal);
		}
		for (int i = 0; i < gammas.size(); i++) {
			for (int j = 0; j < costs.size(); j++) {
				String option = "";
				option += "-S " + svmType + " ";
				option += "-K " + kernelType + " ";
				option += "-D " + degree + " ";
				option += "-R " + coef + " ";
				option += "-J -V ";
				option += costs.get(j);
				if (!gammas.get(i).equals("-G 1/k ")) {
					option += gammas.get(i);
				}
				tempOpt.add(option);
			}
		}
		String[] options = new String[tempOpt.size()];
		return tempOpt.toArray(options);
	}

	@Override
	public void setOptions(String[] options) {
		Integer lowC = null;
		Integer highC = null;
		Integer stepSizeC = null;
		Double lowG = null;
		Double highG = null;
		Double stepSizeG = null;
		Integer svmType = null;
		Integer kernelType = null;
		Integer degree = null;
		Double coef = null;
		boolean firstDif = false;
		for (int i = 0; i < options.length; i++) {
			String[] opt = options[i].split(" ");
			if (i == 0) {
				svmType = Integer.parseInt(opt[1]) - 3;
				kernelType = Integer.parseInt(opt[3]);
				degree = Integer.parseInt(opt[5]);
				coef = Double.parseDouble(opt[7]);
				lowC = Integer.parseInt(opt[11]);
				highC = Integer.parseInt(opt[11]);
				if (opt.length > 12 && !opt[13].equals("1/k")) {
					lowG = Double.parseDouble(opt[13]);
					highG = Double.parseDouble(opt[13]);
				}
			} else {
				lowC = Math.min(lowC, Integer.parseInt(opt[11]));
				highC = Math.max(highC, Integer.parseInt(opt[11]));
				if (opt.length > 12 && !opt[13].equals("1/k")) {
					lowG = Math.min(lowG, Double.parseDouble(opt[13]));
					highG = Math.max(highG, Double.parseDouble(opt[13]));
				}
			}
			if (i == 1) {
				stepSizeC = highC - lowC;
			}
			if (!firstDif && opt.length > 12 && !opt[13].equals("1/k")) {
				if (!highG.equals(lowG)) {
					stepSizeG = highG - lowG;
					firstDif = true;
				}
			}
		}
		this.svmTypeComboBox.setSelectedIndex(svmType);
		this.kernelComboBox.setSelectedIndex(kernelType);
		this.degreeLowTextField.setText("" + degree);
		this.coefTextField.setText("" + coef);
		this.startTextField.setText("" + lowC);
		if (highC != null && stepSizeC != null) {
			this.endTextField.setText("" + highC);
			this.stepSizeTextField.setText("" + stepSizeC);
		} else {
			this.endTextField.setText("");
			this.stepSizeTextField.setText("");
		}
		if (lowG == null) {
			this.gammaLowTextField.setText("1/k");
		} else {
			this.gammaLowTextField.setText("" + lowG);
		}
		if (highG != null && stepSizeG != null) {
			this.gammaHighTextField.setText("" + highG);
			this.gammaStepsizeTextField.setText("" + stepSizeG);
		} else {
			this.gammaHighTextField.setText("");
			this.gammaStepsizeTextField.setText("");
		}
	}

	@Override
	public void makeSingleOption() {
		this.endTextField.setEnabled(false);
		this.stepSizeTextField.setEnabled(false);
		this.gammaHighTextField.setEnabled(false);
		this.gammaStepsizeTextField.setEnabled(false);
	}

	@Override
	public boolean useThreading() {
		return false;
	}
}

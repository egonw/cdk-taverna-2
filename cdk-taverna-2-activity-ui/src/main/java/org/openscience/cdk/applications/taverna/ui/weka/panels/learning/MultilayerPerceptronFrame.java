package org.openscience.cdk.applications.taverna.ui.weka.panels.learning;

import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;

import org.openscience.cdk.applications.taverna.ui.UITools;
import org.openscience.cdk.applications.taverna.ui.weka.panels.AbstractLearningConfigurationFrame;

import weka.classifiers.functions.MultilayerPerceptron;

public class MultilayerPerceptronFrame extends AbstractLearningConfigurationFrame {

	private static final long serialVersionUID = 186871833402337678L;
	private JTextField startTextField;
	private JTextField endTextField;
	private JTextField stepSizeTextField;
	private JTextPane annotationTextPane;
	private JLabel lblNewLabel_3;
	private JLabel lblNewLabel_4;
	private JLabel lblNumberOfEpochs;
	private JTextField learningRateTextField;
	private JTextField momentumRateTextField;
	private JTextField numberEpochsTextField;
	private JCheckBox decayCheckBox;

	public MultilayerPerceptronFrame() {
		setPreferredSize(new Dimension(410, 275));
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);

		JLabel lblNewLabel = new JLabel(" Number Of Hidden Neurons:");
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
		springLayout.putConstraint(SpringLayout.NORTH, annotationTextPane, 9, SpringLayout.SOUTH, startTextField);
		springLayout.putConstraint(SpringLayout.WEST, annotationTextPane, 16, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, annotationTextPane, 99, SpringLayout.SOUTH, startTextField);
		springLayout.putConstraint(SpringLayout.EAST, annotationTextPane, -9, SpringLayout.EAST, this);
		annotationTextPane.setFont(new Font("Courier New", Font.PLAIN, 11));
		annotationTextPane.setEnabled(false);
		add(annotationTextPane);

		annotationTextPane
				.setText("Leave the second field free to perform only one step.\r\nWildcard values (first field only):\r\n'a' = (attribs + classes) / 2\r\n'i' = attribs\r\n'o' = classes\r\n't' = attribs .+ classes ");

		lblNewLabel_3 = new JLabel(" Learning Rate for the backpropagation algorithm:");
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel_3, 10, SpringLayout.WEST, this);
		add(lblNewLabel_3);

		lblNewLabel_4 = new JLabel(" Momentum Rate for the backpropagation algorithm:");
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel_4, 0, SpringLayout.WEST, lblNewLabel);
		add(lblNewLabel_4);

		lblNumberOfEpochs = new JLabel(" Number of epochs to train through:");
		springLayout.putConstraint(SpringLayout.WEST, lblNumberOfEpochs, 0, SpringLayout.WEST, lblNewLabel);
		add(lblNumberOfEpochs);

		learningRateTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.WEST, learningRateTextField, 22, SpringLayout.EAST, lblNewLabel_3);
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel_3, 3, SpringLayout.NORTH, learningRateTextField);
		learningRateTextField.setText("0.3");
		springLayout
				.putConstraint(SpringLayout.NORTH, learningRateTextField, 6, SpringLayout.SOUTH, annotationTextPane);
		add(learningRateTextField);
		learningRateTextField.setColumns(10);

		momentumRateTextField = new JTextField();
		momentumRateTextField.setText("0.2");
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel_4, 3, SpringLayout.NORTH, momentumRateTextField);
		springLayout.putConstraint(SpringLayout.NORTH, momentumRateTextField, 20, SpringLayout.SOUTH,
				learningRateTextField);
		springLayout.putConstraint(SpringLayout.EAST, momentumRateTextField, 0, SpringLayout.EAST,
				learningRateTextField);
		add(momentumRateTextField);
		momentumRateTextField.setColumns(10);

		numberEpochsTextField = new JTextField();
		numberEpochsTextField.setText("500");
		springLayout.putConstraint(SpringLayout.NORTH, lblNumberOfEpochs, 3, SpringLayout.NORTH, numberEpochsTextField);
		springLayout.putConstraint(SpringLayout.NORTH, numberEpochsTextField, 18, SpringLayout.SOUTH,
				momentumRateTextField);
		springLayout.putConstraint(SpringLayout.WEST, numberEpochsTextField, 0, SpringLayout.WEST,
				learningRateTextField);
		add(numberEpochsTextField);
		numberEpochsTextField.setColumns(10);

		decayCheckBox = new JCheckBox("Learning rate decay will occur");
		springLayout.putConstraint(SpringLayout.NORTH, decayCheckBox, 14, SpringLayout.SOUTH, lblNumberOfEpochs);
		springLayout.putConstraint(SpringLayout.WEST, decayCheckBox, 0, SpringLayout.WEST, lblNewLabel);
		add(decayCheckBox);
	}

	@Override
	public String getName() {
		return "Three-Layer Perceptron Neural Network";
	}

	@Override
	public String[] getOptions() {
		ArrayList<String> tempOpt = new ArrayList<String>();
		int epochs = Integer.parseInt(this.numberEpochsTextField.getText());
		double learningRate = Double.parseDouble(this.learningRateTextField.getText());
		double momentumRate = Double.parseDouble(this.momentumRateTextField.getText());
		if (this.startTextField.getText().equals("a") || this.startTextField.getText().equals("i")
				|| this.startTextField.getText().equals("o") || this.startTextField.getText().equals("t")
				|| this.endTextField.getText().trim().equals("")) {
			String option = "-H " + this.startTextField.getText() + " ";
			option += "-L " + learningRate + " ";
			option += "-M " + momentumRate + " ";
			option += "-N " + epochs + " ";
			if (this.decayCheckBox.isSelected()) {
				option += "-D";
			}
			tempOpt.add(option);
		} else {
			int stepSize = Integer.parseInt(this.stepSizeTextField.getText());
			int current = Integer.parseInt(this.startTextField.getText());
			int goal = Integer.parseInt(this.endTextField.getText());
			do {
				String option = "-H " + current + " ";
				option += "-L " + learningRate + " ";
				option += "-M " + momentumRate + " ";
				option += "-N " + epochs + " ";
				if (this.decayCheckBox.isSelected()) {
					option += "-D";
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
		this.startTextField.setText(optMin[1]);
		this.learningRateTextField.setText(optMin[3]);
		this.momentumRateTextField.setText(optMin[5]);
		this.numberEpochsTextField.setText(optMin[7]);
		if (optMin.length > 8) {
			this.decayCheckBox.setSelected(true);
		} else {
			this.decayCheckBox.setSelected(false);
		}
		if (options.length > 1) {
			String[] optMax = options[options.length - 1].split(" ");
			this.stepSizeTextField.setText(""
					+ (Integer.parseInt(options[1].split(" ")[1]) - Integer.parseInt(options[0].split(" ")[1])));
			this.endTextField.setText(optMax[1]);
		}
	}

	@Override
	public boolean checkValues() {
		if (!(this.startTextField.getText().equals("a") || this.startTextField.getText().equals("i")
				|| this.startTextField.getText().equals("o") || this.startTextField.getText().equals("t"))) {
			if (!UITools.checkTextFieldValueInt(this, "# of Neurons lower bound", startTextField, 1, Integer.MAX_VALUE)) {
				return false;
			}
		}
		if (!endTextField.getText().trim().equals("")) {
			if (!UITools.checkTextFieldValueInt(this, "# of Neurons higher bound", endTextField, 1, Integer.MAX_VALUE)) {
				return false;
			}
			int lower = Integer.parseInt(this.startTextField.getText());
			int higher = Integer.parseInt(this.endTextField.getText());
			if (higher <= lower) {
				JOptionPane.showMessageDialog(this, "The higher bound limit has to be greater than the lower limit!",
						"Illegal Argument", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		if (!(endTextField.getText().trim().equals("") && stepSizeTextField.getText().trim().equals(""))) {
			if (!UITools.checkTextFieldValueInt(this, "Step size", stepSizeTextField, 1, Integer.MAX_VALUE)) {
				return false;
			}
		}
		if (!UITools.checkTextFieldValueDouble(this, "Learning rate", learningRateTextField, 0d, 1d)) {
			return false;
		}
		if (!UITools.checkTextFieldValueDouble(this, "Momentum rate", momentumRateTextField, 0d, 1d)) {
			return false;
		}
		if (!UITools.checkTextFieldValueInt(this, "Number of epochs", numberEpochsTextField, 1, Integer.MAX_VALUE)) {
			return false;
		}
		return true;
	}

	@Override
	public Class<?> getConfiguredClass() {
		return MultilayerPerceptron.class;
	}

	@Override
	public void makeSingleOption() {
		this.endTextField.setEnabled(false);
		this.stepSizeTextField.setEnabled(false);
	}

	@Override
	public boolean useThreading() {
		return true;
	}
}

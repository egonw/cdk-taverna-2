package org.openscience.cdk.applications.taverna.ui.weka.panels.classification;

import java.awt.Dimension;

import org.openscience.cdk.applications.taverna.ui.UITools;
import org.openscience.cdk.applications.taverna.ui.weka.panels.AbstractClassificationConfigurationFrame;

import com.sun.tools.internal.jxc.apt.Options;

import weka.classifiers.trees.J48;
import javax.swing.JCheckBox;
import javax.swing.SpringLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

public class J48Frame extends AbstractClassificationConfigurationFrame {
	private JCheckBox chckbxUseunpruned;
	private JLabel lblPruningConfidenceThreshold;
	private JTextField pruningConfidenceTextField;
	private JLabel lblMinNumberOf;
	private JTextField instancesTextField;
	private JCheckBox chckbxUseReducedError;
	private JTextField foldsTextField;
	private JLabel lblNumberOfFolds;
	private JCheckBox chckbxUseBinarySplits;
	private JCheckBox chckbxDontUseSubtree;
	private JCheckBox chckbxDontCleanUp;
	private JCheckBox chckbxLaplaceSmoothingFor;

	public J48Frame() {
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setPreferredSize(new Dimension(410, 275));
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);

		chckbxUseunpruned = new JCheckBox("Use unpruned tree.");
		springLayout.putConstraint(SpringLayout.NORTH, chckbxUseunpruned, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, chckbxUseunpruned, 10, SpringLayout.WEST, this);
		add(chckbxUseunpruned);

		lblPruningConfidenceThreshold = new JLabel("Pruning confidence threshold:");
		springLayout.putConstraint(SpringLayout.WEST, lblPruningConfidenceThreshold, 0, SpringLayout.WEST,
				chckbxUseunpruned);
		add(lblPruningConfidenceThreshold);

		pruningConfidenceTextField = new JTextField();
		pruningConfidenceTextField.setText("0.25");
		springLayout.putConstraint(SpringLayout.NORTH, pruningConfidenceTextField, 36, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, pruningConfidenceTextField, 68, SpringLayout.EAST,
				lblPruningConfidenceThreshold);
		springLayout.putConstraint(SpringLayout.NORTH, lblPruningConfidenceThreshold, 3, SpringLayout.NORTH,
				pruningConfidenceTextField);
		add(pruningConfidenceTextField);
		pruningConfidenceTextField.setColumns(10);

		lblMinNumberOf = new JLabel("Min. number of instances per leaf:");
		springLayout.putConstraint(SpringLayout.WEST, lblMinNumberOf, 0, SpringLayout.WEST, chckbxUseunpruned);
		add(lblMinNumberOf);

		instancesTextField = new JTextField();
		instancesTextField.setText("2");
		springLayout.putConstraint(SpringLayout.NORTH, lblMinNumberOf, 3, SpringLayout.NORTH, instancesTextField);
		springLayout.putConstraint(SpringLayout.NORTH, instancesTextField, 6, SpringLayout.SOUTH,
				pruningConfidenceTextField);
		springLayout.putConstraint(SpringLayout.EAST, instancesTextField, 0, SpringLayout.EAST,
				pruningConfidenceTextField);
		add(instancesTextField);
		instancesTextField.setColumns(10);

		chckbxUseReducedError = new JCheckBox("Use reduced error pruning.");
		springLayout.putConstraint(SpringLayout.NORTH, chckbxUseReducedError, 6, SpringLayout.SOUTH, lblMinNumberOf);
		springLayout.putConstraint(SpringLayout.WEST, chckbxUseReducedError, 0, SpringLayout.WEST, chckbxUseunpruned);
		add(chckbxUseReducedError);

		foldsTextField = new JTextField();
		foldsTextField.setText("3");
		springLayout.putConstraint(SpringLayout.NORTH, foldsTextField, 33, SpringLayout.SOUTH, instancesTextField);
		springLayout.putConstraint(SpringLayout.EAST, foldsTextField, 0, SpringLayout.EAST, pruningConfidenceTextField);
		add(foldsTextField);
		foldsTextField.setColumns(10);

		lblNumberOfFolds = new JLabel("Number of folds for reduced error pruning:");
		springLayout.putConstraint(SpringLayout.NORTH, lblNumberOfFolds, 3, SpringLayout.NORTH, foldsTextField);
		springLayout.putConstraint(SpringLayout.WEST, lblNumberOfFolds, 0, SpringLayout.WEST, chckbxUseunpruned);
		add(lblNumberOfFolds);

		chckbxUseBinarySplits = new JCheckBox("Use binary splits only.");
		springLayout.putConstraint(SpringLayout.NORTH, chckbxUseBinarySplits, 6, SpringLayout.SOUTH, lblNumberOfFolds);
		springLayout.putConstraint(SpringLayout.WEST, chckbxUseBinarySplits, 0, SpringLayout.WEST, chckbxUseunpruned);
		add(chckbxUseBinarySplits);

		chckbxDontUseSubtree = new JCheckBox("Don't use subtree raising.");
		springLayout.putConstraint(SpringLayout.NORTH, chckbxDontUseSubtree, 6, SpringLayout.SOUTH,
				chckbxUseBinarySplits);
		springLayout.putConstraint(SpringLayout.WEST, chckbxDontUseSubtree, 10, SpringLayout.WEST, this);
		add(chckbxDontUseSubtree);

		chckbxDontCleanUp = new JCheckBox("Don't clean up after build.");
		springLayout.putConstraint(SpringLayout.NORTH, chckbxDontCleanUp, 6, SpringLayout.SOUTH, chckbxDontUseSubtree);
		springLayout.putConstraint(SpringLayout.WEST, chckbxDontCleanUp, 0, SpringLayout.WEST, chckbxUseunpruned);
		add(chckbxDontCleanUp);

		chckbxLaplaceSmoothingFor = new JCheckBox("Laplace smoothing for predicted probabilities");
		springLayout.putConstraint(SpringLayout.NORTH, chckbxLaplaceSmoothingFor, 6, SpringLayout.SOUTH,
				chckbxDontCleanUp);
		springLayout.putConstraint(SpringLayout.WEST, chckbxLaplaceSmoothingFor, 0, SpringLayout.WEST,
				chckbxUseunpruned);
		add(chckbxLaplaceSmoothingFor);
	}

	@Override
	public Class<?> getConfiguredClass() {
		return J48.class;
	}

	@Override
	public String getName() {
		return "J48 Decision Tree";
	}

	@Override
	public boolean checkValues() {
		if (!UITools.checkTextFieldValueDouble(this, "Pruning confidence threshold", this.pruningConfidenceTextField,
				0, Double.MAX_VALUE)
				|| !UITools.checkTextFieldValueInt(this, "Instances per leaf", this.instancesTextField, 1,
						Integer.MAX_VALUE)
				|| !UITools.checkTextFieldValueInt(this, "Number of folds", this.foldsTextField, 1, Integer.MAX_VALUE)) {
			return false;
		}
		return true;
	}

	@Override
	public String[] getOptions() {
		String option = "";
		if (this.chckbxUseunpruned.isSelected()) {
			option += "-U ";
		}
		option += "-C " + this.pruningConfidenceTextField.getText() + " ";
		option += "-M " + this.instancesTextField.getText() + " ";
		if (this.chckbxUseReducedError.isSelected()) {
			option += "-R ";
			option += "-N " + this.foldsTextField.getText() + " ";
		}
		if (this.chckbxUseBinarySplits.isSelected()) {
			option += "-B ";
		}
		if (this.chckbxDontUseSubtree.isSelected()) {
			option += "-S ";
		}
		if (this.chckbxDontCleanUp.isSelected()) {
			option += "-L ";
		}
		if (this.chckbxLaplaceSmoothingFor.isSelected()) {
			option += "-A ";
		}
		return new String[] { option };
	}

	@Override
	public void setOptions(String[] options) {
		this.chckbxLaplaceSmoothingFor.setSelected(false);
		this.chckbxDontCleanUp.setSelected(false);
		this.chckbxDontUseSubtree.setSelected(false);
		this.chckbxUseBinarySplits.setSelected(false);
		this.chckbxUseReducedError.setSelected(false);
		this.chckbxUseunpruned.setSelected(false);
		if(options.length == 0) {
			return;
		}
		String[] opts = options[0].split(" ");
		for (int i = 0; i < opts.length; i++) {
			String o = opts[i];
			if (o.equals("-U")) {
				this.chckbxUseunpruned.setSelected(true);
			}
			if (o.equals("-R")) {
				this.chckbxUseReducedError.setSelected(true);
			}
			if (o.equals("-B")) {
				this.chckbxUseBinarySplits.setSelected(true);
			}
			if (o.equals("-S")) {
				this.chckbxDontUseSubtree.setSelected(true);
			}
			if (o.equals("-L")) {
				this.chckbxDontCleanUp.setSelected(true);
			}
			if (o.equals("-A")) {
				this.chckbxLaplaceSmoothingFor.setSelected(true);
			}
			if (o.equals("-C")) {
				this.pruningConfidenceTextField.setText(opts[i + 1]);
				i++;
			}
			if (o.equals("-M")) {
				this.instancesTextField.setText(opts[i + 1]);
				i++;
			}
			if (o.equals("-N")) {
				this.foldsTextField.setText(opts[i + 1]);
				i++;
			}
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

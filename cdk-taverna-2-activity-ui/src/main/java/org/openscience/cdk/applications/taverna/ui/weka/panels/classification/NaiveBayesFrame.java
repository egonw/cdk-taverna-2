package org.openscience.cdk.applications.taverna.ui.weka.panels.classification;

import java.awt.Dimension;

import javax.swing.JCheckBox;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;

import org.openscience.cdk.applications.taverna.ui.weka.panels.AbstractClassificationConfigurationFrame;

import weka.classifiers.bayes.NaiveBayes;

public class NaiveBayesFrame extends AbstractClassificationConfigurationFrame {

	private static final long serialVersionUID = 2952889742862925882L;
	
	private JCheckBox chckbxUseKernelDensity;
	private JCheckBox chckbxSupervised;

	public NaiveBayesFrame() {
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setPreferredSize(new Dimension(410, 275));
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		chckbxUseKernelDensity = new JCheckBox(
				"Use kernel density istead of normal distribution for numeric attributes.");
		springLayout.putConstraint(SpringLayout.NORTH, chckbxUseKernelDensity, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, chckbxUseKernelDensity, 10, SpringLayout.WEST, this);
		add(chckbxUseKernelDensity);

		chckbxSupervised = new JCheckBox("Use supervised discretization to process numeric attributes.\r\n");
		springLayout
				.putConstraint(SpringLayout.NORTH, chckbxSupervised, 16, SpringLayout.SOUTH, chckbxUseKernelDensity);
		springLayout.putConstraint(SpringLayout.WEST, chckbxSupervised, 0, SpringLayout.WEST, chckbxUseKernelDensity);
		add(chckbxSupervised);
	}

	@Override
	public Class<?> getConfiguredClass() {
		return NaiveBayes.class;
	}

	@Override
	public String getName() {
		return "Naive Bayes";
	}

	@Override
	public boolean checkValues() {
		return true;
	}

	@Override
	public String[] getOptions() {
		String option = "";
		if (this.chckbxUseKernelDensity.isSelected()) {
			option += "-K ";
		}
		if (this.chckbxSupervised.isSelected()) {
			option += "-D ";
		}
		return new String[] { option };
	}

	@Override
	public void setOptions(String[] options) {
		this.chckbxUseKernelDensity.setSelected(false);
		this.chckbxSupervised.setSelected(false);
		if (options.length == 0) {
			return;
		}
		String[] opt = options[0].split(" ");
		for (int i = 0; i < opt.length; i++) {
			String o = opt[i];
			if (o.equals("-K")) {
				this.chckbxUseKernelDensity.setSelected(true);
			}
			if (o.equals("-D")) {
				this.chckbxSupervised.setSelected(true);
			}
		}
	}

	@Override
	public void makeSingleOption() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean useThreading() {
		return true;
	}
}

package org.openscience.cdk.applications.taverna.ui.weka.panels.classification;

import java.awt.Dimension;

import org.openscience.cdk.applications.taverna.ui.weka.panels.AbstractClassificationConfigurationFrame;

import weka.classifiers.trees.J48;

public class J48Frame extends AbstractClassificationConfigurationFrame {
	public J48Frame() {
		setPreferredSize(new Dimension(410, 275));
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
		return true;
	}

	@Override
	public String[] getOptions() {
		return new String[] {""};
	}

	@Override
	public void setOptions(String[] options) {
		// TODO Auto-generated method stub

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

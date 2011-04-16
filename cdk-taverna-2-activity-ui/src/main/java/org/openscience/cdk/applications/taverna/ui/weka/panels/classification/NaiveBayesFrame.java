package org.openscience.cdk.applications.taverna.ui.weka.panels.classification;

import org.openscience.cdk.applications.taverna.ui.weka.panels.AbstractClassificationConfigurationFrame;

import weka.classifiers.bayes.NaiveBayes;

public class NaiveBayesFrame extends AbstractClassificationConfigurationFrame {

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

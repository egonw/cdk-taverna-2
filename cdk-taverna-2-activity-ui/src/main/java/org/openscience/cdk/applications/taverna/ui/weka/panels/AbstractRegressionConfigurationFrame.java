package org.openscience.cdk.applications.taverna.ui.weka.panels;

import javax.swing.JPanel;

public abstract class AbstractRegressionConfigurationFrame extends JPanel {

	private static final long serialVersionUID = -7206928107825263602L;

	/**
	 * @return The weka class which is configured( e.g. MultilayerPerceptron)
	 */
	public abstract Class<?> getConfiguredClass();

	public abstract String getName();

	/**
	 * Checks whether the input values are correct.
	 * 
	 * @return True if the values are correct.
	 */
	public abstract boolean checkValues();

	public abstract String[] getOptions();
	
	public abstract void setOptions(String[] options);
	
	public abstract void makeSingleOption();
	
	public abstract boolean useThreading();
}

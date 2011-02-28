package org.openscience.cdk.applications.taverna.ui.weka.panels;

import javax.swing.JPanel;

public abstract class AbstractLearningConfigurationFrame extends JPanel {

	/**
	 * @return The weka class which is configured( e.g. MultilayerPerceptron)
	 */
	public abstract Class<?> getConfiguredClass();
	public abstract boolean isConfigurationChanged();
	 public abstract String getName();
		/**
		 * Checks whether the input values are correct.
		 * 
		 * @return True if the values are correct.
		 */
		public abstract boolean checkValues();
	 public abstract String[] getOptions();
}

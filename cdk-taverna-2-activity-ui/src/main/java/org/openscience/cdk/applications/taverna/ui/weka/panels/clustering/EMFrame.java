package org.openscience.cdk.applications.taverna.ui.weka.panels.clustering;

import org.openscience.cdk.applications.taverna.ui.weka.panels.AbstractConfigurationFrame;

import weka.clusterers.Clusterer;
import weka.clusterers.EM;
import weka.clusterers.SimpleKMeans;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.SpringLayout;
import javax.swing.JTextField;
import java.awt.Dimension;

public class EMFrame extends AbstractConfigurationFrame {

	private static final long serialVersionUID = 3225727167693843163L;
	private final JLabel numberOfClustersLabel = new JLabel("Number of clusters:");
	private final JTextField numberOfClustersTextField = new JTextField();
	private final JTextField numberOfIterationsTextField = new JTextField();
	private final JLabel lblNumberOfIterations = new JLabel("Number of iterations:");

	public EMFrame() {
		numberOfIterationsTextField.setText("100");
		numberOfIterationsTextField.setColumns(10);
		setSize(new Dimension(350, 130));
		numberOfClustersTextField.setText("5");
		numberOfClustersTextField.setColumns(10);

		JPanel configurationPanel = new JPanel();
		getContentPane().add(configurationPanel, BorderLayout.CENTER);
		SpringLayout sl_configurationPanel = new SpringLayout();
		sl_configurationPanel.putConstraint(SpringLayout.WEST, lblNumberOfIterations, 10, SpringLayout.WEST, configurationPanel);
		sl_configurationPanel
				.putConstraint(SpringLayout.WEST, numberOfClustersLabel, 0, SpringLayout.WEST, lblNumberOfIterations);
		sl_configurationPanel.putConstraint(SpringLayout.SOUTH, numberOfClustersLabel, 0, SpringLayout.SOUTH,
				numberOfClustersTextField);
		sl_configurationPanel.putConstraint(SpringLayout.EAST, numberOfClustersLabel, 185, SpringLayout.WEST, configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.SOUTH, lblNumberOfIterations, 0, SpringLayout.SOUTH,
				numberOfIterationsTextField);
		sl_configurationPanel.putConstraint(SpringLayout.NORTH, numberOfIterationsTextField, 6, SpringLayout.SOUTH,
				numberOfClustersTextField);
		sl_configurationPanel.putConstraint(SpringLayout.EAST, numberOfIterationsTextField, 0, SpringLayout.EAST,
				numberOfClustersTextField);
		sl_configurationPanel.putConstraint(SpringLayout.NORTH, numberOfClustersTextField, 5, SpringLayout.NORTH,
				configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.EAST, numberOfClustersTextField, -10, SpringLayout.EAST,
				configurationPanel);
		configurationPanel.setLayout(sl_configurationPanel);
		{
			configurationPanel.add(numberOfClustersLabel);
		}
		{
			configurationPanel.add(numberOfClustersTextField);
		}
		{
			configurationPanel.add(numberOfIterationsTextField);
		}
		{
			configurationPanel.add(lblNumberOfIterations);
		}
	}

	@Override
	public Class<? extends Clusterer> getConfiguratedClass() {
		return EM.class;
	}

	@Override
	public String[] getOptions() {
		String[] options = new String[4];
		options[0] = "-N";
		options[1] = this.numberOfClustersTextField.getText();
		options[2] = "-I";
		options[3] = this.numberOfIterationsTextField.getText();
		return options;
	}

	@Override
	public String getName() {
		return "EM (Expectation Maximisation)";
	}

	@Override
	public boolean checkValues() {
		if (this.checkTextFieldValue("Number of clusters", this.numberOfClustersTextField, 1, Integer.MAX_VALUE)
				&& this.checkTextFieldValue("Number of iterations", this.numberOfIterationsTextField, 1, Integer.MAX_VALUE)) {
			return true;
		}
		return false;
	}
}

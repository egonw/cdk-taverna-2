package org.openscience.cdk.applications.taverna.ui.weka.panels.clustering;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.openscience.cdk.applications.taverna.ui.weka.panels.AbstractConfigurationFrame;

import weka.clusterers.Clusterer;
import weka.clusterers.HierarchicalClusterer;

public class HierarchicalClustererFrame extends AbstractConfigurationFrame {

	private static final long serialVersionUID = 3225727167693843163L;
	private static final String[] LINK_TYPES = { "SINGLE", "COMPLETE", "AVERAGE", "MEAN", "CENTROID", "WARD", "ADJCOMLPETE",
			"NEIGHBOR_JOINING" };
	private final JLabel numberOfClustersLabel = new JLabel("Number of clusters:");
	private final JTextField numberOfClustersTextField = new JTextField();
	private final JLabel lblLinkType = new JLabel("Link Type:");
	private final JComboBox comboBox = new JComboBox(LINK_TYPES);

	public HierarchicalClustererFrame() {
		setSize(new Dimension(350, 134));
		numberOfClustersTextField.setText("5");
		numberOfClustersTextField.setColumns(10);

		JPanel configurationPanel = new JPanel();
		getContentPane().add(configurationPanel, BorderLayout.CENTER);
		SpringLayout sl_configurationPanel = new SpringLayout();
		sl_configurationPanel.putConstraint(SpringLayout.WEST, lblLinkType, 0, SpringLayout.WEST, numberOfClustersLabel);
		sl_configurationPanel.putConstraint(SpringLayout.SOUTH, lblLinkType, 0, SpringLayout.SOUTH, comboBox);
		sl_configurationPanel.putConstraint(SpringLayout.NORTH, comboBox, 6, SpringLayout.SOUTH, numberOfClustersLabel);
		sl_configurationPanel.putConstraint(SpringLayout.WEST, comboBox, -175, SpringLayout.EAST, configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.EAST, comboBox, -10, SpringLayout.EAST, configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.WEST, numberOfClustersLabel, 10, SpringLayout.WEST, configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.EAST, numberOfClustersLabel, -53, SpringLayout.WEST,
				numberOfClustersTextField);
		sl_configurationPanel.putConstraint(SpringLayout.SOUTH, numberOfClustersLabel, 0, SpringLayout.SOUTH,
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
			configurationPanel.add(lblLinkType);
		}
		{
			configurationPanel.add(comboBox);
		}
	}

	@Override
	public Class<? extends Clusterer> getConfiguratedClass() {
		return HierarchicalClusterer.class;
	}

	@Override
	public String[] getOptions() {
		String[] options = new String[4];
		options[0] = "-N";
		options[1] = this.numberOfClustersTextField.getText();
		options[2] = "-L";
		options[3] = LINK_TYPES[comboBox.getSelectedIndex()];
		return options;
	}

	@Override
	public String getName() {
		return "Hierarchical Clusterer";
	}

	@Override
	public boolean checkValues() {
		if (this.checkTextFieldValueInt("Number of clusters", this.numberOfClustersTextField, 1, Integer.MAX_VALUE)) {
			return true;
		}
		return false;
	}
}

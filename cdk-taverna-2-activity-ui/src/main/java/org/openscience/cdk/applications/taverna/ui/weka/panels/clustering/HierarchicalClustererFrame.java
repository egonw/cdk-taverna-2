/*
 * Copyright (C) 2010-2011 by Andreas Truszkowski <ATruszkowski@gmx.de>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.openscience.cdk.applications.taverna.ui.weka.panels.clustering;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import org.openscience.cdk.applications.taverna.ui.UITools;
import org.openscience.cdk.applications.taverna.ui.weka.WekaClusteringConfigurationPanelController;
import org.openscience.cdk.applications.taverna.ui.weka.panels.AbstractClusteringConfigurationFrame;

import weka.clusterers.Clusterer;
import weka.clusterers.HierarchicalClusterer;

/**
 * Hierarchical clusterer configuration frame.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class HierarchicalClustererFrame extends AbstractClusteringConfigurationFrame {

	private static final long serialVersionUID = 3225727167693843163L;
	private static final String[] LINK_TYPES = { "SINGLE", "COMPLETE", "AVERAGE", "MEAN", "CENTROID", "WARD", "ADJCOMLPETE",
			"NEIGHBOR_JOINING" };
	private final JLabel numberOfClustersLabel = new JLabel("Number of clusters:");
	private final JTextField minNumberOfClustersTextField = new JTextField();
	private final JLabel lblLinkType = new JLabel("Link Type:");
	private final JComboBox comboBox = new JComboBox(LINK_TYPES);
	private final JTextField maxNumberOfClustersTextField = new JTextField();
	private final JLabel label = new JLabel("-");

	public HierarchicalClustererFrame() {
		maxNumberOfClustersTextField.setText("10");
		maxNumberOfClustersTextField.setColumns(10);
		setSize(new Dimension(350, 134));
		minNumberOfClustersTextField.setText("2");
		minNumberOfClustersTextField.setColumns(10);

		JPanel configurationPanel = new JPanel();
		getContentPane().add(configurationPanel, BorderLayout.CENTER);
		SpringLayout sl_configurationPanel = new SpringLayout();
		sl_configurationPanel.putConstraint(SpringLayout.NORTH, label, 8, SpringLayout.NORTH, configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.WEST, label, 0, SpringLayout.EAST, minNumberOfClustersTextField);
		sl_configurationPanel.putConstraint(SpringLayout.EAST, label, 0, SpringLayout.WEST, maxNumberOfClustersTextField);
		sl_configurationPanel.putConstraint(SpringLayout.NORTH, maxNumberOfClustersTextField, 5, SpringLayout.NORTH,
				configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.WEST, maxNumberOfClustersTextField, 22, SpringLayout.EAST,
				minNumberOfClustersTextField);
		sl_configurationPanel.putConstraint(SpringLayout.EAST, maxNumberOfClustersTextField, -10, SpringLayout.EAST,
				configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.EAST, minNumberOfClustersTextField, -64, SpringLayout.EAST,
				configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.NORTH, comboBox, 31, SpringLayout.NORTH, configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.WEST, numberOfClustersLabel, 10, SpringLayout.WEST, configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.SOUTH, numberOfClustersLabel, -6, SpringLayout.NORTH, comboBox);
		sl_configurationPanel.putConstraint(SpringLayout.EAST, numberOfClustersLabel, -53, SpringLayout.WEST,
				minNumberOfClustersTextField);
		sl_configurationPanel.putConstraint(SpringLayout.WEST, minNumberOfClustersTextField, -96, SpringLayout.EAST,
				configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.WEST, lblLinkType, 0, SpringLayout.WEST, numberOfClustersLabel);
		sl_configurationPanel.putConstraint(SpringLayout.SOUTH, lblLinkType, 0, SpringLayout.SOUTH, comboBox);
		sl_configurationPanel.putConstraint(SpringLayout.WEST, comboBox, -175, SpringLayout.EAST, configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.EAST, comboBox, -10, SpringLayout.EAST, configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.NORTH, minNumberOfClustersTextField, 5, SpringLayout.NORTH,
				configurationPanel);
		configurationPanel.setLayout(sl_configurationPanel);
		{
			configurationPanel.add(numberOfClustersLabel);
		}
		{
			configurationPanel.add(minNumberOfClustersTextField);
		}
		{
			configurationPanel.add(lblLinkType);
		}
		{
			configurationPanel.add(comboBox);
		}
		{
			configurationPanel.add(maxNumberOfClustersTextField);
		}
		{
			label.setHorizontalAlignment(SwingConstants.CENTER);
			configurationPanel.add(label);
		}
	}

	@Override
	public Class<? extends Clusterer> getConfiguredClass() {
		return HierarchicalClusterer.class;
	}

	@Override
	public String[][] getOptions() {
		int min = Integer.parseInt(this.minNumberOfClustersTextField.getText());
		int max = Integer.parseInt(this.maxNumberOfClustersTextField.getText());
		int numberOfJobs = max - min + 1;
		int id = WekaClusteringConfigurationPanelController.getJobID();
		String[][] options = new String[numberOfJobs][];
		for (int i = 0; i < numberOfJobs; i++) {
			options[i] = new String[6];
			options[i][0] = "-N";
			options[i][1] = "" + (min + i);
			options[i][2] = "-L";
			options[i][3] = LINK_TYPES[comboBox.getSelectedIndex()];
			options[i][4] = "-ID";
			options[i][5] = "" + id;
		}
		return options;
	}

	@Override
	public String getName() {
		return "Hierarchical Clusterer";
	}

	@Override
	public boolean checkValues() {
		if (!UITools.checkTextFieldValueInt(this, "Min number of clusters", this.minNumberOfClustersTextField, 2, Integer.MAX_VALUE)
				|| !UITools.checkTextFieldValueInt(this,"Max number of clusters", this.maxNumberOfClustersTextField, 2, Integer.MAX_VALUE)) {
			return false;
		}
		int min = Integer.parseInt(this.minNumberOfClustersTextField.getText());
		int max = Integer.parseInt(this.maxNumberOfClustersTextField.getText());
		if (max < min) {
			JOptionPane.showMessageDialog(this, "Max number of clusters has to be greater or equal than the min number!",
					"Illegal Argument", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
}

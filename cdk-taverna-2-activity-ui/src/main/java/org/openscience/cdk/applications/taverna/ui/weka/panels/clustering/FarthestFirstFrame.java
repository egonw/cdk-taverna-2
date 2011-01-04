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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.openscience.cdk.applications.taverna.ui.weka.panels.AbstractConfigurationFrame;

import weka.clusterers.Clusterer;
import weka.clusterers.FarthestFirst;

/**
 * Farthest First clusterer configuration frame.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class FarthestFirstFrame extends AbstractConfigurationFrame {

	private static final long serialVersionUID = 3225727167693843163L;
	private final JLabel numberOfClustersLabel = new JLabel("Number of clusters:");
	private final JTextField numberOfClustersTextField = new JTextField();

	public FarthestFirstFrame() {
		setSize(new Dimension(350, 105));
		numberOfClustersTextField.setText("5");
		numberOfClustersTextField.setColumns(10);

		JPanel configurationPanel = new JPanel();
		getContentPane().add(configurationPanel, BorderLayout.CENTER);
		SpringLayout sl_configurationPanel = new SpringLayout();
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
	}

	@Override
	public Class<? extends Clusterer> getConfiguredClass() {
		return FarthestFirst.class;
	}

	@Override
	public String[] getOptions() {
		String[] options = new String[2];
		options[0] = "-N";
		options[1] = this.numberOfClustersTextField.getText();
		return options;
	}

	@Override
	public String getName() {
		return "Farthest First";
	}

	@Override
	public boolean checkValues() {
		if (this.checkTextFieldValueInt("Number of clusters", this.numberOfClustersTextField, 1, Integer.MAX_VALUE)) {
			return true;
		}
		return false;
	}
}

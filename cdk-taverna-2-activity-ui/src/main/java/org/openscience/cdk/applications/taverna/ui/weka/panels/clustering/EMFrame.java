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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.openscience.cdk.applications.taverna.ui.UITools;
import org.openscience.cdk.applications.taverna.ui.weka.WekaClusteringConfigurationPanelController;
import org.openscience.cdk.applications.taverna.ui.weka.panels.AbstractClusteringConfigurationFrame;

import weka.clusterers.Clusterer;
import weka.clusterers.EM;

/**
 * EM clusterer configuration frame.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class EMFrame extends AbstractClusteringConfigurationFrame {

	private static final long serialVersionUID = 3225727167693843163L;
	private final JLabel numberOfClustersLabel = new JLabel(" Number of clusters:");
	private final JTextField minNumberOfClustersTextField = new JTextField();
	private final JTextField numberOfIterationsTextField = new JTextField();
	private final JLabel lblNumberOfIterations = new JLabel(" Number of iterations:");
	private final JTextPane txtpnMinimumAllowableStandard = new JTextPane();
	private final JTextField standardDeviationTextField = new JTextField();
	private final JTextField maxNumberOfClustersTextField = new JTextField();
	private final JLabel label = new JLabel("-");
	private JLabel lblForFree;

	public EMFrame() {
		maxNumberOfClustersTextField.setText("10");
		maxNumberOfClustersTextField.setColumns(10);
		standardDeviationTextField.setText("0.00001");
		standardDeviationTextField.setColumns(10);
		numberOfIterationsTextField.setText("100");
		numberOfIterationsTextField.setColumns(10);
		setSize(new Dimension(350, 200));
		minNumberOfClustersTextField.setText("2");
		minNumberOfClustersTextField.setColumns(10);

		JPanel configurationPanel = new JPanel();
		getContentPane().add(configurationPanel, BorderLayout.CENTER);
		SpringLayout sl_configurationPanel = new SpringLayout();
		sl_configurationPanel.putConstraint(SpringLayout.WEST, standardDeviationTextField, 0, SpringLayout.WEST,
				minNumberOfClustersTextField);
		sl_configurationPanel.putConstraint(SpringLayout.SOUTH, standardDeviationTextField, 0, SpringLayout.SOUTH,
				txtpnMinimumAllowableStandard);
		sl_configurationPanel.putConstraint(SpringLayout.NORTH, numberOfIterationsTextField, -3, SpringLayout.NORTH,
				lblNumberOfIterations);
		sl_configurationPanel.putConstraint(SpringLayout.WEST, numberOfIterationsTextField, 0, SpringLayout.WEST,
				minNumberOfClustersTextField);
		sl_configurationPanel.putConstraint(SpringLayout.NORTH, txtpnMinimumAllowableStandard, 6, SpringLayout.SOUTH,
				lblNumberOfIterations);
		sl_configurationPanel.putConstraint(SpringLayout.WEST, txtpnMinimumAllowableStandard, 0, SpringLayout.WEST,
				numberOfClustersLabel);
		sl_configurationPanel.putConstraint(SpringLayout.SOUTH, txtpnMinimumAllowableStandard, -19, SpringLayout.SOUTH,
				configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.EAST, txtpnMinimumAllowableStandard, 220, SpringLayout.WEST,
				configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.WEST, lblNumberOfIterations, 0, SpringLayout.WEST,
				numberOfClustersLabel);
		sl_configurationPanel.putConstraint(SpringLayout.WEST, numberOfClustersLabel, 10, SpringLayout.WEST,
				configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.EAST, numberOfClustersLabel, -53, SpringLayout.WEST,
				minNumberOfClustersTextField);
		sl_configurationPanel.putConstraint(SpringLayout.NORTH, label, 8, SpringLayout.NORTH, configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.WEST, label, 0, SpringLayout.EAST,
				minNumberOfClustersTextField);
		sl_configurationPanel.putConstraint(SpringLayout.EAST, label, 0, SpringLayout.WEST,
				maxNumberOfClustersTextField);
		sl_configurationPanel.putConstraint(SpringLayout.WEST, maxNumberOfClustersTextField, 22, SpringLayout.EAST,
				minNumberOfClustersTextField);
		sl_configurationPanel.putConstraint(SpringLayout.EAST, minNumberOfClustersTextField, -64, SpringLayout.EAST,
				configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.NORTH, maxNumberOfClustersTextField, 0, SpringLayout.NORTH,
				minNumberOfClustersTextField);
		sl_configurationPanel.putConstraint(SpringLayout.EAST, maxNumberOfClustersTextField, -10, SpringLayout.EAST,
				configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.NORTH, numberOfClustersLabel, 11, SpringLayout.NORTH,
				configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.WEST, minNumberOfClustersTextField, -96, SpringLayout.EAST,
				configurationPanel);
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
			configurationPanel.add(numberOfIterationsTextField);
		}
		{
			configurationPanel.add(lblNumberOfIterations);
		}
		{
			txtpnMinimumAllowableStandard.setEditable(false);
			txtpnMinimumAllowableStandard.setBackground(UIManager.getColor("Label.background"));
			txtpnMinimumAllowableStandard.setText("Minimum allowable standard deviation for normal density:");
			configurationPanel.add(txtpnMinimumAllowableStandard);
		}
		{
			configurationPanel.add(standardDeviationTextField);
		}
		{
			configurationPanel.add(maxNumberOfClustersTextField);
		}
		{
			label.setHorizontalAlignment(SwingConstants.CENTER);
			configurationPanel.add(label);
		}

		lblForFree = new JLabel("-1 for free clustering.");
		sl_configurationPanel.putConstraint(SpringLayout.NORTH, lblNumberOfIterations, 6, SpringLayout.SOUTH,
				lblForFree);
		lblForFree.setEnabled(false);
		sl_configurationPanel.putConstraint(SpringLayout.NORTH, lblForFree, 6, SpringLayout.SOUTH,
				numberOfClustersLabel);
		sl_configurationPanel
				.putConstraint(SpringLayout.WEST, lblForFree, 10, SpringLayout.WEST, numberOfClustersLabel);
		configurationPanel.add(lblForFree);
	}

	@Override
	public Class<? extends Clusterer> getConfiguredClass() {
		return EM.class;
	}

	@Override
	public String[][] getOptions() {
		String[][] options = null;
		int id = WekaClusteringConfigurationPanelController.getJobID();
		if (this.minNumberOfClustersTextField.getText().equals("-1")) {
			options = new String[1][];
			options[0] = new String[6];
			options[0][0] = "-I";
			options[0][1] = this.numberOfIterationsTextField.getText();
			options[0][2] = "-M";
			options[0][3] = this.standardDeviationTextField.getText();
			options[0][4] = "-ID";
			options[0][5] = "" + id;
		} else {
			int min = Integer.parseInt(this.minNumberOfClustersTextField.getText());
			int max = Integer.parseInt(this.maxNumberOfClustersTextField.getText());
			int numberOfJobs = max - min + 1;
			options = new String[numberOfJobs][];
			for (int i = 0; i < numberOfJobs; i++) {
				options[i] = new String[8];
				options[i][0] = "-N";
				options[i][1] = "" + (min + i);
				options[i][2] = "-I";
				options[i][3] = this.numberOfIterationsTextField.getText();
				options[i][4] = "-M";
				options[i][5] = this.standardDeviationTextField.getText();
				options[i][6] = "-ID";
				options[i][7] = "" + id;
			}
		}
		return options;
	}

	@Override
	public String getName() {
		return "EM (Expectation Maximisation)";
	}

	@Override
	public boolean checkValues() {
		if (!this.minNumberOfClustersTextField.getText().equals("-1")) {
			if (!UITools.checkTextFieldValueInt(this, "Min number of clusters", this.minNumberOfClustersTextField, 2,
					Integer.MAX_VALUE)
					|| !UITools.checkTextFieldValueInt(this, "Max number of clusters",
							this.maxNumberOfClustersTextField, 2, Integer.MAX_VALUE)) {
				return false;
			}
		}
		if (!UITools.checkTextFieldValueInt(this, "Number of iterations", this.numberOfIterationsTextField, 1,
				Integer.MAX_VALUE)
				|| !UITools.checkTextFieldValueDouble(this, "Minimum allowable standard deviation for normal density",
						this.standardDeviationTextField, 0, Integer.MAX_VALUE)) {
			return false;
		}
		if (!this.minNumberOfClustersTextField.getText().equals("-1")) {
			int min = Integer.parseInt(this.minNumberOfClustersTextField.getText());
			int max = Integer.parseInt(this.maxNumberOfClustersTextField.getText());
			if (max < min) {
				JOptionPane.showMessageDialog(this,
						"Max number of clusters has to be greater or equal than the min number!", "Illegal Argument",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
	}
}

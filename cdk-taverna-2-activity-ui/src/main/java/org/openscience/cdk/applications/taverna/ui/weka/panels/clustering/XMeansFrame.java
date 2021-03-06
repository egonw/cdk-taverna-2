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
import weka.clusterers.XMeans;

/**
 * XMeans clusterer configuration frame.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class XMeansFrame extends AbstractClusteringConfigurationFrame {

	private static final long serialVersionUID = 3225727167693843163L;
	private final JTextField iterationsOverallTextField = new JTextField();
	private final JTextField iterationsImproveParametersTextField = new JTextField();
	private final JTextPane txtpnMaximumNumberOf = new JTextPane();
	private final JTextPane txtpnMaximumImprove = new JTextPane();
	private final JTextPane txtpnMaximumNumberOf_1 = new JTextPane();
	private final JTextField iterationsImproveStrucureTextField = new JTextField();
	private final JLabel lblNumberOfClusters = new JLabel(" Number of Clusters:");
	private final JTextField minNumberOfClustersTextField = new JTextField();
	private final JTextField maxNumberOfClustersTextField = new JTextField();
	private final JLabel label = new JLabel("-");

	public XMeansFrame() {
		maxNumberOfClustersTextField.setText("10");
		maxNumberOfClustersTextField.setColumns(10);
		minNumberOfClustersTextField.setText("2");
		minNumberOfClustersTextField.setColumns(10);
		iterationsImproveStrucureTextField.setText("1000");
		iterationsImproveStrucureTextField.setColumns(10);
		iterationsImproveParametersTextField.setText("1000");
		iterationsImproveParametersTextField.setColumns(10);
		setSize(new Dimension(350, 226));
		iterationsOverallTextField.setText("1");
		iterationsOverallTextField.setColumns(10);

		JPanel configurationPanel = new JPanel();
		getContentPane().add(configurationPanel, BorderLayout.CENTER);
		SpringLayout sl_configurationPanel = new SpringLayout();
		sl_configurationPanel.putConstraint(SpringLayout.NORTH, label, 9, SpringLayout.SOUTH,
				iterationsImproveStrucureTextField);
		sl_configurationPanel.putConstraint(SpringLayout.WEST, label, 0, SpringLayout.EAST,
				minNumberOfClustersTextField);
		sl_configurationPanel.putConstraint(SpringLayout.EAST, label, 0, SpringLayout.WEST,
				maxNumberOfClustersTextField);
		sl_configurationPanel.putConstraint(SpringLayout.NORTH, maxNumberOfClustersTextField, 6, SpringLayout.SOUTH,
				iterationsImproveStrucureTextField);
		sl_configurationPanel.putConstraint(SpringLayout.WEST, maxNumberOfClustersTextField, 22, SpringLayout.EAST,
				minNumberOfClustersTextField);
		sl_configurationPanel.putConstraint(SpringLayout.EAST, maxNumberOfClustersTextField, -10, SpringLayout.EAST,
				configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.NORTH, lblNumberOfClusters, 3, SpringLayout.NORTH,
				minNumberOfClustersTextField);
		sl_configurationPanel.putConstraint(SpringLayout.WEST, lblNumberOfClusters, 0, SpringLayout.WEST,
				txtpnMaximumNumberOf);
		sl_configurationPanel.putConstraint(SpringLayout.NORTH, minNumberOfClustersTextField, 6, SpringLayout.SOUTH,
				iterationsImproveStrucureTextField);
		sl_configurationPanel.putConstraint(SpringLayout.WEST, minNumberOfClustersTextField, -96, SpringLayout.EAST,
				configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.EAST, minNumberOfClustersTextField, -64, SpringLayout.EAST,
				configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.NORTH, iterationsImproveStrucureTextField, 34,
				SpringLayout.SOUTH, iterationsImproveParametersTextField);
		sl_configurationPanel.putConstraint(SpringLayout.NORTH, txtpnMaximumNumberOf_1, 6, SpringLayout.SOUTH,
				txtpnMaximumImprove);
		sl_configurationPanel.putConstraint(SpringLayout.WEST, txtpnMaximumNumberOf_1, 0, SpringLayout.WEST,
				txtpnMaximumNumberOf);
		sl_configurationPanel.putConstraint(SpringLayout.SOUTH, txtpnMaximumNumberOf_1, 0, SpringLayout.SOUTH,
				iterationsImproveStrucureTextField);
		sl_configurationPanel.putConstraint(SpringLayout.EAST, txtpnMaximumNumberOf_1, -6, SpringLayout.WEST,
				iterationsImproveStrucureTextField);
		sl_configurationPanel.putConstraint(SpringLayout.WEST, iterationsImproveStrucureTextField, 0,
				SpringLayout.WEST, iterationsOverallTextField);
		sl_configurationPanel.putConstraint(SpringLayout.WEST, txtpnMaximumImprove, 10, SpringLayout.WEST,
				configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.EAST, txtpnMaximumImprove, -6, SpringLayout.WEST,
				iterationsImproveParametersTextField);
		sl_configurationPanel.putConstraint(SpringLayout.WEST, iterationsImproveParametersTextField, 0,
				SpringLayout.WEST, iterationsOverallTextField);
		sl_configurationPanel.putConstraint(SpringLayout.SOUTH, iterationsImproveParametersTextField, 0,
				SpringLayout.SOUTH, txtpnMaximumImprove);
		sl_configurationPanel.putConstraint(SpringLayout.NORTH, txtpnMaximumImprove, 6, SpringLayout.SOUTH,
				txtpnMaximumNumberOf);
		sl_configurationPanel.putConstraint(SpringLayout.SOUTH, txtpnMaximumImprove, 40, SpringLayout.SOUTH,
				txtpnMaximumNumberOf);
		sl_configurationPanel.putConstraint(SpringLayout.WEST, txtpnMaximumNumberOf, 10, SpringLayout.WEST,
				configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.EAST, txtpnMaximumNumberOf, -6, SpringLayout.WEST,
				iterationsOverallTextField);
		sl_configurationPanel.putConstraint(SpringLayout.NORTH, txtpnMaximumNumberOf, 5, SpringLayout.NORTH,
				configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.NORTH, iterationsOverallTextField, 5, SpringLayout.NORTH,
				configurationPanel);
		sl_configurationPanel.putConstraint(SpringLayout.EAST, iterationsOverallTextField, -10, SpringLayout.EAST,
				configurationPanel);
		configurationPanel.setLayout(sl_configurationPanel);
		{
			configurationPanel.add(iterationsOverallTextField);
		}
		{
			configurationPanel.add(iterationsImproveParametersTextField);
		}
		{
			txtpnMaximumNumberOf.setEditable(false);
			txtpnMaximumNumberOf.setBackground(UIManager.getColor("Label.background"));
			txtpnMaximumNumberOf.setText("Maximum number of overall iterations:");
			configurationPanel.add(txtpnMaximumNumberOf);
		}
		{
			txtpnMaximumImprove.setEditable(false);
			txtpnMaximumImprove.setBackground(UIManager.getColor("Label.background"));
			txtpnMaximumImprove
					.setText("Maximum number of iterations in the kMeans loop in the Improve-Parameter part:");
			configurationPanel.add(txtpnMaximumImprove);
		}
		{
			txtpnMaximumNumberOf_1.setEditable(false);
			txtpnMaximumNumberOf_1.setBackground(UIManager.getColor("Label.background"));
			txtpnMaximumNumberOf_1
					.setText("Maximum number of iterations in the kMeans loop for the splitted centroids in the Improve-Structure part:");
			configurationPanel.add(txtpnMaximumNumberOf_1);
		}
		{
			configurationPanel.add(iterationsImproveStrucureTextField);
		}

		configurationPanel.add(lblNumberOfClusters);

		configurationPanel.add(minNumberOfClustersTextField);

		configurationPanel.add(maxNumberOfClustersTextField);
		label.setHorizontalAlignment(SwingConstants.CENTER);

		configurationPanel.add(label);
	}

	@Override
	public Class<? extends Clusterer> getConfiguredClass() {
		return XMeans.class;
	}

	@Override
	public String[][] getOptions() {
		int min = Integer.parseInt(this.minNumberOfClustersTextField.getText());
		int max = Integer.parseInt(this.maxNumberOfClustersTextField.getText());
		int numberOfJobs = max - min + 1;
		int id = WekaClusteringConfigurationPanelController.getJobID();
		String[][] options = new String[numberOfJobs][];
		for (int i = 0; i < numberOfJobs; i++) {
			options[i] = new String[12];
			options[i][0] = "-I";
			options[i][1] = this.iterationsOverallTextField.getText();
			options[i][2] = "-M";
			options[i][3] = this.iterationsImproveParametersTextField.getText();
			options[i][4] = "-J";
			options[i][5] = this.iterationsImproveStrucureTextField.getText();
			options[i][6] = "-L";
			options[i][7] = "" + (min + i);
			options[i][8] = "-H";
			options[i][9] = "" + (min + i);
			options[i][10] = "-ID";
			options[i][11] = "" + id;
		}
		return options;
	}

	@Override
	public String getName() {
		return "XMeans";
	}

	@Override
	public boolean checkValues() {
		if (!UITools.checkTextFieldValueInt(this, "Min number of clusters", this.minNumberOfClustersTextField, 2,
				Integer.MAX_VALUE)
				|| !UITools.checkTextFieldValueInt(this, "Max number of clusters", this.maxNumberOfClustersTextField,
						2, Integer.MAX_VALUE)
				|| !UITools.checkTextFieldValueInt(this, "Maximum number of overall iterations",
						this.iterationsOverallTextField, 1, Integer.MAX_VALUE)
				|| !UITools.checkTextFieldValueInt(this,
						"Maximum number of iterations in the kMeans loop in the Improve-Parameter part",
						this.iterationsImproveParametersTextField, 1, Integer.MAX_VALUE)
				|| !UITools
						.checkTextFieldValueInt(
								this,
								"Maximum number of iterations in the kMeans loop for the splitted centroids in the Improve-Structure part",
								this.iterationsImproveParametersTextField, 1, Integer.MAX_VALUE)) {
			return false;
		}
		int min = Integer.parseInt(this.minNumberOfClustersTextField.getText());
		int max = Integer.parseInt(this.maxNumberOfClustersTextField.getText());
		if (max < min) {
			JOptionPane.showMessageDialog(this,
					"Max number of clusters has to be greater or equal than the min number!", "Illegal Argument",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
}

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
package org.openscience.cdk.applications.taverna.ui.weka;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

/**
 * Controller class of the weka clustering configuration panel.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class WekaClusteringConfigurationPanelView extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3034371025237655412L;
	private JComboBox clustererComboBox;
	private JButton configureButton;
	private JList jobList;
	private JButton addJobButton;
	private JButton removeJobButton;
	private JPanel panel;
	private JButton chooseFileButton;
	private JTextField choosenFileTextField;

	public WekaClusteringConfigurationPanelView() {
		setPreferredSize(new Dimension(450, 300));
		setLayout(new BorderLayout(0, 0));

		JPanel northPanel = new JPanel();
		northPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		northPanel.setPreferredSize(new Dimension(10, 35));
		add(northPanel, BorderLayout.NORTH);
		SpringLayout sl_northPanel = new SpringLayout();
		northPanel.setLayout(sl_northPanel);

		clustererComboBox = new JComboBox();
		sl_northPanel.putConstraint(SpringLayout.NORTH, clustererComboBox, 5, SpringLayout.NORTH, northPanel);
		sl_northPanel.putConstraint(SpringLayout.WEST, clustererComboBox, 10, SpringLayout.WEST, northPanel);
		sl_northPanel.putConstraint(SpringLayout.EAST, clustererComboBox, 239, SpringLayout.WEST, northPanel);
		northPanel.add(clustererComboBox);

		configureButton = new JButton("Configure");
		sl_northPanel.putConstraint(SpringLayout.NORTH, configureButton, -1, SpringLayout.NORTH, clustererComboBox);
		sl_northPanel.putConstraint(SpringLayout.EAST, configureButton, -91, SpringLayout.EAST, northPanel);
		northPanel.add(configureButton);

		addJobButton = new JButton("Add Job");
		sl_northPanel.putConstraint(SpringLayout.NORTH, addJobButton, -1, SpringLayout.NORTH, clustererComboBox);
		sl_northPanel.putConstraint(SpringLayout.WEST, addJobButton, 10, SpringLayout.EAST, configureButton);
		northPanel.add(addJobButton);

		JPanel centerPanel = new JPanel();
		centerPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		add(centerPanel, BorderLayout.CENTER);
		SpringLayout sl_centerPanel = new SpringLayout();
		centerPanel.setLayout(sl_centerPanel);

		JScrollPane scrollPane = new JScrollPane();
		sl_centerPanel.putConstraint(SpringLayout.NORTH, scrollPane, 0, SpringLayout.NORTH, centerPanel);
		sl_centerPanel.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, centerPanel);
		sl_centerPanel.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, centerPanel);
		centerPanel.add(scrollPane);

		jobList = new JList();
		scrollPane.setViewportView(jobList);

		panel = new JPanel();
		sl_centerPanel.putConstraint(SpringLayout.WEST, panel, 0, SpringLayout.WEST, centerPanel);
		sl_centerPanel.putConstraint(SpringLayout.EAST, panel, 0, SpringLayout.EAST, centerPanel);
		sl_centerPanel.putConstraint(SpringLayout.SOUTH, scrollPane, 0, SpringLayout.NORTH, panel);
		sl_centerPanel.putConstraint(SpringLayout.NORTH, panel, -35, SpringLayout.SOUTH, centerPanel);
		sl_centerPanel.putConstraint(SpringLayout.SOUTH, panel, 226, SpringLayout.NORTH, centerPanel);
		centerPanel.add(panel);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);

		chooseFileButton = new JButton((Action) null);
		sl_panel.putConstraint(SpringLayout.NORTH, chooseFileButton, 5, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.EAST, chooseFileButton, -10, SpringLayout.EAST, panel);
		chooseFileButton.setPreferredSize(new Dimension(25, 25));
		panel.add(chooseFileButton);

		choosenFileTextField = new JTextField();
		choosenFileTextField.setEnabled(false);
		sl_panel.putConstraint(SpringLayout.NORTH, choosenFileTextField, 5, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.EAST, choosenFileTextField, -6, SpringLayout.WEST, chooseFileButton);
		sl_panel.putConstraint(SpringLayout.WEST, choosenFileTextField, 10, SpringLayout.WEST, panel);
		choosenFileTextField.setPreferredSize(new Dimension(250, 25));
		choosenFileTextField.setEditable(false);
		panel.add(choosenFileTextField);

		JPanel southPanel = new JPanel();
		southPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		FlowLayout flowLayout = (FlowLayout) southPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		add(southPanel, BorderLayout.SOUTH);

		removeJobButton = new JButton("Remove Job");
		removeJobButton.setHorizontalAlignment(SwingConstants.RIGHT);
		southPanel.add(removeJobButton);
	}

	public JComboBox getClustererComboBox() {
		return clustererComboBox;
	}

	public JButton getConfigureButton() {
		return configureButton;
	}

	public JList getJobList() {
		return jobList;
	}

	public JButton getAddJobButton() {
		return addJobButton;
	}

	public JButton getRemoveJobButton() {
		return removeJobButton;
	}

	public JButton getChooseFileButton() {
		return chooseFileButton;
	}

	public JTextField getChoosenFileTextField() {
		return choosenFileTextField;
	}
}

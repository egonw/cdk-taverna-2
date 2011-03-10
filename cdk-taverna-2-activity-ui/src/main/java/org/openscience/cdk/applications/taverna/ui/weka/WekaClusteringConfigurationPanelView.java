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
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
	private JButton btnClearAll;

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
		centerPanel.setLayout(new GridLayout(0, 1, 0, 0));

		JScrollPane scrollPane = new JScrollPane();
		centerPanel.add(scrollPane);

		jobList = new JList();
		scrollPane.setViewportView(jobList);

		JPanel southPanel = new JPanel();
		southPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		FlowLayout flowLayout = (FlowLayout) southPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		add(southPanel, BorderLayout.SOUTH);

		removeJobButton = new JButton("Remove Job");
		removeJobButton.setHorizontalAlignment(SwingConstants.RIGHT);
		southPanel.add(removeJobButton);

		btnClearAll = new JButton("Clear All");
		southPanel.add(btnClearAll);
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

	public JButton getBtnClearAll() {
		return btnClearAll;
	}
}

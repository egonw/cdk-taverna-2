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
package org.openscience.cdk.applications.taverna.ui.weka.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/**
 * Abstract clusterer configuration frame.
 * 
 * @author Andreas Truszkowski
 */
public abstract class AbstractClusteringConfigurationFrame extends JDialog {

	private static final long serialVersionUID = 8133794885345768027L;

	private ActionListener okAction = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			if (checkValues()) {
				setVisible(false);
			}
		}
	};

	/**
	 * Creates a new instance.
	 */
	public AbstractClusteringConfigurationFrame() {
		setAlwaysOnTop(true);
		setModal(true);
		this.setTitle(this.getConfiguredClass().getSimpleName());
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		FlowLayout fl_buttonPanel = (FlowLayout) buttonPanel.getLayout();
		fl_buttonPanel.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(this.okAction);
		buttonPanel.add(btnOk);
	}

	/**
	 * Checks whether the input values are correct.
	 * 
	 * @return True if the values are correct.
	 */
	public abstract boolean checkValues();

	/**
	 * @return The weka class which is configured( e.g. EM Cluster)
	 */
	public abstract Class<?> getConfiguredClass();

	/**
	 * Name of the configured class.
	 */
	public abstract String getName();

	/**
	 * Note: The jobID has to be the last item.
	 * 
	 * @return The options string.
	 */
	public abstract String[][] getOptions();

}

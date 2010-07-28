/*
 * Copyright (C) 2010 by Andreas Truszkowski <ATruszkowski@gmx.de>
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
package org.openscience.cdk.applications.taverna.ui.reactionenumerator;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;

/**
 * Configuration panel for the reaction enumerator activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class ReactionEnumeratorConfigurationPanel extends
		ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> {

	private static final long serialVersionUID = -3256167899077394762L;

	private AbstractCDKActivity activity;
	private CDKActivityConfigurationBean configBean;
	private JTextField portTextField;

	public ReactionEnumeratorConfigurationPanel(AbstractCDKActivity activity) {
		this.activity = activity;
		this.configBean = this.activity.getConfiguration();
		this.initGUI();
	}

	protected void initGUI() {
		try {
			this.removeAll();
			this.setLayout(new GridLayout(2, 0, 1, 1));
			JLabel label = new JLabel("Number of reactant ports:");
			this.add(label);
			this.portTextField = new JTextField("2");
			this.add(this.portTextField);
			this.refreshConfiguration();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean checkValues() {
		try {
			int numberOfPorts = Integer.parseInt(this.portTextField.getText());
			if (numberOfPorts <= 0) {
				throw new Exception("Number of ports must be greater than 0!");
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Invalid number of ports!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
			// Not valid, return false
			return false;
		}
		return true;
	}

	@Override
	public CDKActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}

	@Override
	public boolean isConfigurationChanged() {
		int numberOfPorts = Integer.parseInt(this.portTextField.getText());
		int oldValue = (Integer) this.configBean.getAdditionalProperty(CDKTavernaConstants.PROPERTY_REACTANT_PORTS);
		return !(numberOfPorts == oldValue);
	}

	@Override
	public void noteConfiguration() {
		this.configBean = (CDKActivityConfigurationBean) this.cloneBean(this.configBean);
		int numberOfPorts = Integer.parseInt(this.portTextField.getText());
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_REACTANT_PORTS, new Integer(numberOfPorts));
	}

	@Override
	public void refreshConfiguration() {
		int numberOfPorts = Integer.parseInt(this.portTextField.getText());
		if (numberOfPorts > 0) {
			this.portTextField.setText("" + numberOfPorts);
			this.portTextField.repaint();
		}
	}

}

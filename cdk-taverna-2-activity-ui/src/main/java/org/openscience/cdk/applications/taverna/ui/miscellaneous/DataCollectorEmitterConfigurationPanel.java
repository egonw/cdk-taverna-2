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
package org.openscience.cdk.applications.taverna.ui.miscellaneous;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;

/**
 * Configuration panel for file reading activities.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class DataCollectorEmitterConfigurationPanel extends
		ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> {

	private static final long serialVersionUID = 8171127307831390262L;

	private AbstractCDKActivity activity;
	private CDKActivityConfigurationBean configBean;

	private UUID id = null;
	private JTextField idField = new JTextField();

	private AbstractAction clipboardAction = new AbstractAction() {

		private static final long serialVersionUID = 5977560838089160808L;

		public void actionPerformed(ActionEvent e) {
			String stringID = "";
			Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
			try {
				if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					stringID = (String) t.getTransferData(DataFlavor.stringFlavor);
				}
			} catch (UnsupportedFlavorException ex) {
			} catch (IOException ex) {
			}
			DataCollectorEmitterConfigurationPanel.this.id = UUID.fromString(stringID);
			DataCollectorEmitterConfigurationPanel.this.showValue();
		}
	};

	public DataCollectorEmitterConfigurationPanel(AbstractCDKActivity activity) {
		this.activity = activity;
		this.configBean = this.activity.getConfiguration();
		this.initGUI();
	}

	protected void initGUI() {
		try {
			this.removeAll();
			this.setLayout(new GridLayout(2, 0, 1, 1));
			String description = "Set acceptor UUID";
			JLabel label = new JLabel(description + ":");
			this.add(label);
			JPanel filePanel = new JPanel();
			filePanel.setLayout(new FlowLayout());
			// filePanel.setSize(200, 0);
			this.idField = new JTextField();
			this.idField.setEditable(false);
			this.idField.setPreferredSize(new Dimension(250, 25));
			filePanel.add(this.idField);
			JButton clipboardButton = new JButton(this.clipboardAction);
			ClassLoader cld = getClass().getClassLoader();
			URL url = cld.getResources("icons/paste.gif").nextElement();
			ImageIcon icon = new ImageIcon(url);
			clipboardButton.setPreferredSize(new Dimension(25, 25));
			clipboardButton.setIcon(icon);
			filePanel.add(clipboardButton);
			this.add(filePanel);
			this.refreshConfiguration();
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.ERROR_DURING_ACTIVITY_CONFIGURATION,
					this.getClass().getSimpleName(), e);
			JOptionPane.showMessageDialog(this, CDKTavernaException.ERROR_DURING_ACTIVITY_CONFIGURATION, "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void showValue() {
		if (this.id != null) {
			this.idField.setText(this.id.toString());
			this.idField.repaint();
			this.revalidate();
		}
	}

	@Override
	public boolean checkValues() {
		if (this.id == null) {
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
		if (this.id == null) {
			return false;
		}
		UUID currentID = (UUID) configBean.getAdditionalProperty(CDKTavernaConstants.PROPERTY_UUID);
		if (currentID == null) {
			return false;
		}
		return this.id.compareTo(currentID) != 0;
	}

	@Override
	public void noteConfiguration() {
		this.configBean = (CDKActivityConfigurationBean) this.cloneBean(this.configBean);
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_UUID, this.id);
		this.idField.setText(this.id.toString());
		this.idField.repaint();
	}

	@Override
	public void refreshConfiguration() {
		this.id = (UUID) configBean.getAdditionalProperty(CDKTavernaConstants.PROPERTY_UUID);
		if (this.id != null) {
			this.idField.setText(this.id.toString());
			this.idField.repaint();
		}
	}

}

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
package org.openscience.cdk.applications.taverna.ui.io;

import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;

/**
 * Configuration panel for file writing activities.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class IterativeFileWriterConfigurationPanel extends
		ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> {

	private static final long serialVersionUID = -1161055144757128604L;

	private AbstractCDKActivity activity;
	private CDKActivityConfigurationBean configBean;

	// private File file = null;
	// private JTextField filePathField = new JTextField();
	private JCheckBox checkBox;

	// private AbstractAction chooseFileAction = new AbstractAction() {
	//
	// private static final long serialVersionUID = 2594222854083984583L;
	//
	// public void actionPerformed(ActionEvent e) {
	// JFileChooser openDialog = new JFileChooser(new
	// File(DataStreamController.getInstance().getCurrentDirectory()));
	// openDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	// if (openDialog.showOpenDialog(IterativeFileWriterConfigurationPanel.this)
	// == JFileChooser.APPROVE_OPTION) {
	// DataStreamController.getInstance().setCurrentDirectory(openDialog.getCurrentDirectory().getPath());
	// IterativeFileWriterConfigurationPanel.this.file =
	// openDialog.getSelectedFile();
	// IterativeFileWriterConfigurationPanel.this.showValue();
	// }
	// }
	// };

	public IterativeFileWriterConfigurationPanel(AbstractCDKActivity activity) {
		this.activity = activity;
		this.configBean = this.activity.getConfiguration();
		this.initGUI();
	}

	protected void initGUI() {
		try {
			this.removeAll();
			this.setLayout(new GridLayout(1, 0, 1, 1));
			// JLabel label = new JLabel("Output directory:");
			// // this.add(label);
			// JPanel filePanel = new JPanel();
			// filePanel.setLayout(new FlowLayout());
			// // filePanel.setSize(200, 0);
			// this.filePathField = new JTextField();
			// this.filePathField.setEditable(false);
			// this.filePathField.setPreferredSize(new Dimension(250, 25));
			// filePanel.add(this.filePathField);
			// JButton fileChooserButton = new JButton(this.chooseFileAction);
			// ClassLoader cld = getClass().getClassLoader();
			// URL url = cld.getResources("icons/open.gif").nextElement();
			// ImageIcon icon = new ImageIcon(url);
			// fileChooserButton.setPreferredSize(new Dimension(25, 25));
			// fileChooserButton.setIcon(icon);
			// filePanel.add(fileChooserButton);
			// //this.add(filePanel);
			this.checkBox = new JCheckBox("One file per iteration");
			this.add(this.checkBox);
			this.refreshConfiguration();
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.ERROR_DURING_ACTIVITY_CONFIGURATION,
					this.getClass().getSimpleName(), e);
			JOptionPane.showMessageDialog(this, CDKTavernaException.ERROR_DURING_ACTIVITY_CONFIGURATION, "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// private void showValue() {
	// this.filePathField.setText(this.file.getPath());
	// this.filePathField.repaint();
	// this.revalidate();
	// }

	@Override
	public boolean checkValues() {
		// if (this.file != null && this.file.exists()) {
		// return true;
		// }
		// JOptionPane.showMessageDialog(this, "Chosen directory is not valid!",
		// "Invalid directory", JOptionPane.ERROR_MESSAGE);
		// // Not valid, return false
		// return false;
		return true;
	}

	@Override
	public CDKActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}

	@Override
	public boolean isConfigurationChanged() {
		Boolean oneFilePerIteration = (Boolean) configBean
				.getAdditionalProperty(CDKTavernaConstants.PROPERTY_ONE_FILE_PER_ITERATION);
		// if (this.file == null) {
		// return false;
		// }
		// File file = (File)
		// configBean.getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE);
		// return !this.file.equals(file) && this.checkBox.isSelected() !=
		// oneFilePerIteration;
		return this.checkBox.isSelected() != oneFilePerIteration;
	}

	@Override
	public void noteConfiguration() {
		this.configBean = (CDKActivityConfigurationBean) this.cloneBean(this.configBean);
		// this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE,
		// this.file);
		// this.filePathField.setText(this.file.getPath());
		// this.filePathField.repaint();
		configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_ONE_FILE_PER_ITERATION,
				this.checkBox.isSelected());
	}

	@Override
	public void refreshConfiguration() {
		// this.file = (File)
		// configBean.getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE);
		// if (this.file != null) {
		// this.filePathField.setText(this.file.getAbsolutePath());
		// this.filePathField.repaint();
		// }
		Boolean oneFilePerIteration = (Boolean) configBean
				.getAdditionalProperty(CDKTavernaConstants.PROPERTY_ONE_FILE_PER_ITERATION);
		this.checkBox.setSelected(oneFilePerIteration);
	}

}

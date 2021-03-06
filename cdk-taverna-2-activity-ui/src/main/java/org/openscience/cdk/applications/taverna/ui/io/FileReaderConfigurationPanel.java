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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.CDKFileFilter;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.iterativeio.DataStreamController;

/**
 * Configuration panel for file reading activities.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class FileReaderConfigurationPanel extends
		ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> {

	private static final long serialVersionUID = 8171127307831390262L;

	private AbstractCDKActivity activity;
	private CDKActivityConfigurationBean configBean;

	private File[] files = null;
	private JTextField filePathField = new JTextField();

	private AbstractAction chooseFileAction = new AbstractAction() {

		private static final long serialVersionUID = 5977560838089160808L;

		public void actionPerformed(ActionEvent e) {
			JFileChooser openDialog = new JFileChooser(new File(DataStreamController.getInstance()
					.getCurrentDirectory()));
			if (FileReaderConfigurationPanel.this.activity.getConfiguration().getAdditionalProperty(
					CDKTavernaConstants.PROPERTY_SUPPORT_MULTI_FILE) != null) {
				openDialog.setMultiSelectionEnabled(true);
			}
			String extension = (String) FileReaderConfigurationPanel.this.activity.getConfiguration()
					.getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE_EXTENSION);
			String description = (String) FileReaderConfigurationPanel.this.activity.getConfiguration()
					.getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE_EXTENSION_DESCRIPTION);
			openDialog.addChoosableFileFilter(new CDKFileFilter(description, extension));
			if (openDialog.showOpenDialog(FileReaderConfigurationPanel.this) == JFileChooser.APPROVE_OPTION) {
				DataStreamController.getInstance().setCurrentDirectory(openDialog.getCurrentDirectory().getPath());
				if (FileReaderConfigurationPanel.this.activity.getConfiguration().getAdditionalProperty(
						CDKTavernaConstants.PROPERTY_SUPPORT_MULTI_FILE) != null) {
					FileReaderConfigurationPanel.this.files = openDialog.getSelectedFiles();
				} else {
					File file = openDialog.getSelectedFile();
					FileReaderConfigurationPanel.this.files = new File[] { file };
				}
				FileReaderConfigurationPanel.this.showValue();
			}
		}
	};

	public FileReaderConfigurationPanel(AbstractCDKActivity activity) {
		this.activity = activity;
		this.configBean = this.activity.getConfiguration();
		this.initGUI();
	}

	protected void initGUI() {
		try {
			this.removeAll();
			this.setLayout(new GridLayout(2, 0, 1, 1));
			String description = (String) FileReaderConfigurationPanel.this.activity.getConfiguration()
					.getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE_EXTENSION_DESCRIPTION);
			JLabel label = new JLabel(description + ":");
			this.add(label);
			JPanel filePanel = new JPanel();
			filePanel.setLayout(new FlowLayout());
			// filePanel.setSize(200, 0);
			this.filePathField = new JTextField();
			this.filePathField.setEditable(false);
			this.filePathField.setPreferredSize(new Dimension(250, 25));
			filePanel.add(this.filePathField);
			JButton fileChooserButton = new JButton(this.chooseFileAction);
			ClassLoader cld = getClass().getClassLoader();
			URL url = cld.getResources("icons/open.gif").nextElement();
			ImageIcon icon = new ImageIcon(url);
			fileChooserButton.setPreferredSize(new Dimension(25, 25));
			fileChooserButton.setIcon(icon);
			filePanel.add(fileChooserButton);
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
		String paths = "";
		for (File file : files) {
			paths += file.getPath();
			if (files.length > 1) {
				paths += ";";
			}
		}
		this.filePathField.setText(paths);
		this.filePathField.repaint();
		this.revalidate();
	}

	@Override
	public boolean checkValues() {
		String extension = (String) FileReaderConfigurationPanel.this.activity.getConfiguration()
				.getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE_EXTENSION);
		for (File file : files) {
			if (file == null || !file.exists() || !file.getPath().endsWith(extension)) {
				JOptionPane.showMessageDialog(this, "Chosen file is not valid!", "Invalid File",
						JOptionPane.ERROR_MESSAGE);
				// Not valid, return false
				return false;
			}
		}
		return true;
	}

	@Override
	public CDKActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}

	@Override
	public boolean isConfigurationChanged() {
		if (this.files == null) {
			return false;
		}
		File[] files = (File[]) configBean.getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE);
		return !this.files.equals(files);
	}

	@Override
	public void noteConfiguration() {
		this.configBean = (CDKActivityConfigurationBean) this.cloneBean(this.configBean);
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE, this.files);
		String paths = "";
		for (File file : files) {
			paths += file.getPath();
			if (files.length > 1) {
				paths += ";";
			}
		}
		this.filePathField.setText(paths);
		this.filePathField.repaint();
	}

	@Override
	public void refreshConfiguration() {
		this.files = (File[]) configBean.getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE);
		if (this.files != null) {
			String paths = "";
			for (File file : files) {
				paths += file.getPath();
				if (files.length > 1) {
					paths += ";";
				}
			}
			this.filePathField.setText(paths);
			this.filePathField.repaint();
		}
	}

}

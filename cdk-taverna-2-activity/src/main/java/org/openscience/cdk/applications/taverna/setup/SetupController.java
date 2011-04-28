/*
 * Copyright (C) 2011 by Andreas Truszkowski <ATruszkowski@gmx.de>
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
package org.openscience.cdk.applications.taverna.setup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.basicutilities.Tools;
import org.openscience.cdk.applications.taverna.iterativeio.DataStreamController;

/**
 * Controls the properties of the CDK-Taverna 2.0 project.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class SetupController {

	private static final String WORKING_DIRECTORY = "Working Directory";
	private static final String IS_DATA_CACHING = "Is Data Caching";
	private static final String IS_DATA_COMPRESSION = "Is Data Compression";

	private SetupDialog view = null;
	private static SetupController instance = null;
	private Properties properties = new Properties();
	private boolean loaded = false;

	private ActionListener chooseFileListener = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			JFileChooser openDialog = new JFileChooser(new File(DataStreamController.getInstance()
					.getCurrentDirectory()));
			openDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (openDialog.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
				view.getWorkingDirectoryTextField().setText(openDialog.getSelectedFile().getPath());
			}
		}
	};

	private ActionListener okButtonListener = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			setConfiguration();
			view.dispose();
		}
	};

	private ActionListener cancelButtonListener = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			view.dispose();
		}
	};

	private SetupController() {

	}

	/**
	 * @return The SetupController instance.
	 */
	public synchronized static SetupController getInstance() {
		if (instance == null) {
			instance = new SetupController();
		}
		return instance;
	}

	/**
	 * Load the unit test properties.
	 */
	public synchronized void loadTestConfiguration() {
		if (this.loaded) {
			return;
		}
		this.loaded = true;
		String workingDirFilename = FileNameGenerator.getTempDir() + File.separator + "Test";
		this.properties.setProperty(WORKING_DIRECTORY, workingDirFilename);
		Boolean isDataCaching = false; // Most unit tests are not working with
										// the caching
		this.properties.setProperty(IS_DATA_CACHING, "" + isDataCaching);
		Boolean isDataCompression = false; // Most unit tests are not working
											// with the caching
		this.properties.setProperty(IS_DATA_COMPRESSION, "" + isDataCompression);
	}

	/**
	 * Load the unit test properties.
	 */
	public synchronized void loadFailsafeConfiguration() {
		if (this.loaded) {
			return;
		}
		this.loaded = true;
		String workingDirFilename = FileNameGenerator.getTempDir() + File.separator + "Test";
		this.properties.setProperty(WORKING_DIRECTORY, workingDirFilename);
		Boolean isDataCaching = false; // Most unit tests are not working with
										// the caching
		this.properties.setProperty(IS_DATA_CACHING, "" + isDataCaching);
		Boolean isDataCompression = true; // Most unit tests are not working
											// with the caching
		this.properties.setProperty(IS_DATA_COMPRESSION, "" + isDataCompression);
	}

	/**
	 * Loads the configuration or creates a new configuration file when not
	 * available.
	 */
	public synchronized void loadConfiguration() {
		if (this.loaded) {
			return;
		}
		this.loaded = true;
		// Under windows restricted access to the application dir -> use temp
		// dir
		String appDir = FileNameGenerator.getTempDir(); // FileNameGenerator.getApplicationDir();
		File configFile = new File(appDir + File.separator + "cdktaverna2.config");
		if (!configFile.exists()) {
			this.showConfigurationDialog();
		} else {
			try {
				this.properties.load(new FileReader(configFile));
				if (this.properties.getProperty(IS_DATA_CACHING) == null
						|| this.properties.getProperty(IS_DATA_COMPRESSION) == null
						|| this.properties.getProperty(WORKING_DIRECTORY) == null) {
					this.showConfigurationDialog();
				}
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError(CDKTavernaException.READ_FILE_ERROR + configFile.getPath(),
						this.getClass().getSimpleName(), e);
				this.showConfigurationDialog();
			}
		}
	}

	/**
	 * Shows the configuration dialog and saves the properties to hard disk.
	 */
	private void showConfigurationDialog() {
		this.view = new SetupDialog();
		ImageIcon icon = null;
		try {
			ClassLoader cld = getClass().getClassLoader();
			URL url = cld.getResources("icons/open.gif").nextElement();
			icon = new ImageIcon(url);
			this.view.getChooseDirectoryButton().setIcon(icon);
		} catch (Exception e) {
			// Ignore. Only the symbol missing.
		}
		this.view.getOkButton().addActionListener(this.okButtonListener);
		this.view.getCancelButton().addActionListener(this.cancelButtonListener);
		this.view.getChooseDirectoryButton().addActionListener(chooseFileListener);
		Tools.centerWindowOnScreen(this.view);
		String workingDirFilename = this.properties.getProperty(WORKING_DIRECTORY);
		if (workingDirFilename == null) {
			workingDirFilename = FileNameGenerator.getTempDir();
			this.properties.setProperty(WORKING_DIRECTORY, workingDirFilename);
		}
		this.view.getWorkingDirectoryTextField().setText(workingDirFilename);
		Boolean isDataCaching = Boolean.parseBoolean(this.properties.getProperty(IS_DATA_CACHING));
		if (isDataCaching == null) {
			isDataCaching = true;
			this.properties.setProperty(IS_DATA_CACHING, "" + isDataCaching);
		}
		Boolean isDataCompression = Boolean.parseBoolean(this.properties.getProperty(IS_DATA_CACHING));
		if (isDataCompression == null) {
			isDataCompression = true;
			this.properties.setProperty(IS_DATA_COMPRESSION, "" + isDataCompression);
		}
		this.view.setVisible(true);
	}

	/**
	 * Saves properties into file.
	 */
	private void setConfiguration() {
		String appDir = FileNameGenerator.getTempDir();// FileNameGenerator.getApplicationDir();
		File configFile = new File(appDir + File.separator + "cdktaverna2.config");
		try {
			String workingDirFilename = this.view.getWorkingDirectoryTextField().getText();
			this.properties.setProperty(WORKING_DIRECTORY, workingDirFilename);
			Boolean isDataCaching = this.view.getChckbxCacheDatarecommended().isSelected();
			this.properties.setProperty(IS_DATA_CACHING, "" + isDataCaching);
			Boolean isDataCompression = this.view.getChckbxCacheDatarecommended().isSelected();
			this.properties.setProperty(IS_DATA_COMPRESSION, "" + isDataCompression);
			this.properties.store(new FileWriter(configFile), "CDK-Taverna 2.0 properties");
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.WRITE_FILE_ERROR + configFile.getPath(),
					this.getClass().getSimpleName(), e);
			return;
		}
		String workingDirFilename = this.properties.getProperty(WORKING_DIRECTORY);
		JOptionPane.showMessageDialog(this.view, "The configuration file is saved in folder:" + workingDirFilename);
	}

	/**
	 * @return The working directory path.
	 */
	public String getWorkingDir() {
		if (!this.loaded) {
			this.loadFailsafeConfiguration();
		}
		String path = this.properties.getProperty(WORKING_DIRECTORY);
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}
		return file.getPath() + File.separator;
	}

	/**
	 * @return True - CDK-Taverna 2.0 caches the data instead of Taverna itself.
	 */
	public boolean isDataCaching() {
		if (!this.loaded) {
			this.loadFailsafeConfiguration();
		}
		Boolean isDataCaching = Boolean.parseBoolean(this.properties.getProperty(IS_DATA_CACHING));
		return isDataCaching;
	}

	/**
	 * @return True - CDK-Taverna 2.0 caches the data instead of Taverna itself.
	 */
	public boolean isDataCompression() {
		if (!this.loaded) {
			this.loadFailsafeConfiguration();
		}
		Boolean isDataCompression = Boolean.parseBoolean(this.properties.getProperty(IS_DATA_COMPRESSION));
		return isDataCompression;
	}
}

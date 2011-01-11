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

import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.Preferences;
import org.openscience.cdk.applications.taverna.ui.weka.panels.AbstractConfigurationFrame;

/**
 * Controller class of the weka clustering configuration panel.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class WekaClusteringConfigurationPanelController extends
		ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> {

	private static final long serialVersionUID = -2741333043525860868L;
	private static final String CONFIG_PACKAGE = "org.openscience.cdk.applications.taverna.ui.weka.panels.clustering";
	private WekaClusteringConfigurationPanelView view = null;
	private AbstractCDKActivity activity;
	private CDKActivityConfigurationBean configBean;
	private SPIRegistry<AbstractConfigurationFrame> cdkClusteringConfigFramesRegistry = new SPIRegistry<AbstractConfigurationFrame>(
			AbstractConfigurationFrame.class);
	private List<AbstractConfigurationFrame> configFrames = null;
	private List<Integer> jobClustererIdx = new ArrayList<Integer>();
	private List<String[]> jobOptions = new ArrayList<String[]>();
	private boolean isChanged = false;
	private File file = null;
	private volatile static int jobID = 0;
	private static HashSet<Integer> usedIDs = new HashSet<Integer>();
	
	/**
	 * Action for the clear all button.
	 */
	private ActionListener clearAllAction = new ActionListener() {
		
		public void actionPerformed(ActionEvent e) {
			jobClustererIdx.clear();
			jobOptions.clear();
			showJobData();
			isChanged = true;
		}
	};
	
	/**
	 * Action for the choose file button.
	 */
	private ActionListener chooseFileAction = new ActionListener() {
		
		public void actionPerformed(ActionEvent e) {
			JFileChooser openDialog = new JFileChooser(new File(Preferences.getInstance().getCurrentDirectory()));
			openDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (openDialog.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
				Preferences.getInstance().setCurrentDirectory(openDialog.getCurrentDirectory().getPath());
				file = openDialog.getSelectedFile();
				view.getChoosenFileTextField().setText(file.getPath());
				isChanged = true;
			}
		}
	};

	/**
	 * Action for the remove job button.
	 */
	private ActionListener removeJobAction = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			int idx = view.getJobList().getSelectedIndex();
			jobClustererIdx.remove(idx);
			jobOptions.remove(idx);
			showJobData();
			isChanged = true;
		}
	};

	/**
	 * Action for the add job button.
	 */
	private ActionListener addJobListener = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			this.updateUsedOptions(jobOptions);
			int idx = view.getClustererComboBox().getSelectedIndex();
			String[][] options = configFrames.get(idx).getOptions();
			for (int i = 0; i < options.length; i++) {
				jobClustererIdx.add(idx);
				jobOptions.add(options[i]);
			}
			showJobData();
			isChanged = true;
		}
		
		private void updateUsedOptions(List<String[]> jobOptions) {
			usedIDs.clear();
			for(String[] options : jobOptions) {
				usedIDs.add(Integer.parseInt(options[options.length-1]));
			}
		}
	};

	/**
	 * Action for the configuration button.
	 */
	private ActionListener configureAction = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			int idx = view.getClustererComboBox().getSelectedIndex();
			centerWindowOnScreen(configFrames.get(idx));
			configFrames.get(idx).setVisible(true);
		}
	};

	/**
	 * Creates a new instance.
	 */
	public WekaClusteringConfigurationPanelController(AbstractCDKActivity activity) {
		try {
			this.activity = activity;
			this.configBean = this.activity.getConfiguration();
			this.view = new WekaClusteringConfigurationPanelView();
			Vector<String> clustererNames = new Vector<String>();
			this.configFrames = new ArrayList<AbstractConfigurationFrame>();
			for (AbstractConfigurationFrame configFrame : cdkClusteringConfigFramesRegistry.getInstances()) {
				if (configFrame.getClass().getName().startsWith(CONFIG_PACKAGE)) {
					clustererNames.add(configFrame.getName());
					this.configFrames.add(configFrame);
				}
			}
			DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel(clustererNames);
			this.view.getClustererComboBox().setModel(comboBoxModel);
			this.view.getConfigureButton().addActionListener(this.configureAction);
			this.view.getAddJobButton().addActionListener(this.addJobListener);
			this.view.getRemoveJobButton().addActionListener(this.removeJobAction);
			ClassLoader cld = getClass().getClassLoader();
			URL url = cld.getResources("icons/open.gif").nextElement();
			ImageIcon icon = new ImageIcon(url);
			this.view.getChooseFileButton().setIcon(icon);
			this.view.getChooseFileButton().addActionListener(this.chooseFileAction);
			this.view.getBtnClearAll().addActionListener(this.clearAllAction);
			this.add(this.view);
			this.refreshConfiguration();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean checkValues() {
		if (this.jobClustererIdx.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please add jobs!", "Nothing ToDo", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (this.file == null || !this.file.exists()) {
			JOptionPane.showMessageDialog(this, "Please choose valid directory!", "Error", JOptionPane.ERROR_MESSAGE);
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
		return this.isChanged;
	}

	@Override
	public void noteConfiguration() {
		this.configBean = (CDKActivityConfigurationBean) this.cloneBean(this.configBean);
		String data = "";
		for (int i = 0; i < this.jobClustererIdx.size(); i++) {
			data += configFrames.get(this.jobClustererIdx.get(i)).getConfiguredClass().getName();
			for (String option : jobOptions.get(i)) {
				data += ";" + option;
			}
			data += ";";
		}
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_CLUSTERING_JOB_DATA, data);
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE, this.file);
		this.isChanged = false;
	}

	@Override
	public void refreshConfiguration() {
		String data = (String) this.configBean.getAdditionalProperty(CDKTavernaConstants.PROPERTY_CLUSTERING_JOB_DATA);
		if (data == null) {
			return;
		}
		this.jobClustererIdx.clear();
		this.jobOptions.clear();
		String[] splittedData = data.split(";");
		List<String> tempOption = new ArrayList<String>();
		for (int i = 0; i < splittedData.length; i++) {
			if (splittedData[i].startsWith("weka.clusterers")) {
				for (int j = 0; j < configFrames.size(); j++) {
					if (configFrames.get(j).getConfiguredClass().getName().equals(splittedData[i])) {
						this.jobClustererIdx.add(j);
					}
				}
			} else {
				tempOption.add(splittedData[i]);
			}
			if (!tempOption.isEmpty() && (splittedData[i].startsWith("weka.clusterers") || i == splittedData.length - 1)) {
				String[] option = new String[tempOption.size()];
				option = tempOption.toArray(option);
				this.jobOptions.add(option);
				tempOption.clear();
			}
		}
		this.showJobData();
		File file = (File) this.configBean.getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE);
		if (file != null) {
			this.file = file;
			view.getChoosenFileTextField().setText(file.getPath());
		}
	}

	/**
	 * Shows the jobs.
	 */
	private void showJobData() {
		String[] listData = new String[jobClustererIdx.size()];
		for (int i = 0; i < jobClustererIdx.size(); i++) {
			String jobString = configFrames.get(jobClustererIdx.get(i)).getName();
			for (String option : jobOptions.get(i)) {
				jobString += " " + option;
			}
			listData[i] = jobString;
		}
		view.getJobList().setListData(listData);
	}

	/**
	 * Centers target window on screen.
	 */
	public static void centerWindowOnScreen(Component window) {
		Point center = new Point((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2, (int) Toolkit
				.getDefaultToolkit().getScreenSize().getHeight() / 2);
		window.setLocation((center.x - window.getWidth() / 2), (center.y - window.getHeight() / 2));
	}

	/**
	 * @return A new job ID.
	 */
	public static int getJobID() {
		do {
			jobID++;
		} while(usedIDs.contains(jobID));
		return jobID;
	}
}

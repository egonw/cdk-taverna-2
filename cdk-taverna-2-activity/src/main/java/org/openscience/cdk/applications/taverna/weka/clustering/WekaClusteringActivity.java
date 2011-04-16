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
package org.openscience.cdk.applications.taverna.weka.clustering;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;
import net.sf.taverna.t2.reference.impl.external.object.InlineStringReference;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.basicutilities.Tools;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaClusteringWorker;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaTools;

import weka.clusterers.Clusterer;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;

/**
 * Class which implements the EM (Expectation Maximisation) clustering
 * algorithm.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class WekaClusteringActivity extends AbstractCDKActivity {

	public static final String WEKA_CLUSTERING_ACTIVITY = "Weka Clustering";
	private WekaClusteringWorker[] workers = null;
	private List<String> resultFiles = null;
	private String directory;

	/**
	 * Creates a new instance.
	 */
	public WekaClusteringActivity() {
		this.INPUT_PORTS = new String[] { "Weka Dataset", "File" };
		this.OUTPUT_PORTS = new String[] { "Clustering Model Files" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 0, true, null, byte[].class);
		List<Class<? extends ExternalReferenceSPI>> expectedReferences = new ArrayList<Class<? extends ExternalReferenceSPI>>();
		expectedReferences.add(FileReference.class);
		expectedReferences.add(InlineStringReference.class);
		addInput(this.INPUT_PORTS[1], 0, false, expectedReferences, null);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
	}

	@Override
	public void work() throws Exception {
		// Get input
		Instances dataset = this.getInputAsObject(this.INPUT_PORTS[0], Instances.class);
		File targetFile = this.getInputAsFile(this.INPUT_PORTS[1]);
		this.directory = Tools.getDirectory(targetFile);
		String name = Tools.getFileName(targetFile);
		String jobData = (String) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_CLUSTERING_JOB_DATA);
		if (jobData == null) {
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.NO_CLUSTERING_DATA_AVAILABLE);
		}
		// Do work
		this.resultFiles = new ArrayList<String>();
		// Extract job data
		List<String> jobClustererNames = new ArrayList<String>();
		List<String[]> jobOptions = new ArrayList<String[]>();
		String[] splittedData = jobData.split(";");
		List<String> tempOption = new ArrayList<String>();
		for (int i = 0; i < splittedData.length; i++) {
			if (splittedData[i].startsWith("weka.clusterers")) {
				jobClustererNames.add(splittedData[i]);
			} else {
				tempOption.add(splittedData[i]);
			}
			if (!tempOption.isEmpty()
					&& (splittedData[i].startsWith("weka.clusterers") || i == splittedData.length - 1)) {
				String[] option = new String[tempOption.size()];
				option = tempOption.toArray(option);
				jobOptions.add(option);
				tempOption.clear();
			}
		}
		try {
			WekaTools tools = new WekaTools();
			dataset = Filter.useFilter(dataset, tools.getIDRemover(dataset));
			// Setup worker
			this.workers = new WekaClusteringWorker[jobClustererNames.size()];
			for (int i = 0; i < jobClustererNames.size(); i++) {
				String className = jobClustererNames.get(i);
				String[] options = jobOptions.get(i);
				this.workers[i] = new WekaClusteringWorker(this, className, options, dataset);
				this.workers[i].start();
			}
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.ERROR_INVOKING_WORKERS, this.getActivityName());
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.ERROR_INVOKING_WORKERS);
		}
		synchronized (this) {
			this.wait();
		}
		// Set output
		this.setOutputAsStringList(this.resultFiles, this.OUTPUT_PORTS[0]);
	}

	public synchronized void workerDone(Clusterer clusterer, String options) {
		File emModelFile = null;
		try {
			emModelFile = FileNameGenerator.getNewFile(directory, ".model", clusterer.getClass()
					.getSimpleName() + options);
			SerializationHelper.write(emModelFile.getPath(), clusterer);
			resultFiles.add(emModelFile.getPath());

		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.WRITE_FILE_ERROR + emModelFile.getPath(),
					this.getActivityName(), e);
		}
		try {
			boolean allDone = true;
			for (WekaClusteringWorker worker : this.workers) {
				if (!worker.isDone()) {
					allDone = false;
					break;
				}
			}
			if (allDone) {
				synchronized (this) {
					this.notify();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getActivityName() {
		return WekaClusteringActivity.WEKA_CLUSTERING_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + WekaClusteringActivity.WEKA_CLUSTERING_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.WEKA_CLUSTERING_FOLDER_NAME;
	}
}

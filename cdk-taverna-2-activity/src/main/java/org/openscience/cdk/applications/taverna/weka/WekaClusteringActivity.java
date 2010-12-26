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
package org.openscience.cdk.applications.taverna.weka;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaClusteringWorker;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaTools;

import weka.clusterers.Clusterer;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSink;
import weka.filters.Filter;

/**
 * Class which implements the EM (Expectation Maximisation) clustering algorithm.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class WekaClusteringActivity extends AbstractCDKActivity {

	public static final String WEKA_CLUSTERING_ACTIVITY = "Weka Clustering";
	private WekaClusteringWorker[] workers = null;
	private File directory = null;
	private List<String> resultFiles = null;

	/**
	 * Creates a new instance.
	 */
	public WekaClusteringActivity() {
		this.INPUT_PORTS = new String[] { "Weka Dataset" };
		this.RESULT_PORTS = new String[] { "Weka Clustering Files" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 0, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.RESULT_PORTS[0], 1);
	}

	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback) throws Exception {
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		Instances dataset;
		this.resultFiles = new ArrayList<String>();
		byte[] dataArray = (byte[]) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), byte[].class, context);
		try {
			dataset = CDKObjectHandler.getInstancesObject(dataArray);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.OBJECT_DESERIALIZATION_ERROR, this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		this.directory = (File) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE);
		if (this.directory == null) {
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.NO_OUTPUT_DIRECTORY_CHOSEN);
		}
		String jobData = (String) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_CLUSTERING_JOB_DATA);
		if (jobData == null) {
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.NO_CLUSTERING_DATA_AVAILABLE);
		}
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
			if (!tempOption.isEmpty() && (splittedData[i].startsWith("weka.clusterers") || i == splittedData.length - 1)) {
				String[] option = new String[tempOption.size()];
				option = tempOption.toArray(option);
				jobOptions.add(option);
				tempOption.clear();
			}
		}
		// Save data as arff
		File arffFile = null;
		try {
			arffFile = FileNameGenerator.getNewFile(directory.getPath(), ".arff", "ClusteringData");
			DataSink.write(arffFile.getPath(), dataset);
			this.resultFiles.add(arffFile.getPath());
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.WRITE_FILE_ERROR + arffFile.getPath(),
					this.getActivityName(), e);
		}
		// save as CSV
		File csvFile = null;
		try {
			csvFile = FileNameGenerator.getNewFile(directory.getPath(), ".csv", "ClusteringData");
			DataSink.write(csvFile.getPath(), dataset);
			this.resultFiles.add(csvFile.getPath());
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.WRITE_FILE_ERROR + csvFile.getPath(),
					this.getActivityName(), e);
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
			e.printStackTrace();
		}
		synchronized (this) {
			this.wait();
		}
		T2Reference containerRef = referenceService.register(this.resultFiles, 1, true, context);
		outputs.put(this.RESULT_PORTS[0], containerRef);
		return outputs;
	}

	public synchronized void workerDone(Clusterer clusterer, String options) {
		File emModelFile = null;
		try {
			emModelFile = FileNameGenerator.getNewFile(directory.getPath(), ".model", clusterer.getClass().getSimpleName()
					+ options);
			SerializationHelper.write(emModelFile.getPath(), clusterer);
			resultFiles.add(emModelFile.getPath());

		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.WRITE_FILE_ERROR + emModelFile.getPath(),
					this.getActivityName(), e);
		}
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
		return CDKTavernaConstants.WEKA_FOLDER_NAME;
	}
}

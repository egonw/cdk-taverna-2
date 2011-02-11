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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaTools;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;

/**
 * Class which implements the extract clustering statistics activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class ExtractClusteringResultAsCSVActivity extends AbstractCDKActivity {

	public static final String EXTRACT_CLUSTERING_RESULT_AS_CSV_ACTIVITY = "Extract Clustering Result As CSV";

	/**
	 * Creates a new instance.
	 */
	public ExtractClusteringResultAsCSVActivity() {
		this.INPUT_PORTS = new String[] { "Weka Clustering Files" };
		this.OUTPUT_PORTS = new String[] { "Result Files" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
	}

	@Override
	public void work() throws Exception {
		// Get input
		List<String> files = this.getInputAsList(this.INPUT_PORTS[0], String.class);
		// Do work
		ArrayList<String> resultFiles = new ArrayList<String>();
		Instances dataset = null;
		Instances uuids = null;
		Clusterer clusterer = null;
		WekaTools tools = new WekaTools();
		for (int i = 2; i < files.size(); i++) { // The first two files are data files
			try {
				// Load clusterer
				clusterer = (Clusterer) SerializationHelper.read(files.get(i));
				// load data
				BufferedReader buffReader = new BufferedReader(new FileReader(files.get(0)));
				dataset = new Instances(buffReader);
				buffReader.close();
				uuids = Filter.useFilter(dataset, tools.getIDGetter(dataset));
				dataset = Filter.useFilter(dataset, tools.getIDRemover(dataset));
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError(CDKTavernaException.LOADING_CLUSTERING_DATA_ERROR,
						this.getActivityName(), e);
				throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.LOADING_CLUSTERING_DATA_ERROR);
			}
			try {
				// Write statistics file.
				ClusterEvaluation eval = new ClusterEvaluation();
				eval.setClusterer(clusterer);
				eval.evaluateClusterer(dataset);
				String path = new File(files.get(0)).getParent();
				String name = clusterer.getClass().getSimpleName();
				File resultFile = FileNameGenerator.getNewFile(path, ".txt",
						name + tools.getOptionsFromFile(new File(files.get(i)), name) + "_ClusteringStats");
				PrintWriter writer = new PrintWriter(resultFile);
				resultFiles.add(resultFile.getPath());
				writer.write(eval.clusterResultsToString());
				writer.close();
				// Write UUID-Cluster CSV file
				resultFile = FileNameGenerator.getNewFile(path, ".csv",
						name + tools.getOptionsFromFile(new File(files.get(i)), name) + "_UUIDCluster");
				writer = new PrintWriter(resultFile);
				resultFiles.add(resultFile.getPath());
				writer.write("UUID;Cluster_Number;\n");
				for (int j = 0; j < dataset.numInstances(); j++) {
					Instance instance = dataset.instance(j);
					int cluster = clusterer.clusterInstance(instance);
					String uuid = uuids.instance(j).stringValue(0);
					writer.write(uuid + ";" + cluster + ";\n");
				}
				writer.close();
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError(CDKTavernaException.PROCESS_WEKA_RESULT_ERROR,
						this.getActivityName(), e);
				throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.PROCESS_WEKA_RESULT_ERROR);
			}
		}
		// Set output
		this.setOutputAsStringList(resultFiles, this.OUTPUT_PORTS[0]);
	}

	@Override
	public String getActivityName() {
		return ExtractClusteringResultAsCSVActivity.EXTRACT_CLUSTERING_RESULT_AS_CSV_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + ExtractClusteringResultAsCSVActivity.EXTRACT_CLUSTERING_RESULT_AS_CSV_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.WEKA_FOLDER_NAME;
	}
}

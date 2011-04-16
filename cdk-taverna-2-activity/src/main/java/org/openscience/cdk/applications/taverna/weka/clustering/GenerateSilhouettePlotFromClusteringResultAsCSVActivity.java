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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;
import net.sf.taverna.t2.reference.impl.external.object.InlineStringReference;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaTools;

import weka.clusterers.Clusterer;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;

/**
 * Class which implements the generate silhouette plot from clustering result as
 * csv activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class GenerateSilhouettePlotFromClusteringResultAsCSVActivity extends AbstractCDKActivity {

	public static final String GENERATE_SILHOUETTE_PLOT_FROM_CLUSTERING_RESULT_AS_CSV_ACTIVITY = "Generate Silhouette Plot From Clustering Result As CSV";

	/**
	 * Creates a new instance.
	 */
	public GenerateSilhouettePlotFromClusteringResultAsCSVActivity() {
		this.INPUT_PORTS = new String[] { "Clustering Model Files", " Weka Dataset" };
		this.OUTPUT_PORTS = new String[] { "Files" };
	}

	@Override
	protected void addInputPorts() {
		List<Class<? extends ExternalReferenceSPI>> expectedReferences = new ArrayList<Class<? extends ExternalReferenceSPI>>();
		expectedReferences.add(FileReference.class);
		expectedReferences.add(InlineStringReference.class);
		addInput(this.INPUT_PORTS[0], 1, false, expectedReferences, null);
		addInput(this.INPUT_PORTS[1], 0, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
	}

	@Override
	public void work() throws Exception {
		// Get input
		List<File> files = this.getInputAsFileList(this.INPUT_PORTS[0]);
		Instances dataset = this.getInputAsObject(this.INPUT_PORTS[1], Instances.class);
		// Do work
		ArrayList<String> resultFiles = new ArrayList<String>();
		Clusterer clusterer = null;
		WekaTools tools = new WekaTools();
		HashMap<Integer, LinkedList<String>> meanTable = new HashMap<Integer, LinkedList<String>>();
		for (int i = 0; i < files.size(); i++) {
			try {
				// Load clusterer
				clusterer = (Clusterer) SerializationHelper.read(files.get(i).getPath());
				// Prepare data
				dataset = Filter.useFilter(dataset, tools.getIDRemover(dataset));
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError(CDKTavernaException.LOADING_CLUSTERING_DATA_ERROR,
						this.getActivityName(), e);
				throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.LOADING_CLUSTERING_DATA_ERROR);
			}
			try {
				double[][] s = tools.generateSilhouettePlot(dataset, clusterer);
				// Generate csv
				File file = files.get(0);
				String name = clusterer.getClass().getSimpleName();
				String options = tools.getOptionsFromFile(files.get(i), name);
				int jobID = tools.getIDFromOptions(options);
				file = FileNameGenerator.getNewFile(file.getParent(), ".csv", name + options + "-Silhouette");
				String line = "";
				PrintWriter writer = new PrintWriter(file);
				line += "Cluster;Index;SilhouetteWidth;";
				writer.write(line + "\n");
				for (int j = 0; j < clusterer.numberOfClusters(); j++) {
					Arrays.sort(s[j]);
					for (int k = 0; k < s[j].length; k++) {
						line = (j + 1) + ";" + (k + 1) + ";" + String.format("%.2f", s[j][k]) + ";";
						writer.write(line + "\n");
					}
				}
				writer.close();
				resultFiles.add(file.getPath());
				// Save mean value
				LinkedList<String> meanList = meanTable.get(jobID);
				double mean = tools.calculateSilhouetteMean(s);
				if (meanList == null) {
					meanList = new LinkedList<String>();
					meanTable.put(jobID, meanList);
				}
				meanList.add(clusterer.numberOfClusters() + ";" + mean + ";");
				if (i == (files.size() - 1)) {
					// Write mean csv
					file = FileNameGenerator.getNewFile(file.getParent(), ".csv", "Silhouette-Mean");
					writer = new PrintWriter(file);
					line = "JobID;NumberOfClusters;Mean;";
					writer.write(line + "\n");
					for (Entry<Integer, LinkedList<String>> entry : meanTable.entrySet()) {
						int id = entry.getKey();
						LinkedList<String> ml = entry.getValue();
						for (String m : ml) {
							line = id + ";" + m;
							writer.write(line + "\n");
						}
					}
					writer.close();
					resultFiles.add(file.getPath());
				}
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError(CDKTavernaException.PROCESS_WEKA_RESULT_ERROR,
						this.getActivityName(), e);
				throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.PROCESS_WEKA_RESULT_ERROR);
			}
		}
		this.setOutputAsStringList(resultFiles, this.OUTPUT_PORTS[0]);
	}

	@Override
	public String getActivityName() {
		return GenerateSilhouettePlotFromClusteringResultAsCSVActivity.GENERATE_SILHOUETTE_PLOT_FROM_CLUSTERING_RESULT_AS_CSV_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: "
				+ GenerateSilhouettePlotFromClusteringResultAsCSVActivity.GENERATE_SILHOUETTE_PLOT_FROM_CLUSTERING_RESULT_AS_CSV_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.WEKA_CLUSTERING_FOLDER_NAME;
	}
}

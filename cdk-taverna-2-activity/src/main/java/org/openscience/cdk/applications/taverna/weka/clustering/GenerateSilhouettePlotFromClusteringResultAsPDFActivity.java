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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;
import net.sf.taverna.t2.reference.impl.external.object.InlineStringReference;

import org.jfree.data.category.DefaultCategoryDataset;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ChartTool;
import org.openscience.cdk.applications.taverna.basicutilities.CollectionUtilities;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaTools;

import weka.clusterers.Clusterer;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;

/**
 * Class which implements the extract clustering result as pdf activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class GenerateSilhouettePlotFromClusteringResultAsPDFActivity extends AbstractCDKActivity {

	public static final String GENERATE_SILHOUETTE_PLOT_FROM_CLUSTERING_RESULT_AS_PDF_ACTIVITY = "Generate Silhouette Plot From Clustering Result As PDF";

	/**
	 * Creates a new instance.
	 */
	public GenerateSilhouettePlotFromClusteringResultAsPDFActivity() {
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
		ChartTool chartToolReloaded = new ChartTool();
		HashMap<Integer, LinkedList<Double>> meanValueMap = new HashMap<Integer, LinkedList<Double>>();
		HashMap<Integer, LinkedList<Integer>> meanClustersMap = new HashMap<Integer, LinkedList<Integer>>();
		HashMap<Integer, String> idNameMap = new HashMap<Integer, String>();
		File parent = null;
		List<Object> charts = new LinkedList<Object>();
		WekaTools tools = new WekaTools();
		// Prepare data
		dataset = Filter.useFilter(dataset, tools.getIDRemover(dataset));
		for (int i = 0; i < files.size(); i++) {
			charts.clear();
			try {
				// Load clusterer
				clusterer = (Clusterer) SerializationHelper.read(files.get(i).getPath());
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError(CDKTavernaException.LOADING_CLUSTERING_DATA_ERROR,
						this.getActivityName(), e);
				throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.LOADING_CLUSTERING_DATA_ERROR);
			}
			try {
				double[][] s = tools.generateSilhouettePlot(dataset, clusterer);
				// Generate chart
				String name = clusterer.getClass().getSimpleName();
				String options = tools.getOptionsFromFile(files.get(i), name);
				int jobID = tools.getIDFromOptions(options);
				for (int j = 0; j < clusterer.numberOfClusters(); j++) {
					Arrays.sort(s[j]);
					DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
					double skip = 0;
					for (int k = 0; k < s[j].length; k++) {
						skip += 1.0 - (50.0 / s[j].length);
						if (skip >= 1.0 && s[j].length > 50) {
							skip -= 1.0;
						} else {
							dataSet.addValue(s[j][k], "Silhouette width", "" + (k + 1));
						}
					}
					charts.add(chartToolReloaded.createBarChart(clusterer.getClass().getSimpleName() + " - JobID "
							+ jobID + " - Cluster " + (j + 1), "Cluster Item", "Silhouette Width", dataSet));
				}
				File file = files.get(0);
				parent = file.getParentFile();
				idNameMap.put(jobID, name);
				file = FileNameGenerator.getNewFile(file.getParent(), ".pdf", name + options + "-Silhouette");
				chartToolReloaded.writeChartAsPDF(file, charts);
				resultFiles.add(file.getPath());
				// resultFileNames.add(file.getAbsolutePath());
				// Save mean value
				LinkedList<Double> meanValueList = meanValueMap.get(jobID);
				LinkedList<Integer> meanClustersList = meanClustersMap.get(jobID);
				double mean = tools.calculateSilhouetteMean(s);
				if (meanValueList == null) {
					meanValueList = new LinkedList<Double>();
					meanValueMap.put(jobID, meanValueList);
					meanClustersList = new LinkedList<Integer>();
					meanClustersMap.put(jobID, meanClustersList);
				}
				meanValueList.add(mean);
				meanClustersList.add(clusterer.numberOfClusters());
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError(CDKTavernaException.PROCESS_WEKA_RESULT_ERROR,
						this.getActivityName(), e);
				throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.PROCESS_WEKA_RESULT_ERROR);
			}
		}
		try {
			charts.clear();
			for (Entry<Integer, LinkedList<Double>> entry : meanValueMap.entrySet()) {
				int jobID = entry.getKey();
				LinkedList<Double> meanValueList = meanValueMap.get(jobID);
				LinkedList<Integer> meanClustersList = meanClustersMap.get(jobID);
				CollectionUtilities.sortTwoLists(meanClustersList, meanValueList);
				DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
				for (int i = 0; i < meanValueList.size(); i++) {
					dataSet.addValue(meanValueList.get(i), "Silhouette width", meanClustersList.get(i));
				}
				charts.add(chartToolReloaded.createLineChart(idNameMap.get(jobID) + " - JobID: " + jobID,
						"Number of Clusters", "Mean Silhouette Width", dataSet));
			}
			File file = FileNameGenerator.getNewFile(parent.getPath(), ".pdf", "Silhouette-Mean");
			chartToolReloaded.writeChartAsPDF(file, charts);
			resultFiles.add(file.getPath());
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.PROCESS_WEKA_RESULT_ERROR, this.getActivityName(),
					e);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.PROCESS_WEKA_RESULT_ERROR);
		}
		// Set output
		this.setOutputAsStringList(resultFiles, this.OUTPUT_PORTS[0]);
	}

	@Override
	public String getActivityName() {
		return GenerateSilhouettePlotFromClusteringResultAsPDFActivity.GENERATE_SILHOUETTE_PLOT_FROM_CLUSTERING_RESULT_AS_PDF_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: "
				+ GenerateSilhouettePlotFromClusteringResultAsPDFActivity.GENERATE_SILHOUETTE_PLOT_FROM_CLUSTERING_RESULT_AS_PDF_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.WEKA_CLUSTERING_FOLDER_NAME;
	}
}

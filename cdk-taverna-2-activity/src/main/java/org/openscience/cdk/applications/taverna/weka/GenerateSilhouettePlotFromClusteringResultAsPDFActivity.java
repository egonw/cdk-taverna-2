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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.jfree.chart.JFreeChart;
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
		this.INPUT_PORTS = new String[] { "Weka Clustering Files" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		// empty
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openscience.cdk.applications.taverna.AbstractCDKActivity#work(java.util.Map,
	 * net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openscience.cdk.applications.taverna.AbstractCDKActivity#work(java.util.Map,
	 * net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		List<String> files = (List<String>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), String.class,
				context);
		if (files == null || files.isEmpty()) {
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.NO_CLUSTERING_DATA_AVAILABLE);
		}
		Instances dataset = null;
		Clusterer clusterer = null;
		ChartTool chartToolReloaded = new ChartTool();
		HashMap<Integer, LinkedList<Double>> meanValueMap = new HashMap<Integer, LinkedList<Double>>();
		HashMap<Integer, LinkedList<Integer>> meanClustersMap = new HashMap<Integer, LinkedList<Integer>>();
		HashMap<Integer, String> idNameMap = new HashMap<Integer, String>();
		File parent = null;
		List<JFreeChart> charts = new LinkedList<JFreeChart>();
		for (int i = 2; i < files.size(); i++) { // The first two file are data files
			WekaTools tools = new WekaTools();
			charts.clear();
			try {
				// Load clusterer
				clusterer = (Clusterer) SerializationHelper.read(files.get(i));
				// load data
				BufferedReader buffReader = new BufferedReader(new FileReader(files.get(0)));
				dataset = new Instances(buffReader);
				buffReader.close();
				dataset = Filter.useFilter(dataset, tools.getIDRemover(dataset));
			} catch (Exception e) {
				ErrorLogger.getInstance()
						.writeError(CDKTavernaException.LOADING_CLUSTERING_DATA_ERROR, this.getActivityName(), e);
				throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.LOADING_CLUSTERING_DATA_ERROR);
			}
			try {
				double[][] s = tools.generateSilhouettePlot(dataset, clusterer);
				// Generate chart
				String name = clusterer.getClass().getSimpleName();
				String options = tools.getOptionsFromFile(new File(files.get(i)), name);
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
					charts.add(chartToolReloaded.createBarChart(clusterer.getClass().getSimpleName() + " - JobID " + jobID
							+ " - Cluster " + (j + 1), "Cluster Item", "Silhouette Width", dataSet));
				}
				File file = new File(files.get(0));
				parent = file.getParentFile();
				idNameMap.put(jobID, name);
				file = FileNameGenerator.getNewFile(file.getParent(), ".pdf", name + options + "-Silhouette");
				chartToolReloaded.writeChartAsPDF(file, charts);
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
				e.printStackTrace();
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
				charts.add(chartToolReloaded.createLineChart(idNameMap.get(jobID) + " - JobID: " + jobID, "Number of Clusters",
						"Mean Silhouette Width", dataSet));
			}
			File file = FileNameGenerator.getNewFile(parent.getPath(), ".pdf", "Silhouette-Mean");
			chartToolReloaded.writeChartAsPDF(file, charts);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outputs;
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
		return CDKTavernaConstants.WEKA_FOLDER_NAME;
	}
}

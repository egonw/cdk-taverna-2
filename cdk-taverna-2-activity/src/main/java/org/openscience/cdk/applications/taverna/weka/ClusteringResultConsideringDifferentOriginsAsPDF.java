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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ChartTool;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaTools;

import weka.clusterers.Clusterer;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.SerializationHelper;
import weka.filters.Filter;

/**
 * Class which represents the the clustering result considering different origins to PDF activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class ClusteringResultConsideringDifferentOriginsAsPDF extends AbstractCDKActivity {

	public static final String CLUSTERING_RESULT_CONSIDERING_DIFFERENT_ORIGINS_AS_PDF_ACTIVITY = "Clustering Result Considering Different Origins As PDF";

	/**
	 * Creates a new instance.
	 */
	public ClusteringResultConsideringDifferentOriginsAsPDF() {
		this.INPUT_PORTS = new String[] { "Weka Clustering Files", "Relations Table" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, String.class);
		addInput(this.INPUT_PORTS[1], 1, true, null, String.class);
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
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		List<String> resultFileNames = new ArrayList<String>();
		List<String> pdfTitle = new ArrayList<String>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		List<String> files = (List<String>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), String.class,
				context);
		if (files == null || files.size() == 0) {
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.NO_CLUSTERING_DATA_AVAILABLE);
		}
		ArrayList<String> relationTable = (ArrayList<String>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[1]),
				String.class, context);
		// Prepare relation table data
		ArrayList<String> subjectNames = new ArrayList<String>();
		HashMap<String, Integer> numberOfSubjectsInTable = new HashMap<String, Integer>();
		HashMap<UUID, String> subjectTable = new HashMap<UUID, String>();
		String currentName = null;
		String uuidString = null;
		// Read relation table
		for (String entry : relationTable) {
			if (entry.startsWith("> <NAME> ")) {
				currentName = entry.replaceAll("> <NAME> ", "");
				subjectNames.add(currentName);
			} else if (entry.startsWith("> <ENTRY> ")) {
				uuidString = entry.replaceAll("> <ENTRY> ", "");
				UUID uuid = UUID.fromString(uuidString);
				subjectTable.put(uuid, currentName);
				Integer value = numberOfSubjectsInTable.get(currentName);
				if (value == null) {
					value = 1;
				} else {
					value++;
				}
				numberOfSubjectsInTable.put(currentName, value);
			}
		}
		Instances dataset = null;
		Instances ids = null;
		Clusterer clusterer = null;
		ChartTool chartTool = new ChartTool();
		chartTool.setBarChartHeight(500);
		chartTool.setBarChartWidth(840);
		chartTool.setPlotOrientation(PlotOrientation.VERTICAL);
		chartTool.setDescriptionYAxis("Ratio in percent");
		chartTool.setDescriptionXAxis("(Class number/Number of Vectors)");
		chartTool.setRenderXAxisDescriptionDiagonal(true);
		ArrayList<File> tempFileList = new ArrayList<File>();
		for (int i = 2; i < files.size(); i++) { // The first two file are data files
			tempFileList.clear();
			WekaTools tools = new WekaTools();
			try {
				// Load clusterer
				clusterer = (Clusterer) SerializationHelper.read(files.get(i));
				// load data
				BufferedReader buffReader = new BufferedReader(new FileReader(files.get(0)));
				dataset = new Instances(buffReader);
				buffReader.close();
				ids = Filter.useFilter(dataset, tools.getIDGetter(dataset));
				dataset = Filter.useFilter(dataset, tools.getIDRemover(dataset));
			} catch (Exception e) {
				ErrorLogger.getInstance()
						.writeError(CDKTavernaException.LOADING_CLUSTERING_DATA_ERROR, this.getActivityName(), e);
				throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.LOADING_CLUSTERING_DATA_ERROR);
			}
			File file = new File(".");
			try {
				DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
				int numberOfClasses = clusterer.numberOfClusters();
				int[] numberOfItems = new int[numberOfClasses];
				HashMap<Integer, HashMap<String, Integer>> distributionTable = new HashMap<Integer, HashMap<String, Integer>>();
				for (int j = 0; j < dataset.numInstances(); j++) {
					Instance inst = dataset.instance(j);
					int cluster = clusterer.clusterInstance(inst);
					numberOfItems[cluster]++;
					HashMap<String, Integer> foundSubjectsTable = null;
					if (distributionTable.get(cluster) == null) {
						foundSubjectsTable = new HashMap<String, Integer>();
						distributionTable.put(cluster, foundSubjectsTable);
					} else {
						foundSubjectsTable = distributionTable.get(cluster);
					}
					UUID uuid = UUID.fromString(ids.instance(j).stringValue(0));
					String subject = subjectTable.get(uuid);
					Integer value = foundSubjectsTable.get(subject);
					if (value == null) {
						value = 1;
					} else {
						value++;
					}
					foundSubjectsTable.put(subject, value);
				}
				for (int j = 0; j < numberOfClasses; j++) {
					HashMap<String, Integer> foundSubjectsTable = null;
					if (distributionTable.get(j) == null) {
						foundSubjectsTable = new HashMap<String, Integer>();
						distributionTable.put(j, foundSubjectsTable);
					} else {
						foundSubjectsTable = distributionTable.get(j);
					}
					for (int k = 0; k < subjectNames.size(); k++) {
						String name = subjectNames.get(k);
						double proportion = 0;
						if (foundSubjectsTable.get(name) != null) {
							int value = foundSubjectsTable.get(name);
							proportion = value / (double) numberOfSubjectsInTable.get(name) * 100.0;
						}
						String xAxisHeader = "(" + (j + 1) + "/" + numberOfItems[j] + ")";
						dataSet.addValue(proportion, name, xAxisHeader);
					}
				}
				String header = "(" + clusterer.getClass().getSimpleName() + "/" + clusterer.numberOfClusters() + ")";
				file = chartTool.exportToBarChart(dataSet, header);
				tempFileList.add(file);
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError("Error during evaluation of clustering results in file: " + file.getPath(),
						this.getActivityName(), e);
			}
			try {
				file = new File(files.get(0));
				String optionString = "";
				for (String o : ((OptionHandler) clusterer).getOptions()) {
					optionString += o;
				}
				String name = clusterer.getClass().getSimpleName();
				file = FileNameGenerator.getNewFile(file.getParent(), ".pdf", name
						+ tools.getOptionsFromFile(new File(files.get(i)), name) + "-ClassificationResult");
				pdfTitle.add("(Clusterer name/Number of detected classes)");
				chartTool.setPdfPageInPortrait(false);
				chartTool.exportToChartsToPDF(tempFileList, file, pdfTitle);
				resultFileNames.add(file.getAbsolutePath());

			} catch (Exception e) {
				ErrorLogger.getInstance().writeError(CDKTavernaException.PROCESS_WEKA_RESULT_ERROR, this.getActivityName(), e);
				throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.PROCESS_WEKA_RESULT_ERROR);
			}
		}
		return null;
	}

	@Override
	public String getActivityName() {
		return ClusteringResultConsideringDifferentOriginsAsPDF.CLUSTERING_RESULT_CONSIDERING_DIFFERENT_ORIGINS_AS_PDF_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: "
				+ ClusteringResultConsideringDifferentOriginsAsPDF.CLUSTERING_RESULT_CONSIDERING_DIFFERENT_ORIGINS_AS_PDF_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.WEKA_FOLDER_NAME;
	}

}

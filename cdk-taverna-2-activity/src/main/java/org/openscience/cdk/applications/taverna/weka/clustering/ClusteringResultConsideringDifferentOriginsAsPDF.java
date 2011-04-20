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
import java.util.UUID;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;
import net.sf.taverna.t2.reference.impl.external.object.InlineStringReference;

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
 * Class which represents the the clustering result considering different
 * origins to PDF activity.
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
		this.INPUT_PORTS = new String[] { "Clustering Model Files", "Weka Dataset", "Relations Table" };
		this.OUTPUT_PORTS = new String[] { "Files" };
	}

	@Override
	protected void addInputPorts() {
		List<Class<? extends ExternalReferenceSPI>> expectedReferences = new ArrayList<Class<? extends ExternalReferenceSPI>>();
		expectedReferences.add(FileReference.class);
		expectedReferences.add(InlineStringReference.class);
		addInput(this.INPUT_PORTS[0], 1, false, expectedReferences, null);
		addInput(this.INPUT_PORTS[1], 0, true, null, byte[].class);
		addInput(this.INPUT_PORTS[2], 1, true, null, String.class);
	}

	@Override
	protected void addOutputPorts() {
		// empty
	}

	@Override
	public void work() throws Exception {
		// Get input
		List<File> files = this.getInputAsFileList(this.INPUT_PORTS[0]);
		Instances dataset = this.getInputAsObject(this.INPUT_PORTS[1], Instances.class);
		List<String> relationTable = this.getInputAsList(this.INPUT_PORTS[2], String.class);
		// Do work
		List<String> resultFileNames = new ArrayList<String>();
		List<String> pdfTitle = new ArrayList<String>();
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
		Instances uuids = null;
		Clusterer clusterer = null;
		ChartTool chartTool = new ChartTool();
		ArrayList<File> tempFileList = new ArrayList<File>();
		ArrayList<Object> charts = new ArrayList<Object>();
		WekaTools tools = new WekaTools();
		// Prepare data
		uuids = Filter.useFilter(dataset, tools.getIDGetter(dataset));
		dataset = Filter.useFilter(dataset, tools.getIDRemover(dataset));
		for (int i = 0; i < files.size(); i++) {
			tempFileList.clear();
			try {
				// Load clusterer
				clusterer = (Clusterer) SerializationHelper.read(files.get(i).getPath());
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError(CDKTavernaException.LOADING_CLUSTERING_DATA_ERROR,
						this.getActivityName(), e);
				throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.LOADING_CLUSTERING_DATA_ERROR);
			}
			try {
				DefaultCategoryDataset chartDataSet = new DefaultCategoryDataset();
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
					UUID uuid = UUID.fromString(uuids.instance(j).stringValue(0));
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
						chartDataSet.addValue(proportion, name, xAxisHeader);
					}
				}
				String name = clusterer.getClass().getSimpleName();
				String options = tools.getOptionsFromFile(files.get(i), name);
				int jobID = tools.getIDFromOptions(options);
				String optionString = "";
				for (String o : ((OptionHandler) clusterer).getOptions()) {
					optionString += o;
				}
				String header = name + " - JobID: " + jobID + "\n" + "Options: " + optionString;
				charts.add(chartTool.createBarChart(header, "(Class number/Number of Vectors)", "Ratio in percent",
						chartDataSet));
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError(
						"Error during evaluation of clustering results in file: " + files.get(i),
						this.getActivityName(), e);
			}
		}
		try {
			File file = files.get(0);
			file = FileNameGenerator.getNewFile(file.getParent(), ".pdf", "ClassificationResult");
			pdfTitle.add("(Clusterer name/Number of detected classes)");
			chartTool.writeChartAsPDF(file, charts);
			resultFileNames.add(file.getAbsolutePath());
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.PROCESS_WEKA_RESULT_ERROR, this.getActivityName(),
					e);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.PROCESS_WEKA_RESULT_ERROR);
		}
		this.setOutputAsStringList(resultFileNames, this.OUTPUT_PORTS[0]);
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
		return CDKTavernaConstants.WEKA_CLUSTERING_FOLDER_NAME;
	}

}

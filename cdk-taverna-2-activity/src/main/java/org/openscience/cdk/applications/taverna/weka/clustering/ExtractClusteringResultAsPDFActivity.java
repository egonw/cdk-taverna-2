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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ChartTool;
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
public class ExtractClusteringResultAsPDFActivity extends AbstractCDKActivity {

	public static final String EXTRACT_CLUSTERING_RESULT_AS_PDF_ACTIVITY = "Extract Clustering Result As PDF";

	/**
	 * Creates a new instance.
	 */
	public ExtractClusteringResultAsPDFActivity() {
		this.INPUT_PORTS = new String[] { "Weka Clustering Files" };
		this.OUTPUT_PORTS = new String[] { "Files" };
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
		List<String> resultFiles = new ArrayList<String>();
		List<String> pdfTitle = new ArrayList<String>();
		Instances dataset = null;
		Clusterer clusterer = null;
		ChartTool chartTool = new ChartTool();
		ArrayList<JFreeChart> charts = null;
		WekaTools tools = new WekaTools();
		for (int i = 2; i < files.size(); i++) { // The first two file are data files
			charts = new ArrayList<JFreeChart>();
			try {
				// Load clusterer
				clusterer = (Clusterer) SerializationHelper.read(files.get(i));
				// load data
				BufferedReader buffReader = new BufferedReader(new FileReader(files.get(0)));
				dataset = new Instances(buffReader);
				buffReader.close();
				dataset = Filter.useFilter(dataset, tools.getIDRemover(dataset));
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError(CDKTavernaException.LOADING_CLUSTERING_DATA_ERROR,
						this.getActivityName(), e);
				throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.LOADING_CLUSTERING_DATA_ERROR);
			}
			try {
				String row = "Number of vectors in class";
				String name = clusterer.getClass().getSimpleName();
				String options = tools.getOptionsFromFile(new File(files.get(i)), name);
				int jobID = tools.getIDFromOptions(options);
				int[] numberOfVectorsInClass = new int[clusterer.numberOfClusters()];
				for (int j = 0; j < dataset.numInstances(); j++) {
					numberOfVectorsInClass[clusterer.clusterInstance(dataset.instance(j))]++;
				}
				DefaultCategoryDataset chartDataSet = new DefaultCategoryDataset();
				for (int j = 0; j < clusterer.numberOfClusters(); j++) {
					String column = "(" + (j + 1) + "/" + numberOfVectorsInClass[j] + ")";
					chartDataSet.addValue(numberOfVectorsInClass[j], row, column);
				}
				charts.add(chartTool.createBarChart(name + " - JobID: " + jobID, "(Class number/Number of Vectors)",
						"Number of vectors", chartDataSet));
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError(CDKTavernaException.PROCESS_WEKA_RESULT_ERROR,
						this.getActivityName(), e);
				throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.PROCESS_WEKA_RESULT_ERROR);
			}
			try {
				File file = new File(files.get(0));
				String name = clusterer.getClass().getSimpleName();
				file = FileNameGenerator.getNewFile(file.getParent(), ".pdf",
						name + tools.getOptionsFromFile(new File(files.get(i)), name) + "-Result");
				pdfTitle.add("Weka " + clusterer.getClass().getSimpleName() + " Clustering Result");
				chartTool.writeChartAsPDF(file, charts);
				resultFiles.add(file.getPath());
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
		return ExtractClusteringResultAsPDFActivity.EXTRACT_CLUSTERING_RESULT_AS_PDF_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + ExtractClusteringResultAsPDFActivity.EXTRACT_CLUSTERING_RESULT_AS_PDF_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.WEKA_CLUSTERING_FOLDER_NAME;
	}
}

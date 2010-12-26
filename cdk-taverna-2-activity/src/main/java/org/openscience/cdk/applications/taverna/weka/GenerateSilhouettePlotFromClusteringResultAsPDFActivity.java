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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		List<String> pdfTitle = new ArrayList<String>();
		List<String> files = (List<String>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), String.class,
				context);
		if (files == null || files.isEmpty()) {
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.NO_CLUSTERING_DATA_AVAILABLE);
		}
		Instances dataset = null;
		Clusterer clusterer = null;
		ChartTool chartTool = new ChartTool();
		chartTool.setBarChartHeight(450);
		chartTool.setBarChartWidth(750);
		chartTool.setPlotOrientation(PlotOrientation.VERTICAL);
		chartTool.setDescriptionXAxis("(Class number/Number of Vectors)");
		chartTool.setDescriptionYAxis("Number of vectors");
		chartTool.setRenderXAxisDescriptionDiagonal(true);
		ArrayList<File> tempFileList = new ArrayList<File>();
		for (int i = 2; i < files.size(); i++) { // The first two file are data files
			WekaTools tools = new WekaTools();
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
				tempFileList.clear();
				for (int j = 0; j < clusterer.numberOfClusters(); j++) {
					Arrays.sort(s[j]);
					DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
					for (int k = 0; k < s[j].length; k++) {
						dataSet.addValue(s[j][k], "Silhouette width", "" + (k + 1));
					}
					tempFileList.add(chartTool.exportToBarChart(dataSet, clusterer.getClass().getSimpleName() + " - Cluster "
							+ (j + 1)));
				}
				File file = new File(files.get(0));
				String name = clusterer.getClass().getSimpleName();
				file = FileNameGenerator.getNewFile(file.getParent(), ".pdf", name
						+ tools.getOptionsFromFile(new File(files.get(i)), name) + "-Silhouette");
				chartTool.setPdfPageInPortrait(false);
				chartTool.exportToChartsToPDF(tempFileList, file, pdfTitle);
				// resultFileNames.add(file.getAbsolutePath());
				System.out.println();
			} catch (Exception e) {
				e.printStackTrace();
			}
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

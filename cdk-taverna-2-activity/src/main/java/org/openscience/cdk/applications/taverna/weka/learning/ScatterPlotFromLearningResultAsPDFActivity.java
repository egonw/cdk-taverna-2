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
package org.openscience.cdk.applications.taverna.weka.learning;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;
import net.sf.taverna.t2.reference.impl.external.object.InlineStringReference;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYSeries;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.basicutilities.ChartTool;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaTools;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;

/**
 * Class which implements the extract learning result as pdf activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class ScatterPlotFromLearningResultAsPDFActivity extends AbstractCDKActivity {

	public static final String SCATTER_PLOT_FROM_LEARNING_RESULT_AS_PDF_ACTIVITY = "Scatter Plot From Learning Result As PDF";

	/**
	 * Creates a new instance.
	 */
	public ScatterPlotFromLearningResultAsPDFActivity() {
		this.INPUT_PORTS = new String[] { "Model Files", "Train Data Files", "Test Datasets", "ID Class CSV" };
		this.OUTPUT_PORTS = new String[] { "Files" };
	}

	@Override
	protected void addInputPorts() {
		List<Class<? extends ExternalReferenceSPI>> expectedReferences = new ArrayList<Class<? extends ExternalReferenceSPI>>();
		expectedReferences.add(FileReference.class);
		expectedReferences.add(InlineStringReference.class);
		addInput(this.INPUT_PORTS[0], 1, false, expectedReferences, null);
		expectedReferences = new ArrayList<Class<? extends ExternalReferenceSPI>>();
		expectedReferences.add(FileReference.class);
		expectedReferences.add(InlineStringReference.class);
		addInput(this.INPUT_PORTS[1], 1, false, expectedReferences, null);
		addInput(this.INPUT_PORTS[2], 1, true, null, byte[].class);
		addInput(this.INPUT_PORTS[3], 1, true, null, String.class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
	}

	@Override
	public void work() throws Exception {
		// Get input
		List<File> modelFiles = this.getInputAsFileList(this.INPUT_PORTS[0]);
		List<File> trainDataFiles = this.getInputAsFileList(this.INPUT_PORTS[1]);
		List<Instances> testDatasets = this.getInputAsList(this.INPUT_PORTS[2], Instances.class);
		List<String> csv = this.getInputAsList(this.INPUT_PORTS[3], String.class);
		String directory = modelFiles.get(0).getParent();
		// Do work
		ArrayList<String> resultFiles = new ArrayList<String>();

		try {
			HashMap<UUID, Double> orgClassMap = new HashMap<UUID, Double>();
			HashMap<UUID, Double> calcClassMap = new HashMap<UUID, Double>();
			ArrayList<UUID> uuids = new ArrayList<UUID>();
			WekaTools tools = new WekaTools();
			ChartTool chartTool = new ChartTool();
			for (int i = 1; i < csv.size(); i++) {
				String[] frag = csv.get(i).split(";");
				UUID uuid = UUID.fromString(frag[0]);
				uuids.add(uuid);
				orgClassMap.put(uuid, Double.valueOf(frag[1]));
			}
			List<JFreeChart> rmseCharts = new ArrayList<JFreeChart>();
			List<Double> trainMeanRMSE = new ArrayList<Double>();
			List<Double> testMeanRMSE = new ArrayList<Double>();
			List<Double> cvMeanRMSE = new ArrayList<Double>();
			while (!modelFiles.isEmpty()) {
				List<Double> trainRMSE = new ArrayList<Double>();
				HashSet<Integer> trainSkippedRMSE = new HashSet<Integer>();
				List<Double> testRMSE = new ArrayList<Double>();
				HashSet<Integer> testSkippedRMSE = new HashSet<Integer>();
				List<Double> cvRMSE = new ArrayList<Double>();
				HashSet<Integer> cvSkippedRMSE = new HashSet<Integer>();
				List<Double> trainingSetRatios = new ArrayList<Double>();
				List<JFreeChart> charts = new LinkedList<JFreeChart>();
				List<String> summary = new LinkedList<String>();
				File modelFile = null;
				Classifier classifier = null;
				for (int j = 0; j < trainDataFiles.size(); j++) {
					if (modelFiles.isEmpty()) {
						break;
					}
					modelFile = modelFiles.remove(0);
					Instances trainset = (Instances) SerializationHelper.read(trainDataFiles.get(j).getPath());
					Instances testset = testDatasets.get(j);
					Instances trainUUIDSet = Filter.useFilter(trainset, tools.getIDGetter(trainset));
					trainset = Filter.useFilter(trainset, tools.getIDRemover(trainset));
					Instances testUUIDSet = Filter.useFilter(testset, tools.getIDGetter(testset));
					testset = Filter.useFilter(testset, tools.getIDRemover(testset));
					calcClassMap.clear();
					double trainingSetRatio = trainset.numInstances()
							/ (double) (trainset.numInstances() + testset.numInstances());
					trainingSetRatios.add(trainingSetRatio);
					classifier = (Classifier) SerializationHelper.read(modelFile.getPath());
					// Predict
					for (int k = 0; k < trainset.numInstances(); k++) {
						UUID uuid = UUID.fromString(trainUUIDSet.instance(k).stringValue(0));
						calcClassMap.put(uuid, classifier.classifyInstance(trainset.instance(k)));
					}
					for (int k = 0; k < testset.numInstances(); k++) {
						UUID uuid = UUID.fromString(testUUIDSet.instance(k).stringValue(0));
						calcClassMap.put(uuid, classifier.classifyInstance(testset.instance(k)));
					}
					// Evaluate model
					Evaluation trainEval = new Evaluation(trainset);
					trainEval.evaluateModel(classifier, trainset);
					Evaluation testEval = new Evaluation(testset);
					testEval.evaluateModel(classifier, testset);
					Instances fullSet = tools.getFullSet(trainset, testset);
					Evaluation cvEval = new Evaluation(trainset);
					cvEval.crossValidateModel(classifier, fullSet, 10, new Random(1));
					// Create chart
					DefaultXYDataset xyDataSet = new DefaultXYDataset();
					String trainSeries = "Training Set (RMSE: "
							+ String.format("%.2f", trainEval.rootMeanSquaredError()) + ")";
					XYSeries series = new XYSeries(trainSeries);
					for (int k = 0; k < trainUUIDSet.numInstances(); k++) {
						UUID uuid = UUID.fromString(trainUUIDSet.instance(k).stringValue(0));
						double org = orgClassMap.get(uuid);
						double calc = calcClassMap.get(uuid);
						series.add(org, calc);
					}
					xyDataSet.addSeries(trainSeries, series.toArray());
					String testSeries = "Test Set (RMSE: " + String.format("%.2f", testEval.rootMeanSquaredError())
							+ ")";
					series = new XYSeries(testSeries);
					for (int k = 0; k < testUUIDSet.numInstances(); k++) {
						UUID uuid = UUID.fromString(testUUIDSet.instance(k).stringValue(0));
						double org = orgClassMap.get(uuid);
						double calc = calcClassMap.get(uuid);
						series.add(org, calc);
					}
					xyDataSet.addSeries(testSeries, series.toArray());
					charts.add(chartTool.createScatterPlot(xyDataSet, classifier.getClass().getSimpleName()
							+ "\n Training set ratio: " + String.format("%.2f", trainingSetRatios.get(j)) + "%"
							+ "\n Model name: " + modelFile.getName(), "Original values", "Predicted values"));
					// Create summary
					String name = classifier.getClass().getSimpleName();
					String sum = "Method: " + name + " " + tools.getOptionsFromFile(modelFile, name) + "\n\n";
					sum += "Training Set:\n";
					if (trainEval.rootRelativeSquaredError() > 300) {
						trainSkippedRMSE.add(j);
					}
					trainRMSE.add(trainEval.rootMeanSquaredError());
					sum += trainEval.toSummaryString(true);
					sum += "\nTest Set:\n";
					if (testEval.rootRelativeSquaredError() > 300) {
						testSkippedRMSE.add(j);
					}
					testRMSE.add(testEval.rootMeanSquaredError());
					sum += testEval.toSummaryString(true);
					sum += "\n10-fold cross-validation:\n";
					if (cvEval.rootRelativeSquaredError() > 300) {
						cvSkippedRMSE.add(j);
					}
					cvRMSE.add(cvEval.rootMeanSquaredError());
					sum += cvEval.toSummaryString(true);
					summary.add(sum);
				}
				// Create RMSE Plot
				DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
				double meanRMSE = 0;
				for (int i = 0; i < trainRMSE.size(); i++) {
					if (!trainSkippedRMSE.contains(i)) {
						dataSet.addValue(trainRMSE.get(i), "Training Set",
								"(" + String.format("%.2f", trainingSetRatios.get(i)) + "%/" + (i + 1) + ")");
					}
					meanRMSE += trainRMSE.get(i);
				}
				trainMeanRMSE.add(meanRMSE / trainRMSE.size());
				meanRMSE = 0;
				for (int i = 0; i < testRMSE.size(); i++) {
					if (!testSkippedRMSE.contains(i)) {
						dataSet.addValue(testRMSE.get(i), "Test Set",
								"(" + String.format("%.2f", trainingSetRatios.get(i)) + "%/" + (i + 1) + ")");
					}
					meanRMSE += testRMSE.get(i);
				}
				testMeanRMSE.add(meanRMSE / testRMSE.size());
				meanRMSE = 0;
				for (int i = 0; i < cvRMSE.size(); i++) {
					if (!cvSkippedRMSE.contains(i)) {
						dataSet.addValue(cvRMSE.get(i), "10-fold Cross-validation",
								"(" + String.format("%.2f", trainingSetRatios.get(i)) + "%/" + (i + 1) + ")");
					}
					meanRMSE += cvRMSE.get(i);
				}
				cvMeanRMSE.add(meanRMSE / cvRMSE.size());
				String name = classifier.getClass().getSimpleName();
				JFreeChart rmseChart = chartTool.createLineChart(
						"RMSE Plot\n Classifier:" + name + " " + tools.getOptionsFromFile(modelFile, name),
						"(Training set ratio/Index)", "RMSE", dataSet, false);
				charts.add(rmseChart);
				rmseCharts.add(rmseChart);
				// Write PDF
				File file = FileNameGenerator.getNewFile(directory, ".pdf", "ScatterPlot");
				chartTool.writeChartAsPDF(file, charts, summary);
				resultFiles.add(file.getPath());
			}
			// Create mean RMSE plot
			DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
			for (int i = 0; i < trainMeanRMSE.size(); i++) {
				dataSet.addValue(trainMeanRMSE.get(i), "Training Set", "" + (i + 1));
			}
			for (int i = 0; i < testMeanRMSE.size(); i++) {
				dataSet.addValue(testMeanRMSE.get(i), "Test Set", "" + (i + 1));
			}
			for (int i = 0; i < cvMeanRMSE.size(); i++) {
				dataSet.addValue(cvMeanRMSE.get(i), "10-fold Cross-validation", "" + (i + 1));
			}
			JFreeChart rmseChart = chartTool.createLineChart("RMSE Mean Plot", "Dataset number", "Mean RMSE", dataSet);
			rmseCharts.add(rmseChart);
			File file = FileNameGenerator.getNewFile(directory, ".pdf", "RMSE-Sum");
			chartTool.writeChartAsPDF(file, rmseCharts);
			resultFiles.add(file.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Set output
		this.setOutputAsStringList(resultFiles, this.OUTPUT_PORTS[0]);
	}

	@Override
	public String getActivityName() {
		return ScatterPlotFromLearningResultAsPDFActivity.SCATTER_PLOT_FROM_LEARNING_RESULT_AS_PDF_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: "
				+ ScatterPlotFromLearningResultAsPDFActivity.SCATTER_PLOT_FROM_LEARNING_RESULT_AS_PDF_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.WEKA_LEARNING_FOLDER_NAME;
	}
}

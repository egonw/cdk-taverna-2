/*
 * Copyright (C) 2010 - 2011 by Andreas Truszkowski <ATruszkowski@gmx.de>
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
package org.openscience.cdk.applications.taverna.weka.classification;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;
import net.sf.taverna.t2.reference.impl.external.object.InlineStringReference;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.basicutilities.ChartTool;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaTools;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;

/**
 * Class which implements the evaluate regression results as pdf activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class EvaluateClassificationResultsAsPDFActivity extends AbstractCDKActivity {

	public static final String EVALUATE_CLASSIFICATION_RESULTS_AS_PDF_ACTIVITY = "Evaluate Classification Results as PDF";
	public static final int TEST_TRAININGSET_PORT = 0;
	public static final int SINGLE_DATASET_PORT = 1;

	/**
	 * Creates a new instance.
	 */
	public EvaluateClassificationResultsAsPDFActivity() {
		this.INPUT_PORTS = new String[] { "Classification Model Files", "Classification Train Datasets",
				"Classification Test Datasets" };
		this.OUTPUT_PORTS = new String[] { "Files" };
	}

	@Override
	protected void addInputPorts() {
		String[] options = ((String) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_SCATTER_PLOT_OPTIONS)).split(";");
		List<Class<? extends ExternalReferenceSPI>> expectedReferences = new ArrayList<Class<? extends ExternalReferenceSPI>>();
		expectedReferences.add(FileReference.class);
		expectedReferences.add(InlineStringReference.class);
		addInput(this.INPUT_PORTS[0], 1, false, expectedReferences, null);
		if (options[0].equals("" + TEST_TRAININGSET_PORT)) {
			this.INPUT_PORTS[1] = "Classification Train Datasets";
			addInput(this.INPUT_PORTS[2], 1, true, null, byte[].class);
		} else {
			this.INPUT_PORTS[1] = "Weka Classification Datasets";
		}
		addInput(this.INPUT_PORTS[1], 1, false, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
	}

	@Override
	public void work() throws Exception {
		// Get input
		String[] options = ((String) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_SCATTER_PLOT_OPTIONS)).split(";");
		List<File> modelFiles = this.getInputAsFileList(this.INPUT_PORTS[0]);
		List<Instances> trainDatasets = this.getInputAsList(this.INPUT_PORTS[1], Instances.class);
		List<Instances> testDatasets = null;
		if (options[0].equals("" + TEST_TRAININGSET_PORT)) {
			testDatasets = this.getInputAsList(this.INPUT_PORTS[2], Instances.class);
		}
		String directory = modelFiles.get(0).getParent();
		// Do work
		ChartTool chartTool = new ChartTool();
		WekaTools tools = new WekaTools();
		ArrayList<String> resultFiles = new ArrayList<String>();
		try {
			DefaultCategoryDataset meanClassificationChartset = new DefaultCategoryDataset();
			int fileIndex = 0;
			while (!modelFiles.isEmpty()) {
				fileIndex++;
				List<Object> chartsObjects = new LinkedList<Object>();
				LinkedList<Double> trainPercentage = new LinkedList<Double>();
				LinkedList<Double> testPercentage = new LinkedList<Double>();
				for (int j = 0; j < trainDatasets.size(); j++) {
					File modelFile = modelFiles.remove(0);
					Classifier classifier = (Classifier) SerializationHelper.read(modelFile.getPath());
					DefaultCategoryDataset chartDataset = new DefaultCategoryDataset();
					String summary = "";
					Instances trainset = trainDatasets.get(j);
					Instances tempset = Filter.useFilter(trainset, tools.getIDRemover(trainset));
					Evaluation trainsetEval = new Evaluation(tempset);
					trainsetEval.evaluateModel(classifier, tempset);
					String setname = "Training set (" + String.format("%.2f", trainsetEval.pctCorrect()) + "%)";
					this.createDataset(trainset, classifier, chartDataset, trainPercentage, setname);
					summary += "Training set:\n\n";
					summary += trainsetEval.toSummaryString(true);
					double ratio = 100;
					if (testDatasets != null) {
						Instances testset = testDatasets.get(j);
						tempset = Filter.useFilter(testset, tools.getIDRemover(testset));
						Evaluation testEval = new Evaluation(trainset);
						testEval.evaluateModel(classifier, tempset);
						setname = "Test set (" + String.format("%.2f", testEval.pctCorrect()) + "%)";
						this.createDataset(testset, classifier, chartDataset, testPercentage, setname);
						summary += "\nTest set:\n\n";
						summary += testEval.toSummaryString(true);
						ratio = trainset.numInstances() / (double) (trainset.numInstances() + testset.numInstances())
								* 100;
					}
					String header = classifier.getClass().getSimpleName() + "\n Training set ratio: "
							+ String.format("%.2f", ratio) + "\n" + modelFile.getName();
					chartsObjects
							.add(chartTool.createBarChart(header, "Class", "Correct classified (%)", chartDataset));
					chartsObjects.add(summary);
				}
				DefaultCategoryDataset percentageChartSet = new DefaultCategoryDataset();

				double mean = 0;
				for (int i = 0; i < trainPercentage.size(); i++) {
					percentageChartSet.addValue(trainPercentage.get(i), "Training Set", "" + (i + 1));
					mean += trainPercentage.get(i);
				}
				mean /= trainPercentage.size();
				meanClassificationChartset.addValue(mean, "Training Set", "" + fileIndex);
				mean = 0;
				for (int i = 0; i < testPercentage.size(); i++) {
					percentageChartSet.addValue(testPercentage.get(i), "Test Set", "" + (i + 1));
					mean += testPercentage.get(i);
				}
				mean /= testPercentage.size();
				meanClassificationChartset.addValue(mean, "Test Set", "" + fileIndex);
				chartsObjects.add(chartTool.createLineChart("Overall Percentages", "Index", "Correct Classified (%)",
						percentageChartSet, false, true));
				File file = FileNameGenerator.getNewFile(directory, ".pdf", "ScatterPlot");
				chartTool.writeChartAsPDF(file, chartsObjects);
				resultFiles.add(file.getPath());
			}
			JFreeChart meanChart = chartTool.createLineChart("Overall Percentages", "Model Index",
					"Correct Classified (%)", meanClassificationChartset, false, true);
			File file = FileNameGenerator.getNewFile(directory, ".pdf", "ScatterPlot");
			chartTool.writeChartAsPDF(file, Collections.singletonList((Object) meanChart));
			resultFiles.add(file.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Set output
		this.setOutputAsStringList(resultFiles, this.OUTPUT_PORTS[0]);
	}

	private void createDataset(Instances dataset, Classifier classifier, DefaultCategoryDataset chartDataset,
			LinkedList<Double> setPercentage, String setname) throws Exception {
		WekaTools tools = new WekaTools();
		HashMap<UUID, Double> orgClassMap = new HashMap<UUID, Double>();
		HashMap<UUID, Double> calcClassMap = new HashMap<UUID, Double>();
		Instances trainUUIDSet = Filter.useFilter(dataset, tools.getIDGetter(dataset));
		dataset = Filter.useFilter(dataset, tools.getIDRemover(dataset));
		for (int k = 0; k < dataset.numInstances(); k++) {
			double pred = classifier.classifyInstance(dataset.instance(k));
			UUID uuid = UUID.fromString(trainUUIDSet.instance(k).stringValue(0));
			calcClassMap.put(uuid, pred);
			orgClassMap.put(uuid, dataset.instance(k).classValue());
		}
		HashMap<Double, Integer> correctPred = new HashMap<Double, Integer>();
		HashMap<Double, Integer> occurances = new HashMap<Double, Integer>();
		for (int k = 0; k < dataset.numInstances(); k++) {
			UUID uuid = UUID.fromString(trainUUIDSet.instance(k).stringValue(0));
			double pred = calcClassMap.get(uuid);
			double org = orgClassMap.get(uuid);
			Integer oc = occurances.get(org);
			if (oc == null) {
				occurances.put(org, 1);
			} else {
				occurances.put(org, ++oc);
			}
			if (pred == org) {
				Integer co = correctPred.get(org);
				if (co == null) {
					correctPred.put(org, 1);
				} else {
					correctPred.put(org, ++co);
				}
			}
		}
		double overall = 0;
		for (Entry<Double, Integer> entry : occurances.entrySet()) {
			Double key = entry.getKey();
			int occ = entry.getValue();
			Integer pred = correctPred.get(key);
			int pre = pred == null ? 0 : pred;
			double ratio = pre / (double) occ * 100;
			overall += ratio;
			chartDataset.addValue(ratio, setname, dataset.classAttribute().value(key.intValue()));
		}
		overall /= occurances.size();
		setPercentage.add(overall);
		chartDataset.addValue(overall, setname, "Overall");
	}

	@Override
	public String getActivityName() {
		return EvaluateClassificationResultsAsPDFActivity.EVALUATE_CLASSIFICATION_RESULTS_AS_PDF_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_SCATTER_PLOT_OPTIONS, "0;false");
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: "
				+ EvaluateClassificationResultsAsPDFActivity.EVALUATE_CLASSIFICATION_RESULTS_AS_PDF_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.WEKA_CLASSIFICATION_FOLDER_NAME;
	}
}

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaTools;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.clusterers.EM;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

/**
 * Class which represents the create Weka learning dataset activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class CreateWekaLearningDatasetActivity extends AbstractCDKActivity {

	public static final String[] METHODS = new String[] { "Random", "ClusterRepresentatives", "SimpleGlobalMax" };
	public static final String CREATE_WEKA_LEARNING_DATASET_ACTIVITY = "Create Weka Learning Dataset";

	/**
	 * Creates a new instance.
	 */
	public CreateWekaLearningDatasetActivity() {
		this.INPUT_PORTS = new String[] { "Weka Dataset", "ID Class CSV" };
		this.OUTPUT_PORTS = new String[] { "Weka Train Datasets", "Weka Test Datasets" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 0, true, null, byte[].class);
		addInput(this.INPUT_PORTS[1], 1, true, null, String.class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
		addOutput(this.OUTPUT_PORTS[1], 1);
	}

	@Override
	public void work() throws Exception {
		WekaTools tools = new WekaTools();
		// Get input
		Instances dataset = this.getInputAsObject(this.INPUT_PORTS[0], Instances.class);
		List<String> csv = this.getInputAsList(this.INPUT_PORTS[1], String.class);
		if (this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_CREATE_SET_OPTIONS) == null) {
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.PLEASE_CONFIGURE_ACTIVITY);
		}
		String optionsString = (String) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_CREATE_SET_OPTIONS);
		String[] options = optionsString.split(";");
		// Do work
		HashMap<UUID, Double> classMap = new HashMap<UUID, Double>();
		for (int i = 1; i < csv.size(); i++) {
			String[] frag = csv.get(i).split(";");
			classMap.put(UUID.fromString(frag[0]), Double.valueOf(frag[1]));
		}

		List<Instances> trainSets = new ArrayList<Instances>();
		List<Instances> testSets = new ArrayList<Instances>();
		try {
			// Create the whole dataset
			Instances learningSet = tools.createLearningSet(dataset, classMap);
			// Split into train/test set
			Instances trainset = null;
			Instances testset = null;
			HashMap<Integer, Integer> trainClusterMap = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> testClusterMap = new HashMap<Integer, Integer>();
			double lowerFraction = Double.parseDouble(options[0]) / 100D;
			double higherFraction = Double.parseDouble(options[1]) / 100D;
			int steps = Integer.parseInt(options[2]);
			double stepSize = (higherFraction - lowerFraction) / (double) (steps - 1);
			for (double fraction = lowerFraction; fraction <= higherFraction; fraction += stepSize) {
				int numTrain = (int) Math.round(learningSet.numInstances() * fraction);
				int numTest = learningSet.numInstances() - numTrain;
				if (options[3].equals(METHODS[0])) {
					trainset = new Instances(learningSet, 0, numTrain);
					testset = new Instances(learningSet, numTrain, numTest);
				} else if (options[3].equals(METHODS[1]) || options[3].equals(METHODS[2])) {
					Instances clusterSet = Filter.useFilter(learningSet, tools.getIDRemover(learningSet));
					clusterSet = Filter.useFilter(clusterSet, tools.getClassRemover(clusterSet));
					clusterSet.setClassIndex(-1);
					EM clusterer = new EM();
					clusterer.setOptions(new String[] { "-N", "" + numTrain });
					clusterer.buildClusterer(clusterSet);
					Instances currentTrain = new Instances(learningSet);
					currentTrain.delete();
					Instances currentTest = new Instances(learningSet);
					currentTest.delete();
					HashSet<Integer> usedClusters = new HashSet<Integer>();
					for (int i = 0; i < learningSet.numInstances(); i++) {
						Instance instance = learningSet.instance(i);
						int cluster = clusterer.clusterInstance(clusterSet.instance(i));
						if (usedClusters.contains(cluster)) {
							testClusterMap.put(currentTest.numInstances(), cluster);
							currentTest.add(instance);
						} else {
							usedClusters.add(cluster);
							trainClusterMap.put(cluster, currentTrain.numInstances());
							currentTrain.add(instance);
						}
					}
					if (options[3].equals(METHODS[2])) {
						Class<?> classifierClass = Class.forName(options[4]);
						int iterations = Integer.parseInt(options[6]);
						boolean isBlacklisting = Boolean.parseBoolean(options[7]);
						boolean isChooseBest = Boolean.parseBoolean(options[8]);
						System.out.println("Set: " + fraction);
						LinkedList<Integer> blacklist = new LinkedList<Integer>();
						Double previousRMSE = null;
						Instances cleanTrainSet = Filter.useFilter(currentTrain, tools.getIDRemover(currentTrain));
						Instances cleanTestSet = Filter.useFilter(currentTest, tools.getIDRemover(currentTest));
						for (int i = 0; i < iterations; i++) {
							Classifier classifier = (Classifier) classifierClass.newInstance();
							String[] classOptions = options[5].split(" ");
							classifier.setOptions(classOptions);
							classifier.buildClassifier(cleanTrainSet);
							Double biggestError = null;
							int biggestErrorInst = 0;
							if (blacklist.size() > 5) {
								blacklist.remove();
							}
							for (int j = 0; j < currentTest.numInstances(); j++) {
								Instance instance = currentTest.instance(j);
								double rt = instance.value(currentTest.classIndex());
								double predrt = classifier.classifyInstance(cleanTestSet.instance(j));
								double error = Math.abs(rt - predrt);
								int c = testClusterMap.get(j);
								if (blacklist.contains(c) && isBlacklisting) {
									continue;
								}
								if (biggestError == null || biggestError < error) {
									biggestError = error;
									biggestErrorInst = j;
									blacklist.add(c);
								}
							}
							if (biggestError == null) {
								System.out.println("Stopped!");
								break;
							}
							Evaluation eval = new Evaluation(cleanTrainSet);
							eval.evaluateModel(classifier, cleanTestSet);
							System.out.println("RMSE Step " + i + ": "
									+ String.format("%.2f", eval.rootMeanSquaredError()));
							if (isChooseBest) {
								Evaluation trainEval = new Evaluation(cleanTrainSet);
								eval.evaluateModel(classifier, cleanTrainSet);
								double currentRMSE = eval.rootMeanSquaredError() * (1 - fraction);
								currentRMSE += trainEval.rootMeanSquaredError() * fraction;
								if (previousRMSE == null || previousRMSE > currentRMSE) {
									previousRMSE = currentRMSE;
									trainset = currentTrain;
									testset = currentTest;
								}
							} else {
								trainset = currentTrain;
								testset = currentTest;
							}
							int cluster = testClusterMap.get(biggestErrorInst);
							System.out.println("Switched Cluster: " + cluster);
							int trainInstance = trainClusterMap.get(cluster);
							Instance temp = currentTrain.instance(trainInstance);
							currentTrain = this.replaceInstance(currentTrain, currentTest.instance(biggestErrorInst),
									trainInstance);
							currentTest = this.replaceInstance(currentTest, temp, biggestErrorInst);
							cleanTrainSet = Filter.useFilter(currentTrain, tools.getIDRemover(currentTrain));
							cleanTestSet = Filter.useFilter(currentTest, tools.getIDRemover(currentTest));
						}
						Classifier classifier = (Classifier) classifierClass.newInstance();
						String[] classOptions = options[5].split(" ");
						classifier.setOptions(classOptions);
						classifier.buildClassifier(cleanTrainSet);
						Evaluation eval = new Evaluation(cleanTrainSet);
						eval.evaluateModel(classifier, cleanTestSet);
						System.out.println("RMSE Step " + (iterations) + ": "
								+ String.format("%.2f", eval.rootMeanSquaredError()));
						if (isChooseBest) {
							Evaluation trainEval = new Evaluation(cleanTrainSet);
							eval.evaluateModel(classifier, cleanTestSet);
							double currentRMSE = eval.rootMeanSquaredError() * (1 - fraction);
							currentRMSE += trainEval.rootMeanSquaredError() * fraction;
							if (previousRMSE == null || previousRMSE > currentRMSE) {
								previousRMSE = currentRMSE;
								trainset = currentTrain;
								testset = currentTest;
							}
						} else {
							trainset = currentTrain;
							testset = currentTest;
						}
					}
				}
				trainSets.add(trainset);
				testSets.add(testset);
			}
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error during learning dataset creation!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		// Set output
		this.setOutputAsObjectList(trainSets, this.OUTPUT_PORTS[0]);
		this.setOutputAsObjectList(testSets, this.OUTPUT_PORTS[1]);
	}

	private Instances replaceInstance(Instances instances, Instance newInst, int index) {
		Instances temp = new Instances(instances);
		temp.delete();
		for (int i = 0; i < instances.numInstances(); i++) {
			if (i != index) {
				temp.add(instances.instance(i));
			} else {
				temp.add(newInst);
			}
		}
		return temp;
	}

	@Override
	public String getActivityName() {
		return CreateWekaLearningDatasetActivity.CREATE_WEKA_LEARNING_DATASET_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + CreateWekaLearningDatasetActivity.CREATE_WEKA_LEARNING_DATASET_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.WEKA_LEARNING_FOLDER_NAME;
	}

}

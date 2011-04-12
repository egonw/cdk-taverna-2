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
package org.openscience.cdk.applications.taverna.weka.utilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.openscience.cdk.applications.taverna.weka.regression.SplitDatasetIntoTrainTestsetActivity;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

/**
 * Worker for the SplitDatasetIntoTrainTestset activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class SplitDatasetIntoTrainTestsetWorker extends Thread {

	private SplitDatasetIntoTrainTestsetActivity owner = null;
	private boolean isDone = false;

	/**
	 * Creates a new instance.
	 */
	public SplitDatasetIntoTrainTestsetWorker(SplitDatasetIntoTrainTestsetActivity owner) {
		this.owner = owner;
	}

	@Override
	public void run() {
		WekaTools tools = new WekaTools();
		String[] options = this.owner.getOptions();
		Instances learningSet = this.owner.getDataSet();
		try {
			Integer idx;
			while ((idx = owner.getWork()) != null) {
				String progress = "";
				double fraction = this.owner.getFractions()[idx];
				Instances trainset = null;
				Instances testset = null;
				HashMap<Integer, Integer> trainClusterMap = new HashMap<Integer, Integer>();
				HashMap<Integer, Integer> testClusterMap = new HashMap<Integer, Integer>();
				int numTrain = (int) Math.round(learningSet.numInstances() * fraction);
				int numTest = learningSet.numInstances() - numTrain;
				if (options[3].equals(SplitDatasetIntoTrainTestsetActivity.METHODS[0])) {
					trainset = new Instances(learningSet, 0, numTrain);
					testset = new Instances(learningSet, numTrain, numTest);
				} else if (options[3].equals(SplitDatasetIntoTrainTestsetActivity.METHODS[1])
						|| options[3].equals(SplitDatasetIntoTrainTestsetActivity.METHODS[2])) {
					Instances clusterSet = Filter.useFilter(learningSet, tools.getIDRemover(learningSet));
					clusterSet = Filter.useFilter(clusterSet, tools.getClassRemover(clusterSet));
					clusterSet.setClassIndex(-1);
					SimpleKMeans clusterer = new SimpleKMeans();
					// EM clusterer = new EM();
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
					if (options[3].equals(SplitDatasetIntoTrainTestsetActivity.METHODS[2])) {
						Class<?> classifierClass = Class.forName(options[4]);
						int iterations = Integer.parseInt(options[6]);
						boolean isBlacklisting = Boolean.parseBoolean(options[7]);
						boolean isChooseBest = Boolean.parseBoolean(options[8]);
						double blLengthRatio = Double.parseDouble(options[9]) / 100;
						progress += "Set: " + fraction + "\n";
						LinkedList<Integer> blacklist = new LinkedList<Integer>();
						Double previousRMSE = null;
						Instances cleanTrainSet = Filter.useFilter(currentTrain, tools.getIDRemover(currentTrain));
						Instances cleanTestSet = Filter.useFilter(currentTest, tools.getIDRemover(currentTest));
						int blLength = (int) Math.round(numTrain * blLengthRatio);
						for (int i = 0; i < iterations; i++) {
							Classifier classifier = (Classifier) classifierClass.newInstance();
							String[] classOptions = options[5].split(" ");
							classifier.setOptions(classOptions);
							classifier.buildClassifier(cleanTrainSet);
							Double biggestError = null;
							int biggestErrorInst = 0;
							if (blacklist.size() > blLength) {
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

								}
							}
							if (biggestError == null) {
								break;
							} else {
								blacklist.add(testClusterMap.get(biggestErrorInst));
							}
							Evaluation eval = new Evaluation(cleanTrainSet);
							eval.evaluateModel(classifier, cleanTestSet);
							progress += "RMSE Step " + i + ": " + String.format("%.2f", eval.rootMeanSquaredError())
									+ "\n";
							if (isChooseBest) {
								double currentRMSE = eval.rootMeanSquaredError();
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
							progress += "Switched Cluster: " + cluster + "\n";
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
						progress += "RMSE Step " + (iterations) + ": "
								+ String.format("%.2f", eval.rootMeanSquaredError()) + "\n";
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
				this.owner.publishTrainset(trainset, idx);
				this.owner.publishTestset(testset, idx);
				this.owner.publishProgress(progress, idx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		isDone = true;
		this.owner.workerDone();
	}

	/**
	 * Replaces a instance within a set of instances.
	 * 
	 * @param instances
	 *            Set of instances
	 * @param newInst
	 *            The new instance.
	 * @param index
	 *            Position where to insert the new instance.
	 * @return Altered instances.
	 */
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

	/**
	 * @return True when worker has finished.
	 */
	public boolean isDone() {
		return isDone;
	}

}

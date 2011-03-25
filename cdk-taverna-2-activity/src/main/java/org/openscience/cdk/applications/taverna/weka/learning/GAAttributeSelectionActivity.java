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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.hibernate.cfg.ExtendsQueueEntry;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.basicutilities.ProgressLogger;
import org.openscience.cdk.applications.taverna.weka.utilities.AttributeSelectionGenome;
import org.openscience.cdk.applications.taverna.weka.utilities.GASelectionWorker;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaLearningWorker;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaTools;

import weka.classifiers.Classifier;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSink;

/**
 * Class which represents the create GA attribute selection activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class GAAttributeSelectionActivity extends AbstractCDKActivity {

	public double ATTR_MUTATION_RATE = 0.07;
	public double CROSS_OVER_RATE = 0.05;
	public int NUMBER_OF_FOLDS = 10;
	private int NUMBER_OF_INDIVIDUALS = 50;
	private int NUMBER_OF_ITERATIONS = 2000;

	private int MIN_ATTRIBUTES = 30;
	private int MAX_ATTRIBUTES = 30;
	private int STEPSIZE = 1;

	private Class<?> classifierClass = null;
	private String classifierOptions = null;

	public static final String CREATE_WEKA_LEARNING_DATASET_ACTIVITY = "GA Attribute Selection";

	private AttributeSelectionGenome[] individuals = null;
	private int currentWork = 0;
	private GASelectionWorker[] workers = null;

	/**
	 * Creates a new instance.
	 */
	public GAAttributeSelectionActivity() {
		this.INPUT_PORTS = new String[] { "Weka Learning Dataset" };
		this.OUTPUT_PORTS = new String[] { "Optimized Dataset", "Attribut Setup CSV" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 0);
		addOutput(this.OUTPUT_PORTS[1], 0);
	}

	@Override
	public void work() throws Exception {
		Random rand = new Random();
		// Get input
		Instances orgDataset = this.getInputAsList(this.INPUT_PORTS[0], Instances.class).get(0);
		String options = (String) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_GA_ATTRIBUTE_SELECTION_OPTIONS);
		String[] optionArray = options.split(";");
		ATTR_MUTATION_RATE = Double.parseDouble(optionArray[0]);
		CROSS_OVER_RATE = Double.parseDouble(optionArray[1]);
		NUMBER_OF_INDIVIDUALS = Integer.parseInt(optionArray[2]);
		NUMBER_OF_ITERATIONS = Integer.parseInt(optionArray[3]);
		String[] attrOpt = optionArray[4].split(" ");
		if (attrOpt.length < 3) {
			MIN_ATTRIBUTES = 1;
			MAX_ATTRIBUTES = 1;
			STEPSIZE = 1;
		} else {
			MIN_ATTRIBUTES = Integer.parseInt(attrOpt[0]);
			MAX_ATTRIBUTES = Integer.parseInt(attrOpt[1]);
			STEPSIZE = Integer.parseInt(attrOpt[2]);
		}
		this.classifierClass = Class.forName(optionArray[5]);
		this.classifierOptions = optionArray[6];
		int threads = (Integer)this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_NUMBER_OF_USED_THREADS);
		workers = new GASelectionWorker[threads];
		// Do work
		ArrayList<String> resultSetFiles = new ArrayList<String>();
		ArrayList<String> resultAttrCSV = new ArrayList<String>();
		try {
			this.individuals = new AttributeSelectionGenome[NUMBER_OF_INDIVIDUALS];
			for (int numAttr = MIN_ATTRIBUTES; numAttr <= MAX_ATTRIBUTES; numAttr += STEPSIZE) {
				// Initialize individuals
				for (int i = 0; i < NUMBER_OF_INDIVIDUALS; i++) {
					this.individuals[i] = new AttributeSelectionGenome(orgDataset);
					if (attrOpt.length == 3) {
						this.individuals[i].setAttrRestriction(numAttr);
					}
				}
				AttributeSelectionGenome allTimeBest = null;
				if (attrOpt.length == 3) {
				ProgressLogger.getInstance().writeProgress(this.getActivityName(),
						"Number of Attributes: " + numAttr + "\n");
				} else {
					ProgressLogger.getInstance().writeProgress(this.getActivityName(),
							"Number of Attributes: free\n");
				}
				for (int i = 0; i < NUMBER_OF_ITERATIONS; i++) {
					ProgressLogger.getInstance().writeProgress(this.getActivityName(), "Iteration: " + i + "\n");
					for (int j = 0; j < NUMBER_OF_INDIVIDUALS; j++) {
						// Mutate
						this.individuals[j].mutate(ATTR_MUTATION_RATE);
						// Cross over
						if (rand.nextDouble() < CROSS_OVER_RATE) {
							int individual;
							do {
								individual = rand.nextInt(NUMBER_OF_INDIVIDUALS);
							} while (j == individual);
							this.individuals[j].crossOver(this.individuals[individual]);
						}
					}
					// Do scoring
					this.currentWork = 0;
					for (int j = 0; j < this.workers.length; j++) {
						this.workers[j] = new GASelectionWorker(this);
						this.workers[j].start();
					}
					synchronized (this) {
						this.wait();
					}
					// Show progress
					String progress = "";
					for (int j = 0; j < this.individuals.length; j++) {
						progress += "Individual " + (j + 1) + " - RMSE: " + this.individuals[j].getRmse()
								+ " - Score: " + this.individuals[j].getScore() + "\n";
					}
					ProgressLogger.getInstance().writeProgress(this.getActivityName(), progress);
					// Select individuals
					double maxScore = 0;
					double minScore = Double.MAX_VALUE;
					for (int j = 0; j < NUMBER_OF_INDIVIDUALS; j++) {
						maxScore = Math.max(maxScore, this.individuals[j].getScore());
						minScore = Math.min(minScore, this.individuals[j].getScore());
						if (allTimeBest == null) {
							allTimeBest = (AttributeSelectionGenome) this.individuals[j].clone();
						} else {
							if (allTimeBest.getScore() < this.individuals[j].getScore()) {
								allTimeBest = (AttributeSelectionGenome) this.individuals[j].clone();
							}
						}
					}
					double normSum = 0;
					for (int j = 0; j < this.individuals.length; j++) {
						normSum += this.individuals[j].getScore() - minScore;
					}
					AttributeSelectionGenome[] fittestIndividuals = new AttributeSelectionGenome[NUMBER_OF_INDIVIDUALS];
					for (int j = 1; j < NUMBER_OF_INDIVIDUALS; j++) {
						double r = rand.nextDouble();
						double sum = 0;
						for (int k = 0; k < NUMBER_OF_INDIVIDUALS; k++) {
							sum += (this.individuals[k].getScore() - minScore) / normSum;
							if (k == NUMBER_OF_INDIVIDUALS - 1) {
								fittestIndividuals[j] = this.individuals[k];
							} else {
								if (r < sum) {
									fittestIndividuals[j] = (AttributeSelectionGenome) this.individuals[k].clone();
									break;
								}
							}
						}
					}
					// Apply all time best
					ProgressLogger.getInstance().writeProgress(this.getActivityName(), "AllTimeBest - RMSE: " + allTimeBest.getRmse() + " - Score: " + allTimeBest.getScore()
							+ "\n");
					fittestIndividuals[0] = (AttributeSelectionGenome) allTimeBest.clone();
					this.individuals = fittestIndividuals;
				}
				File file = FileNameGenerator.getNewFile(FileNameGenerator.getCacheDir(), ".arff", "Attributes_"
						+ (allTimeBest.getOptDataset().numAttributes() - 2));
				DataSink.write(file.getPath(), allTimeBest.getOptDataset());
				resultSetFiles.add(file.getPath());
				resultAttrCSV.add(allTimeBest.getAttributeSetupCSV());
				System.out.println("# Attributes: " + numAttr + " - RMSE: " + allTimeBest.getRmse());
			}
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error during learning dataset creation!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		// Set output
		this.setOutputAsStringList(resultSetFiles, this.OUTPUT_PORTS[0]);
		this.setOutputAsStringList(resultAttrCSV, this.OUTPUT_PORTS[1]);
	}

	public synchronized AttributeSelectionGenome getWork() {
		if (this.currentWork < this.individuals.length) {
			return this.individuals[this.currentWork++];
		}
		return null;
	}

	public synchronized Classifier getClassifier() throws Exception, IllegalAccessException {
		Classifier classifier = (Classifier) this.classifierClass.newInstance();
		classifier.setOptions(this.classifierOptions.split(" "));
		return classifier;
	}
	
	public void workerDone() {
		boolean allDone = true;
		for (GASelectionWorker worker : this.workers) {
			if (!worker.isDone()) {
				allDone = false;
				break;
			}
		}
		if (allDone) {
			synchronized (this) {
				this.notify();
			}
		}
	}

	@Override
	public String getActivityName() {
		return GAAttributeSelectionActivity.CREATE_WEKA_LEARNING_DATASET_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_NUMBER_OF_USED_THREADS, Runtime.getRuntime().availableProcessors());
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + GAAttributeSelectionActivity.CREATE_WEKA_LEARNING_DATASET_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.WEKA_LEARNING_FOLDER_NAME;
	}

}

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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.basicutilities.ProgressLogger;

import weka.core.Instances;

/**
 * Class which represents the split dataset into train/testset activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public abstract class AbstractSplitTrainTestsetActivity extends AbstractCDKActivity {

	public static final String[] METHODS = new String[] { "Random", "Cluster Representatives", "Simple Global Max" };
	public static final String CREATE_WEKA_LEARNING_DATASET_ACTIVITY = "Split Dataset Into Train-/Testset";

	private SplitLearningTrainTestsetWorker[] workers = null;

	private String[] options = null;
	private Instances dataSet = null;
	private double[] fractions = null;
	private int currentIndex;

	private Instances[] trainSets = null;
	private Instances[] testSets = null;

	/**
	 * Creates a new instance.
	 */
	public AbstractSplitTrainTestsetActivity() {
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
		addOutput(this.OUTPUT_PORTS[1], 1);
	}

	@Override
	public void work() throws Exception {
		// Get input
		List<Instances> dataset = this.getInputAsList(this.INPUT_PORTS[0], Instances.class);
		if (this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_CREATE_SET_OPTIONS) == null) {
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.PLEASE_CONFIGURE_ACTIVITY);
		}
		String optionsString = (String) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_CREATE_SET_OPTIONS);
		this.options = optionsString.split(";");
		int threads = (Integer) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_NUMBER_OF_USED_THREADS);
		// Do work
		try {
			this.dataSet = dataset.get(0);
			// Split into train/test set
			double lowerFraction = Double.parseDouble(options[0]) / 100;
			int steps = Integer.parseInt(options[2]);
			double higherFraction;
			double stepSize;
			if (steps == 1) {
				higherFraction = lowerFraction;
				stepSize = 1;
			} else {
				higherFraction = Double.parseDouble(options[1]) / 100;
				stepSize = (higherFraction - lowerFraction) / (double) (steps - 1);
			}
			this.fractions = new double[steps];
			double fraction = lowerFraction;
			for (int i = 0; i < this.fractions.length; i++) {
				this.fractions[i] = fraction;
				fraction += stepSize;
			}
			// Start workers
			this.trainSets = new Instances[this.fractions.length];
			this.testSets = new Instances[this.fractions.length];
			ProgressLogger.getInstance().writeProgress(this.getActivityName(), "Starting workers");
			this.currentIndex = 0;
			this.workers = new SplitLearningTrainTestsetWorker[threads];
			for (int j = 0; j < this.workers.length; j++) {
				this.workers[j] = new SplitLearningTrainTestsetWorker(this);
				this.workers[j].start();
			}
			synchronized (this) {
				this.wait();
			}
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error during learning dataset creation!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		// Set output
		this.setOutputAsObjectList(Arrays.asList(trainSets), this.OUTPUT_PORTS[0]);
		this.setOutputAsObjectList(Arrays.asList(testSets), this.OUTPUT_PORTS[1]);
	}

	/**
	 * @return Array containing the trainset fractions.
	 */
	public double[] getFractions() {
		return this.fractions;
	}

	/**
	 * @return Index of fraction to be processed or null if ready.
	 */
	public synchronized Integer getWork() {
		if (this.currentIndex < this.fractions.length) {
			return this.currentIndex++;
		} else {
			return null;
		}
	}

	/**
	 * @param trainset
	 *            The produced trainset.
	 * @param idx
	 *            Index of the trainset.
	 */
	public synchronized void publishTrainset(Instances trainset, int idx) {
		this.trainSets[idx] = trainset;
	}

	/**
	 * @param trainset
	 *            The produced testset.
	 * @param idx
	 *            Index of the testset.
	 */
	public synchronized void publishTestset(Instances testset, int idx) {
		this.testSets[idx] = testset;
	}

	/**
	 * @param progress
	 *            Current progress.
	 * @param idx
	 *            Fraction index.
	 */
	public synchronized void publishProgress(String progress, int idx) {
		String header = "Fraction: " + String.format("%.2f", this.fractions[idx]) + "%";
		ProgressLogger.getInstance().writeProgress(this.getActivityName(), header);
		ProgressLogger.getInstance().writeProgress(this.getActivityName(), progress);
	}

	/**
	 * Called by worker when finished.
	 */
	public synchronized void workerDone() {
		boolean allDone = true;
		for (SplitLearningTrainTestsetWorker worker : this.workers) {
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

	/**
	 * @return The querz dataset.
	 */
	public Instances getDataSet() {
		return dataSet;
	}

	/**
	 * @return Options string.
	 */
	public String[] getOptions() {
		return options;
	}

	@Override
	public String getActivityName() {
		return AbstractSplitTrainTestsetActivity.CREATE_WEKA_LEARNING_DATASET_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_NUMBER_OF_USED_THREADS, Runtime.getRuntime().availableProcessors());
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + AbstractSplitTrainTestsetActivity.CREATE_WEKA_LEARNING_DATASET_ACTIVITY;
	}

}

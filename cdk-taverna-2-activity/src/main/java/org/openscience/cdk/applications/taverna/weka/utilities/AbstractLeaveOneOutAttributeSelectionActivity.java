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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.basicutilities.ProgressLogger;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.filters.Filter;

/**
 * Class which represents the heuristic attribute evaluation activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public abstract class AbstractLeaveOneOutAttributeSelectionActivity extends AbstractCDKActivity {

	public static final String LEAVEONEOUT_ATTRIBUTE_SELECTION_ACTIVITY = "Leave-One-Out Attribute Selection";

	private LeaveOneOutAttributeEvaluationWorker[] workers = null;

	public boolean USE_CV = false;
	public int FOLDS = 10;

	private Class<?> classifierClass = null;
	private String classifierOptions = null;
	private Instances currentSet = null;
	private double[] rmses = null;
	private int currentIndex;

	/**
	 * Creates a new instance.
	 */
	public AbstractLeaveOneOutAttributeSelectionActivity() {
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
		WekaTools tools = new WekaTools();
		// Get input
		Instances dataset = this.getInputAsList(this.INPUT_PORTS[0], Instances.class).get(0);
		String options = (String) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_ATTRIBUTE_SELECTION_OPTIONS);
		String[] optionArray = options.split(";");
		this.classifierClass = Class.forName(optionArray[0]);
		this.classifierOptions = optionArray[1];
		USE_CV = Boolean.parseBoolean(optionArray[2]);
		FOLDS = Integer.parseInt(optionArray[3]);
		int threads = (Integer) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_NUMBER_OF_USED_THREADS);
		// Do work
		ArrayList<Instances> newDatasets = new ArrayList<Instances>();
		LinkedList<Integer> removedAttributes = new LinkedList<Integer>();
		this.currentSet = dataset;
		ArrayList<String> attrInfo = new ArrayList<String>();
		ProgressLogger.getInstance().writeProgress(this.getActivityName(), "Starting work");
		for (int i = 0; i < dataset.numAttributes() - 3; i++) {
			// remove ID
			this.currentSet = Filter.useFilter(this.currentSet, tools.getIDRemover(this.currentSet));
			this.rmses = new double[currentSet.numAttributes() - 1];
			// Calculate RMSEs
			this.currentIndex = 0;

			this.workers = new LeaveOneOutAttributeEvaluationWorker[threads];
			for (int j = 0; j < this.workers.length; j++) {
				this.workers[j] = new LeaveOneOutAttributeEvaluationWorker(this);
				this.workers[j].start();
			}
			synchronized (this) {
				this.wait();
			}
			// Choose worst attribute (best RMSE describes worst attribute)
			double best = this.rmses[0];
			int idx = 0;
			for (int j = 1; j < this.rmses.length; j++) {
				if (best > this.rmses[j]) {
					best = this.rmses[j];
					idx = j;
				}
			}
			String name = this.currentSet.attribute(idx).name();
			idx = dataset.attribute(name).index();
			ProgressLogger.getInstance().writeProgress(this.getActivityName(),
					"Step: " + (i + 1) + " - Attribute removed: " + name);
			// range is 1..n
			removedAttributes.add(idx + 1);
			this.currentSet = Filter.useFilter(dataset, tools.getAttributRemover(dataset, removedAttributes));
		}
		// Create datasets
		Instances set;
		attrInfo.add("Index;RemovedAttribute;");
		for (int i = 0; i < removedAttributes.size(); i++) {
			String info = (i + 1) + ";" + dataset.attribute(removedAttributes.get(i) - 1).name() + ";";
			attrInfo.add(info);
			set = Filter.useFilter(dataset, tools.getAttributRemover(dataset, removedAttributes.subList(0, i + 1)));
			newDatasets.add(set);
		}
		// Set output
		this.setOutputAsObjectList(newDatasets, this.OUTPUT_PORTS[0]);
		this.setOutputAsStringList(attrInfo, this.OUTPUT_PORTS[1]);
	}

	/**
	 * @return current processed dataset.
	 */
	public Instances getCurrentSet() {
		return this.currentSet;
	}

	/**
	 * @return the configured classifier.
	 * @throws Exception
	 */
	public synchronized Classifier getClassifier() throws Exception {
		Classifier classifier = (Classifier) this.classifierClass.newInstance();
		classifier.setOptions(this.classifierOptions.split(" "));
		return classifier;
	}

	/**
	 * @return index of the current leaved out attribute.
	 */
	public synchronized Integer getWork() {
		if (this.currentIndex < this.currentSet.numAttributes() - 1) {
			return this.currentIndex++;
		} else {
			return null;
		}
	}

	/**
	 * @param result
	 *            RMSE value of processed set.
	 * @param idx
	 *            Index of the leaved out attribute.
	 */
	public synchronized void publishResult(double result, int idx) {
		this.rmses[idx] = result;
	}

	/**
	 * Called by workers which are ready.
	 */
	public synchronized void workerDone() {
		boolean allDone = true;
		for (LeaveOneOutAttributeEvaluationWorker worker : this.workers) {
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
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_NUMBER_OF_USED_THREADS, Runtime.getRuntime().availableProcessors());
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + AbstractLeaveOneOutAttributeSelectionActivity.LEAVEONEOUT_ATTRIBUTE_SELECTION_ACTIVITY;
	}

	@Override
	public String getActivityName() {
		return AbstractLeaveOneOutAttributeSelectionActivity.LEAVEONEOUT_ATTRIBUTE_SELECTION_ACTIVITY;
	}

	public boolean isUSE_CV() {
		return USE_CV;
	}

	public int getFOLDS() {
		return FOLDS;
	}

}
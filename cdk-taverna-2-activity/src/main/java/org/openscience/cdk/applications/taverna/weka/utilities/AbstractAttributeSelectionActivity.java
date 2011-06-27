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

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;

import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * Class which represents the heuristic attribute evaluation activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public abstract class AbstractAttributeSelectionActivity extends AbstractCDKActivity {

	protected AbstractAttributeEvaluationWorker[] workers = null;

	public boolean USE_CV = false;
	public int FOLDS = 10;

	protected Class<?> classifierClass = null;
	protected String classifierOptions = null;
	protected Instances currentSet = null;
	protected double[] rmses = null;
	protected int currentIndex;

	/**
	 * Creates a new instance.
	 */
	public AbstractAttributeSelectionActivity() {
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
	public abstract Object getWork();

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
		for (AbstractAttributeEvaluationWorker worker : this.workers) {
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

	public boolean isUSE_CV() {
		return USE_CV;
	}

	public int getFOLDS() {
		return FOLDS;
	}

}

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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.basicutilities.ProgressLogger;

import weka.core.Instances;
import weka.filters.Filter;

/**
 * Class which represents the heuristic attribute evaluation activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public abstract class AbstractForwardAttributeSelectionActivity extends AbstractAttributeSelectionActivity {

	public static final String FORWARD_ATTRIBUTE_SELECTION_ACTIVITY = "Forward Attribute Selection";

	private LinkedList<Integer> availableAttr;
	private HashSet<Integer> usedAttributes;

	/**
	 * Creates a new instance.
	 */
	public AbstractForwardAttributeSelectionActivity() {
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
		int threads = (Integer) this.getConfiguration()
				.getAdditionalProperty(CDKTavernaConstants.PROPERTY_NUMBER_OF_USED_THREADS);
		// Do work
		ArrayList<Instances> newDatasets = new ArrayList<Instances>();
		this.currentSet = dataset;
		ArrayList<String> attrInfo = new ArrayList<String>();
		ProgressLogger.getInstance().writeProgress(this.getActivityName(), "Starting work");
		this.usedAttributes = new HashSet<Integer>();
		// Add ID
		this.usedAttributes.add(1);
		// Add class
		this.usedAttributes.add(dataset.classIndex() + 1);
		int step = 1;
		ArrayList<Integer> attList;
		while (this.usedAttributes.size() < dataset.numAttributes()) {
			// Init set

			// Init rmses
			this.rmses = new double[dataset.numAttributes() - 2];
			for (int j = 0; j < this.rmses.length; j++) {
				this.rmses[j] = Double.MAX_VALUE;
			}
			this.availableAttr = new LinkedList<Integer>();
			for (int j = 2; j < dataset.numAttributes(); j++) {
				if (!this.usedAttributes.contains(j)) {
					this.availableAttr.add(j);
				}
			}
			// Calculate RMSEs
			this.workers = new ForwardAttributeEvaluationWorker[threads];
			for (int j = 0; j < this.workers.length; j++) {
				this.workers[j] = new ForwardAttributeEvaluationWorker(this);
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
			String name = dataset.attribute(idx + 1).name();
			ProgressLogger.getInstance().writeProgress(this.getActivityName(), "Step: " + step + " - Attribute added: " + name);
			String info = step + ";" + name + ";";
			attrInfo.add(info);
			this.usedAttributes.add(idx + 2);
			attList = new ArrayList<Integer>(this.usedAttributes);
			Collections.sort(attList);
			Instances newSet = Filter.useFilter(dataset, tools.getAttributRemover(dataset, attList, true));
			newDatasets.add(newSet);
			step++;
		}
		// Set output
		this.setOutputAsObjectList(newDatasets, this.OUTPUT_PORTS[0]);
		this.setOutputAsStringList(attrInfo, this.OUTPUT_PORTS[1]);
	}

	/**
	 * @return index of the current leaved out attribute.
	 */
	public synchronized ArrayList<Integer> getWork() {
		if (this.availableAttr.isEmpty()) {
			return null;
		} else {
			Integer idx = this.availableAttr.pollFirst();
			ArrayList<Integer> attList = new ArrayList<Integer>(this.usedAttributes);
			attList.add(idx);
			return attList;
		}
	}
	
	@Override
	public String getDescription() {
		return "Description: " + AbstractForwardAttributeSelectionActivity.FORWARD_ATTRIBUTE_SELECTION_ACTIVITY;
	}

	@Override
	public String getActivityName() {
		return AbstractForwardAttributeSelectionActivity.FORWARD_ATTRIBUTE_SELECTION_ACTIVITY;
	}

}

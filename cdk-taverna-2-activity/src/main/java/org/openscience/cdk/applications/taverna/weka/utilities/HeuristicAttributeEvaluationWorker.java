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

import java.util.Random;

import org.openscience.cdk.applications.taverna.weka.regression.HeuristicAttributeSelectionActivity;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.filters.Filter;

/**
 * Worker class for the heuristic attribute evaluation activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class HeuristicAttributeEvaluationWorker extends Thread {

	private HeuristicAttributeSelectionActivity owner = null;
	private boolean isDone = false;

	/**
	 * Creates a new instance.
	 */
	public HeuristicAttributeEvaluationWorker(HeuristicAttributeSelectionActivity owner) {
		this.owner = owner;
	}

	@Override
	public void run() {
		WekaTools tools = new WekaTools();
		try {
			Integer idx;
			while ((idx = owner.getWork()) != null) {
				Instances currentSet = this.owner.getCurrentSet();
				// Calculate score
				Instances work = Filter.useFilter(currentSet, tools.getAttributRemover(currentSet, idx + 1));
				Classifier classifier = this.owner.getClassifier();
				Evaluation eval = new Evaluation(work);
				if (this.owner.isUSE_CV()) {
					eval.crossValidateModel(classifier, work, this.owner.getFOLDS(), new Random(1));
				} else {
					classifier.buildClassifier(work);
					eval.evaluateModel(classifier, work);
				}
				double rmse = eval.rootMeanSquaredError();
				this.owner.publishResult(rmse, idx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		isDone = true;
		this.owner.workerDone();
	}

	/**
	 * @return True if worker is ready.
	 */
	public boolean isDone() {
		return isDone;
	}

}

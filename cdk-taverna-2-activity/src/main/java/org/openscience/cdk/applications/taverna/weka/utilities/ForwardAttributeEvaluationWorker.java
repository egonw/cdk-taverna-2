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
import java.util.Random;

import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;

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
public class ForwardAttributeEvaluationWorker extends AbstractAttributeEvaluationWorker {

	/**
	 * Creates a new instance.
	 */
	public ForwardAttributeEvaluationWorker(AbstractForwardAttributeSelectionActivity owner) {
		super(owner);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		WekaTools tools = new WekaTools();
		try {
			ArrayList<Integer> attr;
			while ((attr = (ArrayList<Integer>) this.owner.getWork()) != null) {
				Instances currentSet = this.owner.getCurrentSet();
				int idx = attr.get(attr.size() - 1) - 2;
				Collections.sort(attr);
				// Calculate score
				Instances work = Filter.useFilter(currentSet, tools.getAttributRemover(currentSet, attr, true));
				work = Filter.useFilter(work, tools.getIDRemover(work));
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
			ErrorLogger.getInstance().writeError(e.getMessage(), this.getName(), e);
		}
		isDone = true;
		this.owner.workerDone();
	}

}

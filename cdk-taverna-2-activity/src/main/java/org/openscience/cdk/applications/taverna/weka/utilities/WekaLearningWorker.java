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

import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;

import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * Worker for the weka regression activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class WekaLearningWorker extends Thread {

	private AbstractWekaLearningActivity owner;
	private boolean isDone = false;

	public WekaLearningWorker(AbstractWekaLearningActivity owner) {
		this.owner = owner;
	}

	@Override
	public void run() {
		WekaLearningWork work = null;
		while ((work = this.owner.getWork()) != null) {
			try {
				Instances trainset = work.trainingSet;
				Classifier classifier = work.classifierClass.newInstance();
				classifier.setOptions(work.option.split(" "));
				classifier.buildClassifier(trainset);
				this.owner.publishResult(work, classifier);
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError(CDKTavernaException.CLUSTERING_ERROR,
						this.getClass().getSimpleName(), e);
			}
		}
		this.isDone = true;
		this.owner.workerDone();
	}

	/**
	 * @return True when the worker is done.
	 */
	public boolean isDone() {
		return isDone;
	}
}

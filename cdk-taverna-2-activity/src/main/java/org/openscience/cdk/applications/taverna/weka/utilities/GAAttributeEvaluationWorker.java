package org.openscience.cdk.applications.taverna.weka.utilities;

import org.openscience.cdk.applications.taverna.weka.learning.GAAttributeEvaluationActivity;

import weka.classifiers.Classifier;

public class GAAttributeEvaluationWorker extends Thread {

	private GAAttributeEvaluationActivity owner = null;
	private boolean isDone = false;

	public GAAttributeEvaluationWorker(GAAttributeEvaluationActivity owner) {
		this.owner = owner;
	}

	@Override
	public void run() {
		try {
			GAAttributeEvaluationGenome individual;
			while ((individual = owner.getWork()) != null) {
				// Create individual dataset
				individual.updateDataset();
				// Calculate score
				Classifier classifier = this.owner.getClassifier();
				individual.calculateScore(classifier);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		isDone = true;
		this.owner.workerDone();
	}

	public boolean isDone() {
		return isDone;
	}

}

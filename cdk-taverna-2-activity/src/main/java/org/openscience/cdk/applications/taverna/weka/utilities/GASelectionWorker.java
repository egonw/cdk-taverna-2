package org.openscience.cdk.applications.taverna.weka.utilities;

import org.openscience.cdk.applications.taverna.weka.learning.GAAttributeSelectionActivity;

import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;

public class GASelectionWorker extends Thread {

	private GAAttributeSelectionActivity owner = null;
	private boolean isDone = false;

	public GASelectionWorker(GAAttributeSelectionActivity owner) {
		this.owner = owner;
	}

	@Override
	public void run() {
		try {
			AttributeSelectionGenome individual;
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

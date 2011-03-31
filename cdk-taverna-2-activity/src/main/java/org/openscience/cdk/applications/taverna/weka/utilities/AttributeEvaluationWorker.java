package org.openscience.cdk.applications.taverna.weka.utilities;

import java.util.Random;

import org.openscience.cdk.applications.taverna.weka.learning.AttributeEvaluationActivity;
import org.openscience.cdk.applications.taverna.weka.learning.GAAttributeEvaluationActivity;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.filters.Filter;

public class AttributeEvaluationWorker extends Thread {

	private AttributeEvaluationActivity owner = null;
	private boolean isDone = false;

	public AttributeEvaluationWorker(AttributeEvaluationActivity owner) {
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

	public boolean isDone() {
		return isDone;
	}

}

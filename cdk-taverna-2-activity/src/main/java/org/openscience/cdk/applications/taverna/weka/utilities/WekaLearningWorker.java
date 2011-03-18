package org.openscience.cdk.applications.taverna.weka.utilities;

import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.weka.learning.WekaLearningActivity;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;

public class WekaLearningWorker extends Thread {

	private WekaLearningActivity owner;
	private boolean isDone = false;

	public WekaLearningWorker(WekaLearningActivity owner) {
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

	public boolean isDone() {
		return isDone;
	}
}

package org.openscience.cdk.applications.taverna.weka.utilities;

import java.io.File;

import weka.classifiers.Classifier;
import weka.core.Instances;

public class WekaLearningWork {
	public Class<? extends Classifier> classifierClass;
	public String option;
	public Instances trainingSet;
	public int id;
	public File modelFile;
}

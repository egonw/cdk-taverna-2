package org.openscience.cdk.applications.taverna.weka.utilities;

import java.util.Arrays;

import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.weka.WekaClusteringActivity;

import weka.clusterers.Clusterer;
import weka.core.Instances;
import weka.core.OptionHandler;

public class WekaClusteringWorker extends Thread {

	private String className = null;
	private String[] options = null;
	private Instances dataset = null;
	private WekaClusteringActivity owner = null;
	private boolean done = false;

	public WekaClusteringWorker(WekaClusteringActivity owner, String className, String[] options, Instances dataset) {
		this.owner = owner;
		this.className = className;
		this.options = options;
		this.dataset = dataset;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		Clusterer clusterer = null;
		try {
			Class<? extends Clusterer> clustererClass = (Class<? extends Clusterer>) Class.forName(this.className);
			clusterer = clustererClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String optionString = "";
		try {
			for (String o : this.options) {
				optionString += o;
			}
			this.options = Arrays.copyOfRange(this.options, 0, this.options.length - 2);
			((OptionHandler) clusterer).setOptions(this.options);
			// Do clustering
			clusterer.buildClusterer(dataset);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.CLUSTERING_ERROR, this.getClass().getSimpleName(),
					e);
		} finally {
			done = true;
			this.owner.workerDone(clusterer, optionString);
		}
	}

	public boolean isDone() {
		return done;
	}

}

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
package org.openscience.cdk.applications.taverna.weka.regression;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;
import net.sf.taverna.t2.reference.impl.external.object.InlineStringReference;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.basicutilities.Tools;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaRegressionWork;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaRegressionWorker;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaTools;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;

/**
 * Class which represents the Weka learning activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class WekaRegressionActivity extends AbstractCDKActivity {

	public static final String WEKA_LEARNING_ACTIVITY = "Weka Regression";
	private LinkedList<WekaRegressionWork> workList = null;
	private WekaRegressionWorker[] workers = null;
	private String[] modelFiles = null;

	/**
	 * Creates a new instance.
	 */
	public WekaRegressionActivity() {
		this.INPUT_PORTS = new String[] { "Regression Train Datasets", "File" };
		this.OUTPUT_PORTS = new String[] { "Regression Model Files" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
		List<Class<? extends ExternalReferenceSPI>> expectedReferences = new ArrayList<Class<? extends ExternalReferenceSPI>>();
		expectedReferences.add(FileReference.class);
		expectedReferences.add(InlineStringReference.class);
		addInput(this.INPUT_PORTS[1], 0, false, expectedReferences, null);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void work() throws Exception {
		// Get input
		List<Instances> dataset = this.getInputAsList(this.INPUT_PORTS[0], Instances.class);
		File targetFile = this.getInputAsFile(this.INPUT_PORTS[1]);
		String directory = Tools.getDirectory(targetFile);
		String name = Tools.getFileName(targetFile);
		int numberOfThreads = (Integer) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_NUMBER_OF_USED_THREADS);
		// Do work
		this.workList = new LinkedList<WekaRegressionWork>();
		// Get job informations
		String className = (String) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_LEARNER_NAME);
		String optString = (String) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_LEARNER_OPTIONS);
		String[] options = optString.split(";");
		// Create job list
		int id = 0;
		WekaTools tools = new WekaTools();
		this.modelFiles = new String[options.length * dataset.size()];
		for (int i = 0; i < options.length; i++) {
			for (int j = 0; j < dataset.size(); j++) {
				WekaRegressionWork work = new WekaRegressionWork();
				work.classifierClass = (Class<? extends Classifier>) Class.forName(className);
				work.option = options[i];
				work.trainingSet = Filter.useFilter(dataset.get(j), tools.getIDRemover(dataset.get(j)));
				work.id = id;
				File classifierFile = FileNameGenerator.getNewFile(directory, ".model", name + "_"
						+ work.classifierClass.getSimpleName() + options[i].replaceAll(" ", ""), i + 1);
				work.modelFile = classifierFile;
				this.workList.add(work);
				id++;
			}
		}
		// //start workers
		int numWorkers = numberOfThreads > this.workList.size() ? this.workList.size() : numberOfThreads;
		this.workers = new WekaRegressionWorker[numWorkers];
		for (int i = 0; i < numWorkers; i++) {
			this.workers[i] = new WekaRegressionWorker(this);
			this.workers[i].start();
		}
		synchronized (this) {
			this.wait();
		}
		// Set output
		this.setOutputAsStringList(Arrays.asList(this.modelFiles), this.OUTPUT_PORTS[0]);
	}

	/**
	 * @return A work object or null when finished.
	 */
	public synchronized WekaRegressionWork getWork() {
		if (!this.workList.isEmpty()) {
			return this.workList.removeFirst();
		} else {
			return null;
		}
	}

	/**
	 * @param work
	 *            Processed work object.
	 * @param classifier
	 *            Builded classifier.
	 * @throws Exception
	 */
	public synchronized void publishResult(WekaRegressionWork work, Classifier classifier) throws Exception {
		SerializationHelper.write(work.modelFile.getPath(), classifier);
		this.modelFiles[work.id] = work.modelFile.getPath();
	}

	/**
	 * Final point for the workers.
	 * 
	 * @param results
	 *            Described molecules.
	 */
	public synchronized void workerDone() {
		boolean allDone = true;
		for (WekaRegressionWorker worker : this.workers) {
			if (!worker.isDone()) {
				allDone = false;
				break;
			}
		}
		if (allDone) {
			synchronized (this) {
				this.notify();
			}
		}
	}

	@Override
	public String getActivityName() {
		return WekaRegressionActivity.WEKA_LEARNING_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_NUMBER_OF_USED_THREADS, Runtime.getRuntime().availableProcessors());
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + WekaRegressionActivity.WEKA_LEARNING_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.WEKA_REGRESSION_FOLDER_NAME;
	}

}

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
package org.openscience.cdk.applications.taverna.weka;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;
import net.sf.taverna.t2.reference.impl.external.object.InlineStringReference;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaTools;

import weka.classifiers.Classifier;
import weka.clusterers.Clusterer;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.Utils;
import weka.filters.Filter;

/**
 * Class which represents the Weka prediction activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class WekaPredictionActivity extends AbstractCDKActivity {

	public static final String WEKA_PREDICTION_ACTIVITY = "Weka Prediction";

	/**
	 * Creates a new instance.
	 */
	public WekaPredictionActivity() {
		this.INPUT_PORTS = new String[] { "Weka Prediction Dataset", "Model File" };
		this.OUTPUT_PORTS = new String[] { "ID prediction CSV" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 0, true, null, byte[].class);
		List<Class<? extends ExternalReferenceSPI>> expectedReferences = new ArrayList<Class<? extends ExternalReferenceSPI>>();
		expectedReferences.add(FileReference.class);
		expectedReferences.add(InlineStringReference.class);
		addInput(this.INPUT_PORTS[1], 0, false, expectedReferences, null);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
	}

	@Override
	public void work() throws Exception {
		// Get input
		Instances dataset = this.getInputAsObject(this.INPUT_PORTS[0], Instances.class);
		File modelFile = this.getInputAsFile(this.INPUT_PORTS[1]);
		// Do work
		WekaTools tools = new WekaTools();
		// Prepare data
		Instances uuids = Filter.useFilter(dataset, tools.getIDGetter(dataset));
		dataset = Filter.useFilter(dataset, tools.getIDRemover(dataset));
		ArrayList<String> csv = new ArrayList<String>();
		csv.add("UUID;Prediction;Distribution");
		Object model = SerializationHelper.read(modelFile.getPath());
		if (model instanceof Classifier) {
			// Classify data
			Classifier classifier = (Classifier) model;
			for (int i = 0; i < dataset.numInstances(); i++) {
				String uuid = uuids.instance(i).stringValue(0);
				double predValue = classifier.classifyInstance(dataset.instance(i));
				double[] dist = classifier.distributionForInstance(dataset.instance(i));
				String line = uuid + ";" + predValue + ";" + Utils.arrayToString(dist);
				csv.add(line);
			}
		} else if (model instanceof Clusterer) {
			Clusterer clusterer = (Clusterer) model;
			// Remove class
			if (dataset.classIndex() >= 0) {
				dataset = Filter.useFilter(dataset, tools.getClassRemover(dataset));
				dataset.setClassIndex(-1);
			}
			for (int i = 0; i < dataset.numInstances(); i++) {
				String uuid = uuids.instance(i).stringValue(0);
				int cluster = clusterer.clusterInstance(dataset.instance(i));
				double[] dist = clusterer.distributionForInstance(dataset.instance(i));
				String line = uuid + ";" + cluster + ";" + Utils.arrayToString(dist);
				csv.add(line);
			}
		} else {
			throw new CDKTavernaException(this.getActivityName(), "Unknown model type!");
		}
		// Set output
		this.setOutputAsStringList(csv, this.OUTPUT_PORTS[0]);
	}

	@Override
	public String getActivityName() {
		return WekaPredictionActivity.WEKA_PREDICTION_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_NUMBER_OF_USED_THREADS, Runtime.getRuntime().availableProcessors());
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + WekaPredictionActivity.WEKA_PREDICTION_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.WEKA_FOLDER_NAME;
	}

}

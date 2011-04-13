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
import java.util.HashMap;
import java.util.List;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;
import net.sf.taverna.t2.reference.impl.external.object.InlineStringReference;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaTools;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;

/**
 * Class which represents the Weka regression prediction activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class WekaRegressionPredictionActivity extends AbstractCDKActivity {

	public static final String WEKA_REGRESSION_PREDICTION_ACTIVITY = "Weka Regression Prediction";

	/**
	 * Creates a new instance.
	 */
	public WekaRegressionPredictionActivity() {
		this.INPUT_PORTS = new String[] { "Weka Prediction Dataset", "Regression Model File" };
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
		csv.add("UUID;PredictedValue;");
		// Classify data
		Classifier classifier = (Classifier) SerializationHelper.read(modelFile.getPath());
		for (int i = 0; i < dataset.numInstances(); i++) {
			String uuid = uuids.instance(i).stringValue(0);
			double predValue = classifier.distributionForInstance(dataset.instance(i))[0];
			String line = uuid + ";" + predValue + ";";
			csv.add(line);
		}
		// Set output
		this.setOutputAsStringList(csv, this.OUTPUT_PORTS[0]);
	}

	@Override
	public String getActivityName() {
		return WekaRegressionPredictionActivity.WEKA_REGRESSION_PREDICTION_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_NUMBER_OF_USED_THREADS, Runtime.getRuntime().availableProcessors());
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + WekaRegressionPredictionActivity.WEKA_REGRESSION_PREDICTION_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.WEKA_REGRESSION_FOLDER_NAME;
	}

}

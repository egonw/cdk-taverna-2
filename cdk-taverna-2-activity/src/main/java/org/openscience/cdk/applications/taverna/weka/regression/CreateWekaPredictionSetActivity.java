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

import java.util.HashMap;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Class which represents the create Weka prediction dataset activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class CreateWekaPredictionSetActivity extends AbstractCDKActivity {

	public static final String CREATE_WEKA_REGRESSION_DATASET_ACTIVITY = "Create Weka Prediction Dataset";

	/**
	 * Creates a new instance.
	 */
	public CreateWekaPredictionSetActivity() {
		this.INPUT_PORTS = new String[] { "Weka Dataset", "Regression Train Dataset" };
		this.OUTPUT_PORTS = new String[] { "Weka Prediction Dataset" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 0, true, null, byte[].class);
		addInput(this.INPUT_PORTS[1], 0, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 0);
	}

	@Override
	public void work() throws Exception {
		// Get input
		Instances dataset = this.getInputAsObject(this.INPUT_PORTS[0], Instances.class);
		Instances regressionSet = this.getInputAsObject(this.INPUT_PORTS[1], Instances.class);
		// Do work
		try {
			// Build index/attribute name map
			HashMap<String, Integer> nameIdxMap = new HashMap<String, Integer>();
			for (int i = 0; i < dataset.numAttributes(); i++) {
				nameIdxMap.put(dataset.attribute(i).name(), i);
			}
			// Fill the regression set
			for (int i = 0; i < dataset.numInstances(); i++) {
				double[] values = new double[regressionSet.numAttributes()];
				for (int j = 0; j < regressionSet.numAttributes(); j++) {
					if(j == regressionSet.classIndex()) {
						continue;
					}
					Attribute attribute = regressionSet.attribute(j);
					Integer idx = nameIdxMap.get(attribute.name());
					if (idx == null) {
						throw new CDKTavernaException(this.getActivityName(),
								CDKTavernaException.DATASETS_ARE_NOT_COMPATIBLE);
					}
					if (attribute.isString()) {
						String s = dataset.instance(i).stringValue(j);
						values[j] = regressionSet.attribute(j).addStringValue(s);
					} else if (attribute.isNumeric()) {
						double v = dataset.instance(i).value(j);
						values[j] = v;
					} else {
						throw new CDKTavernaException(this.getActivityName(),
								CDKTavernaException.DATASETS_ARE_NOT_COMPATIBLE);
					}
				}
				Instance instance = new Instance(1.0, values);
				regressionSet.add(instance);
			}
			// Try to fill it again
		} catch (Exception e) {
			ErrorLogger.getInstance()
					.writeError("Error during regression dataset creation!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		// Set output
		this.setOutputAsObject(regressionSet, this.OUTPUT_PORTS[0]);
	}

	@Override
	public String getActivityName() {
		return CreateWekaPredictionSetActivity.CREATE_WEKA_REGRESSION_DATASET_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + CreateWekaPredictionSetActivity.CREATE_WEKA_REGRESSION_DATASET_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.WEKA_REGRESSION_FOLDER_NAME;
	}

}

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;

import weka.clusterers.ClusterEvaluation;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Class which represents the convert Weka dataset activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class ConvertWekaDatasetActivity extends AbstractCDKActivity {

	public static final String CONVERT_WEKA_DATASET_ACTIVITY = "Convert Weka Dataset";

	/**
	 * Creates a new instance.
	 */
	public ConvertWekaDatasetActivity() {
		this.INPUT_PORTS = new String[] { "Weka Datasets", "Weka Base Dataset" };
		this.OUTPUT_PORTS = new String[] { "Converted Weka Datasets" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
		addInput(this.INPUT_PORTS[1], 0, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
	}

	@Override
	public void work() throws Exception {
		// Get input
		List<Instances> datasets = this.getInputAsList(this.INPUT_PORTS[0], Instances.class);
		Instances baseSet = this.getInputAsObject(this.INPUT_PORTS[1], Instances.class);
		// Do work
		ArrayList<Instances> results = new ArrayList<Instances>();
		for (Instances dataset : datasets) {
			try {
				// Build index/attribute name map
				HashMap<String, Integer> nameIdxMap = new HashMap<String, Integer>();
				for (int i = 0; i < dataset.numAttributes(); i++) {
					nameIdxMap.put(dataset.attribute(i).name(), i);
				}
				Instances resultSet = new Instances(baseSet, dataset.numInstances());
				// Fill the result set
				for (int i = 0; i < dataset.numInstances(); i++) {
					double[] values = new double[resultSet.numAttributes()];
					for (int j = 0; j < resultSet.numAttributes(); j++) {
						Attribute attribute = resultSet.attribute(j);
						Integer idx = nameIdxMap.get(attribute.name());
						if (idx == null) {
							throw new CDKTavernaException(this.getActivityName(),
									CDKTavernaException.DATASETS_ARE_NOT_COMPATIBLE);
						}
						if (attribute.isString()) {
							String s = dataset.instance(i).stringValue(j);
							values[j] = resultSet.attribute(j).addStringValue(s);
						} else if (attribute.isNumeric() || attribute.isNominal()) {
							double v = dataset.instance(i).value(j);
							values[j] = v;
						} else {
							throw new CDKTavernaException(this.getActivityName(),
									CDKTavernaException.DATASETS_ARE_NOT_COMPATIBLE);
						}
					}
					Instance instance = new Instance(1.0, values);
					resultSet.add(instance);
				}
				results.add(resultSet);
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError(CDKTavernaException.DATASET_CONVERSION_ERROR,
						this.getActivityName(), e);
			}
		}
		if (results.isEmpty()) {
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.DATASET_CONVERSION_ERROR);
		}
		// Set output
		this.setOutputAsObjectList(results, this.OUTPUT_PORTS[0]);
	}

	@Override
	public String getActivityName() {
		return ConvertWekaDatasetActivity.CONVERT_WEKA_DATASET_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + ConvertWekaDatasetActivity.CONVERT_WEKA_DATASET_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.WEKA_FOLDER_NAME;
	}

}

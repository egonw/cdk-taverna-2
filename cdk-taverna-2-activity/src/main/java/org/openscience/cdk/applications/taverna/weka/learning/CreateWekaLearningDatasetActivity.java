/*
 * Copyright (C) 2010 by Andreas Truszkowski <ATruszkowski@gmx.de>
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
package org.openscience.cdk.applications.taverna.weka.learning;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.openscience.cdk.applications.art2aclassification.FingerprintItem;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Class which represents the create Weka learning dataset activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class CreateWekaLearningDatasetActivity extends AbstractCDKActivity {

	public static final String CREATE_WEKA_LEARNING_DATASET_ACTIVITY = "Create Weka Learning Dataset";

	/**
	 * Creates a new instance.
	 */
	public CreateWekaLearningDatasetActivity() {
		this.INPUT_PORTS = new String[] { "Weka Dataset", "ID Class CSV" };
		this.OUTPUT_PORTS = new String[] { "Weka Learning Dataset" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 0, true, null, byte[].class);
		addInput(this.INPUT_PORTS[1], 1, true, null, String.class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 0);
	}

	@Override
	public void work() throws Exception {
		// Get input
		Instances dataset = this.getInputAsObject(this.INPUT_PORTS[0], Instances.class);
		List<String> csv = this.getInputAsList(this.INPUT_PORTS[1], String.class);
		// Do work
		HashMap<UUID, Double> classMap = new HashMap<UUID, Double>();
		for (int i = 1; i < csv.size(); i++) {
			String[] frag = csv.get(i).split(";");
			classMap.put(UUID.fromString(frag[0]), Double.valueOf(frag[1]));
		}
		Instances learningSet = null;
		try {
			FastVector attributes = new FastVector();
			for (int i = 0; i < dataset.numAttributes(); i++) {
				attributes.addElement(dataset.attribute(i));
			}
			attributes.addElement(new Attribute("Class"));
			learningSet = new Instances("LearningSet", attributes, dataset.numInstances());
			learningSet.setClassIndex(learningSet.numAttributes() - 1);
			for (int i = 0; i < dataset.numInstances(); i++) {
				Instance instance = dataset.instance(i);
				UUID uuid = UUID.fromString(instance.stringValue(0));
				double[] values = new double[learningSet.numAttributes()];
				values[0] = learningSet.attribute(0).addStringValue(instance.stringValue(0));
				for (int j = 1; j < instance.numAttributes(); j++) {
					values[j] = instance.value(j);
				}
				Instance inst = new Instance(1.0, values);
				inst.setDataset(learningSet);
				inst.setClassValue(classMap.get(uuid));
				learningSet.add(inst);
			}
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error during learning dataset creation!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		// Set output
		this.setOutputAsObject(learningSet, this.OUTPUT_PORTS[0]);
	}

	@Override
	public String getActivityName() {
		return CreateWekaLearningDatasetActivity.CREATE_WEKA_LEARNING_DATASET_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + CreateWekaLearningDatasetActivity.CREATE_WEKA_LEARNING_DATASET_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.WEKA_LEARNING_FOLDER_NAME;
	}

}

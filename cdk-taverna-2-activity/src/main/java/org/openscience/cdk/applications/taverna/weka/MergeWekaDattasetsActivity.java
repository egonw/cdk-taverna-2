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
package org.openscience.cdk.applications.taverna.weka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.interfaces.IPortNumber;
import org.openscience.cdk.applications.taverna.qsar.utilities.QSARVectorUtility;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Class which represents the merge QSAR vectors activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class MergeWekaDattasetsActivity extends AbstractCDKActivity implements IPortNumber {

	public static final String MERGE_QSAR_VECTORS_ACTIVITY = "Merge Weka Datasets";

	/**
	 * Creates a new instance.
	 */
	public MergeWekaDattasetsActivity() {
		this.INPUT_PORTS = new String[] { "Weka Dataset", "Name" };
		this.OUTPUT_PORTS = new String[] { "Merged Weka Dataset", "Relations Table" };
	}

	@Override
	protected void addInputPorts() {
		int numberOfPorts = (Integer) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_NUMBER_OF_PORTS);
		for (int i = 1; i <= numberOfPorts; i++) {
			addInput(this.INPUT_PORTS[0] + "_" + i, 0, true, null, byte[].class);
			addInput(this.INPUT_PORTS[1] + "_" + i, 0, true, null, String.class);
		}
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 0);
		addOutput(this.OUTPUT_PORTS[1], 1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void work() throws Exception {
		// Get input
		int numberOfPorts = (Integer) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_NUMBER_OF_PORTS);
		String names[] = new String[numberOfPorts];
		Instances[] datasets = new Instances[numberOfPorts];
		for (int i = 1; i <= numberOfPorts; i++) {
			datasets[i - 1] = this.getInputAsObject(this.INPUT_PORTS[0] + "_" + i, Instances.class);
			names[i - 1] = this.getInputAsObject(this.INPUT_PORTS[1] + "_" + i, String.class);
		}
		// Do work
		QSARVectorUtility qsarVectorUtility = new QSARVectorUtility();
		// Create descriptor names list
		int dataSize = 0;
		List<List<String>> descriptorNamesList = new ArrayList<List<String>>();
		for (int i = 0; i < numberOfPorts; i++) {
			ArrayList<String> descriptorNames = new ArrayList<String>();
			for (int j = 0; j < datasets[i].numAttributes(); j++) {
				descriptorNames.add(datasets[i].attribute(j).name());
			}
			descriptorNamesList.add(descriptorNames);
			dataSize += datasets[i].numInstances();
		}
		// Create minimum set of the descriptor names
		ArrayList<String> mergedDescriptorNames = qsarVectorUtility
				.createMinimumDescriptorNamesList(descriptorNamesList);
		// Create merged dataset
		FastVector attributes = new FastVector(mergedDescriptorNames.size());
		Attribute idAttr = new Attribute("ID", (FastVector) null);
		attributes.addElement(idAttr);
		for (int i = 1; i < mergedDescriptorNames.size(); i++) {
			attributes.addElement(new Attribute(mergedDescriptorNames.get(i)));
		}
		Instances dataset = new Instances("Weka Dataset", attributes, dataSize);
		for (int i = 0; i < numberOfPorts; i++) {
			for (int j = 0; j < datasets[i].numInstances(); j++) {
				double[] values = new double[dataset.numAttributes()];
				String uuid = datasets[i].instance(j).stringValue(0);
				values[0] = dataset.attribute(0).addStringValue(uuid);
				for (int k = 1; k < mergedDescriptorNames.size(); k++) {
					String name = mergedDescriptorNames.get(k);
					for (int l = 0; l < datasets[i].numAttributes(); l++) {
						if (name.equals(datasets[i].attribute(l).name())) {
							values[k] = datasets[i].instance(j).value(l);
						}
					}
				}
				Instance inst = new Instance(1.0, values);
				dataset.add(inst);
			}
		}
		// Create id relation table
		ArrayList<String> idTable = new ArrayList<String>();
		for (int i = 0; i < numberOfPorts; i++) {
			String name = names[i];
			String line = "> <NAME> " + name;
			idTable.add(line);
			for (int j = 0; j < datasets[i].numInstances(); j++) {
				line = "> <ENTRY> " + datasets[i].instance(j).stringValue(0);
				idTable.add(line);
			}
		}
		// Set output
		this.setOutputAsObject(dataset, this.OUTPUT_PORTS[0]);
		this.setOutputAsStringList(idTable, this.OUTPUT_PORTS[1]);
	}

	@Override
	public String getActivityName() {
		return MergeWekaDattasetsActivity.MERGE_QSAR_VECTORS_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_NUMBER_OF_PORTS, 2);
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + MergeWekaDattasetsActivity.MERGE_QSAR_VECTORS_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.WEKA_FOLDER_NAME;
	}

}

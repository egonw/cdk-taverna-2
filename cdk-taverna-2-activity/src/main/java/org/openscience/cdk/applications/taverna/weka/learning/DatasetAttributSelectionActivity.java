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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaTools;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.clusterers.EM;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * Class which represents the dataset attribut selection activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class DatasetAttributSelectionActivity extends AbstractCDKActivity {

	//public static final String[] METHODS = new String[] { "Random", "ClusterRepresentatives", "SimpleGlobalMax" };
	public static final String DATASET_ATTRIBUT_SELECTION_ACTIVITY = "Dataset Attribut Selection";

	/**
	 * Creates a new instance.
	 */
	public DatasetAttributSelectionActivity() {
		this.INPUT_PORTS = new String[] { "Weka Train Datasets", "Weka Test Datasets" };
		this.OUTPUT_PORTS = new String[] { "Weka Train Datasets", "Weka Test Datasets", "Attribut Info" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
		addInput(this.INPUT_PORTS[1], 1, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
		addOutput(this.OUTPUT_PORTS[1], 1);
		addOutput(this.OUTPUT_PORTS[2], 1);
	}

	@Override
	public void work() throws Exception {
		WekaTools tools = new WekaTools();
		// Get input
		List<Instances> trainsets = this.getInputAsList(this.INPUT_PORTS[0], Instances.class);
		List<Instances> testsets = this.getInputAsList(this.INPUT_PORTS[1], Instances.class);
		// Do work
		ArrayList<Instances> newTrainSets = new ArrayList<Instances>();
		ArrayList<Instances> newTestSets = new ArrayList<Instances>();
		Instances trainset = trainsets.get(0);
		Instances testset = testsets.get(0);
		ArrayList<String> attrInfos = new ArrayList<String>();
		String info = "Set 1 - Removed Attribut: --------";
		newTrainSets.add(trainset);
		newTestSets.add(testset);
		attrInfos.add(info);
		for(int i = 2; i < trainset.numAttributes() - 1; i++) {
			info = "Set " + i + " - Removed Attribut: " + trainset.attribute(i).name();
			newTrainSets.add(Filter.useFilter(trainset, tools.getAttributRemover(trainset, i)));
			newTestSets.add(Filter.useFilter(testset, tools.getAttributRemover(testset, i)));
			attrInfos.add(info);
		}
		// Set output
		this.setOutputAsObjectList(newTrainSets, this.OUTPUT_PORTS[0]);
		this.setOutputAsObjectList(newTestSets, this.OUTPUT_PORTS[1]);
		this.setOutputAsStringList(attrInfos, this.OUTPUT_PORTS[2]);
	}

	@Override
	public String getActivityName() {
		return DatasetAttributSelectionActivity.DATASET_ATTRIBUT_SELECTION_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + DatasetAttributSelectionActivity.DATASET_ATTRIBUT_SELECTION_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.WEKA_LEARNING_FOLDER_NAME;
	}

}

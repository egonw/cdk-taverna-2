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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;
import net.sf.taverna.t2.reference.impl.external.object.InlineStringReference;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.basicutilities.Tools;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaTools;

import weka.classifiers.Classifier;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.trees.M5P;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;

/**
 * Class which represents the Weka learning activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class WekaLearningActivity extends AbstractCDKActivity {

	public static final String WEKA_LEARNING_ACTIVITY = "Weka Learning";

	/**
	 * Creates a new instance.
	 */
	public WekaLearningActivity() {
		this.INPUT_PORTS = new String[] { "Weka Train Datasets", "File" };
		this.OUTPUT_PORTS = new String[] { "Models Files", "Train Data Files" };
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
		addOutput(this.OUTPUT_PORTS[1], 1);
	}

	@Override
	public void work() throws Exception {
		// Get input
		List<Instances> dataset = this.getInputAsList(this.INPUT_PORTS[0], Instances.class);
		File targetFile = this.getInputAsFile(this.INPUT_PORTS[1]);
		String directory = Tools.getDirectory(targetFile);
		String name = Tools.getFileName(targetFile);
		// Do work 0
		Classifier[] classifiers = new Classifier[] { new LinearRegression(), new M5P(), new MultilayerPerceptron(),
				new LibSVM(), new LibSVM() };
		String[] options = new String[] { "", "", "", "-S 3 -K 3", "-S 4 -K 3" };
		ArrayList<String> modelFiles = new ArrayList<String>();
		ArrayList<String> dataFiles = new ArrayList<String>();
		WekaTools tools = new WekaTools();
		for (int i = 0; i < dataset.size(); i++) {
			Instances trainset = dataset.get(i);
			File dataFile = FileNameGenerator.getNewFile(directory, ".data", name + "_set");
			SerializationHelper.write(dataFile.getPath(), trainset);
			dataFiles.add(dataFile.getPath());
			trainset = Filter.useFilter(trainset, tools.getIDRemover(trainset));
			Classifier classifier = classifiers[1];
			classifier.setOptions(options[1].split(" "));
			classifier.buildClassifier(trainset);
			File classifierFile = FileNameGenerator.getNewFile(directory, ".model", name + "_"
					+ classifier.getClass().getSimpleName());
			SerializationHelper.write(classifierFile.getPath(), classifier);
			modelFiles.add(classifierFile.getPath());
		}
		// Set output
		this.setOutputAsStringList(modelFiles, this.OUTPUT_PORTS[0]);
		this.setOutputAsStringList(dataFiles, this.OUTPUT_PORTS[1]);
	}

	@Override
	public String getActivityName() {
		return WekaLearningActivity.WEKA_LEARNING_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + WekaLearningActivity.WEKA_LEARNING_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.WEKA_LEARNING_FOLDER_NAME;
	}

}

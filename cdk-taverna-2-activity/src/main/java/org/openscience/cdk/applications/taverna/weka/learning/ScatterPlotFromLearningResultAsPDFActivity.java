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
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;
import net.sf.taverna.t2.reference.impl.external.object.InlineStringReference;

import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYSeries;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.basicutilities.ChartTool;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaTools;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;

/**
 * Class which implements the extract learning result as pdf activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class ScatterPlotFromLearningResultAsPDFActivity extends AbstractCDKActivity {

	public static final String SCATTER_PLOT_FROM_LEARNING_RESULT_AS_PDF_ACTIVITY = "Scatter Plot From Learning Result As PDF";

	/**
	 * Creates a new instance.
	 */
	public ScatterPlotFromLearningResultAsPDFActivity() {
		this.INPUT_PORTS = new String[] { "Model File", "Weka Dataset", "ID Class CSV" };
		this.OUTPUT_PORTS = new String[] { "Files" };
	}

	@Override
	protected void addInputPorts() {
		List<Class<? extends ExternalReferenceSPI>> expectedReferences = new ArrayList<Class<? extends ExternalReferenceSPI>>();
		expectedReferences.add(FileReference.class);
		expectedReferences.add(InlineStringReference.class);
		addInput(this.INPUT_PORTS[0], 0, false, expectedReferences, null);
		addInput(this.INPUT_PORTS[1], 0, true, null, byte[].class);
		addInput(this.INPUT_PORTS[2], 1, true, null, String.class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
	}

	@Override
	public void work() throws Exception {
		// Get input
		File targetFile = this.getInputAsFile(this.INPUT_PORTS[0]);
		Instances dataset = this.getInputAsObject(this.INPUT_PORTS[1], Instances.class);
		List<String> csv = this.getInputAsList(this.INPUT_PORTS[2], String.class);
		// Do work
		ArrayList<String> resultFiles = new ArrayList<String>();
		try {
			HashMap<UUID, Double> orgClassMap = new HashMap<UUID, Double>();
			HashMap<UUID, Double> calcClassMap = new HashMap<UUID, Double>();
			for (int i = 1; i < csv.size(); i++) {
				String[] frag = csv.get(i).split(";");
				orgClassMap.put(UUID.fromString(frag[0]), Double.valueOf(frag[1]));
			}
			Classifier classifier = (Classifier) SerializationHelper.read(targetFile.getPath());
			WekaTools tools = new WekaTools();
			Instances uuidSet = Filter.useFilter(dataset, tools.getIDGetter(dataset));
			dataset = Filter.useFilter(dataset, tools.getIDRemover(dataset));
			for (int i = 0; i < dataset.numInstances(); i++) {
				UUID uuid = UUID.fromString(uuidSet.instance(i).stringValue(0));
				calcClassMap.put(uuid, classifier.classifyInstance(dataset.instance(i)));
			}
			List<JFreeChart> charts = new LinkedList<JFreeChart>();
			ChartTool chartTool = new ChartTool();
			DefaultXYDataset xyDataSet = new DefaultXYDataset();
			XYSeries series = new XYSeries("Result");
			for (int i = 0; i < uuidSet.numInstances(); i++) {
				UUID uuid = UUID.fromString(uuidSet.instance(i).stringValue(0));
				double org = orgClassMap.get(uuid);
				double calc = calcClassMap.get(uuid);
				series.add(org, calc);
			}
			xyDataSet.addSeries("Result", series.toArray());
			charts.add(chartTool.createScatterPlot(xyDataSet));
			File file = FileNameGenerator.getNewFile(targetFile.getParent(), ".pdf", "ScatterPlot");
			chartTool.writeChartAsPDF(file, charts);
			resultFiles.add(file.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Set output
		this.setOutputAsStringList(resultFiles, this.OUTPUT_PORTS[0]);
	}

	@Override
	public String getActivityName() {
		return ScatterPlotFromLearningResultAsPDFActivity.SCATTER_PLOT_FROM_LEARNING_RESULT_AS_PDF_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + ScatterPlotFromLearningResultAsPDFActivity.SCATTER_PLOT_FROM_LEARNING_RESULT_AS_PDF_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.WEKA_LEARNING_FOLDER_NAME;
	}
}

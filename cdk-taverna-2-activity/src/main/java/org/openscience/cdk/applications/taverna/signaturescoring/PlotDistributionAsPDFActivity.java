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
package org.openscience.cdk.applications.taverna.signaturescoring;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;
import net.sf.taverna.t2.reference.impl.external.object.InlineStringReference;

import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ChartTool;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.basicutilities.Tools;

/**
 * Class which represents the CSV file writer activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class PlotDistributionAsPDFActivity extends AbstractCDKActivity {

	public static final String PLOT_DISTRIBUTION_AS_PDF_ACTIVITY = "Plot Distribution As PDF";

	/**
	 * Creates a new instance.
	 */
	public PlotDistributionAsPDFActivity() {
		this.INPUT_PORTS = new String[] { "ID Score CSV Files", "File" };
		this.OUTPUT_PORTS = new String[] { "Files" };
	}

	@Override
	protected void addInputPorts() {
		List<Class<? extends ExternalReferenceSPI>> expectedReferences = new ArrayList<Class<? extends ExternalReferenceSPI>>();
		expectedReferences.add(FileReference.class);
		expectedReferences.add(InlineStringReference.class);
		addInput(this.INPUT_PORTS[0], 1, false, expectedReferences, null);
		expectedReferences = new ArrayList<Class<? extends ExternalReferenceSPI>>();
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
		List<File> files = this.getInputAsFileList(this.INPUT_PORTS[0]);
		File targetFile = this.getInputAsFile(this.INPUT_PORTS[1]);
		String directory = Tools.getDirectory(targetFile);
		String name = Tools.getFileName(targetFile);
		// Do work
		List<String> resultFiles = new ArrayList<String>();
		ChartTool chartTool = new ChartTool();
		List<Object> chartObjects = new LinkedList<Object>();
		HistogramDataset allHistoSet = new HistogramDataset();
		allHistoSet.setType(HistogramType.SCALE_AREA_TO_1);
		for (File file : files) {
			try {
				ArrayList<Double> values = new ArrayList<Double>();
				HistogramDataset histoSet = new HistogramDataset();
				histoSet.setType(HistogramType.SCALE_AREA_TO_1);
				LineNumberReader reader = new LineNumberReader(new FileReader(file));
				// First line line contains the header
				String line = reader.readLine();
				if (line.contains("Distribution")) {
					while ((line = reader.readLine()) != null) {
						line = line.replaceAll("\"", "");
						String[] parts = line.split(";");
						String[] dist = parts[2].split(",");
						double value;
						if (dist.length > 1) {
							value = Double.parseDouble(dist[1]);
							value = Math.pow(value * 2 - 1, 7);
						} else {
							value = Double.parseDouble(dist[0]);
						}
						values.add(value);
					}
				} else {
					while ((line = reader.readLine()) != null) {
						line = line.replaceAll("\"", "");
						String[] parts = line.split(";");
						values.add(Double.parseDouble(parts[1]));
					}
				}
				double[] v = new double[values.size()];
				for (int i = 0; i < values.size(); i++) {
					v[i] = values.get(i).doubleValue();
				}
				int bins = (int) (v.length * 0.05);
				// Limit number of bins to 30
				bins = bins > 30 ? 30 : bins;
				bins = bins < 15 ? 15 : bins;
				histoSet.addSeries(file.getName(), v, bins);
				allHistoSet.addSeries(file.getName(), v, bins);
				chartObjects.add(chartTool.createXYLineChart("Score Distribution", "Score", "Frequency (Area scaled to 1)",
						histoSet, true, false));
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError(CDKTavernaException.WRITE_FILE_ERROR + file.getPath() + "!",
						this.getActivityName(), e);
				throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.WRITE_FILE_ERROR + file.getPath() + "!");
			}
		}
		chartObjects.add(chartTool.createXYLineSplineChart("Score Distribution", "Score", "Frequency (Area scaled to 1)",
				allHistoSet, true, false));
		File file = FileNameGenerator.getNewFile(directory, ".pdf", "Distribution_" + name);
		chartTool.writeChartAsPDF(file, chartObjects);
		resultFiles.add(file.getPath());
		// Set output
		this.setOutputAsStringList(resultFiles, this.OUTPUT_PORTS[0]);
	}

	@Override
	public String getActivityName() {
		return PlotDistributionAsPDFActivity.PLOT_DISTRIBUTION_AS_PDF_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + PlotDistributionAsPDFActivity.PLOT_DISTRIBUTION_AS_PDF_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.SIGNATURE_SCORING_FOLDER_NAME;
	}
}

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
package org.openscience.cdk.applications.taverna.clustering.art2a.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamReader;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;
import net.sf.taverna.t2.reference.impl.external.object.InlineStringReference;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.openscience.cdk.applications.art2aclassification.Art2aClassificator;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ChartTool;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.io.XMLFileIO;

/**
 * Class which represents the ART-2a result to PDF activity. Reads ART-2a result
 * files from hard disk.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class ART2aResultAsPDFFileReader extends AbstractCDKActivity {

	public static final String ART2A_RESULT_AS_PDF_FILE_READER_ACTIVITY = "ART-2a Result As PDF File Reader";

	/**
	 * Creates a new instance.
	 */
	public ART2aResultAsPDFFileReader() {
		this.INPUT_PORTS = new String[] { "Files" }; 
		this.OUTPUT_PORTS = new String[] { "Files" };
	}

	@Override
	protected void addInputPorts() {
		List<Class<? extends ExternalReferenceSPI>> expectedReferences = new ArrayList<Class<? extends ExternalReferenceSPI>>();
		expectedReferences.add(FileReference.class);
		expectedReferences.add(InlineStringReference.class);
		addInput(this.INPUT_PORTS[0], 1, false, expectedReferences, null);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
	}

	@Override
	public void work() throws Exception {
		// Get input
		List<File> files = this.getInputAsFileList(this.INPUT_PORTS[0]);
		// Do work
		List<String> resultFileNames = new ArrayList<String>();
		List<String> pdfTitle = new ArrayList<String>();
		try {
			XMLStreamReader xmlReader;
			StringBuffer buffer;
			Art2aClassificator classificator;
			XMLFileIO xmlFileIO = new XMLFileIO();
			ChartTool chartTool = new ChartTool();
			int classNumberWithMaxOfVectors = 0;
			int maxVectorsInClass = 0;
			ArrayList<Object> charts = new ArrayList<Object>();
			TreeMap<Double, Integer> treeMap = new TreeMap<Double, Integer>();
			Map.Entry<Double, Integer> entry;
			DefaultCategoryDataset dataSet;
			String numberOfVectorsInClass = "Number of vectors in class";
			pdfTitle.add("This file contains charts from the ART2A Classification");
			pdfTitle.add("The header of each chart contains the following informations; ");
			pdfTitle.add("(Filename/Vigilance Parameter/Number of detected classes/Number of epochs)");
			for (File file : files) {
				String fileName = file.getPath();
				xmlReader = xmlFileIO.getXMLStreamReaderWithCompression(fileName);
				xmlReader.next();
				classificator = new Art2aClassificator(xmlReader, true);
				xmlReader.close();
				xmlFileIO.closeXMLStreamReader();
				classNumberWithMaxOfVectors = 0;
				maxVectorsInClass = 0;
				for (int i = 0; i < classificator.getNumberOfDetectedClasses(); i++) {
					if (maxVectorsInClass < classificator.getNumberOfVectorsInClass(i)) {
						maxVectorsInClass = classificator.getNumberOfVectorsInClass(i);
						classNumberWithMaxOfVectors = i;
					}
				}
				treeMap.clear();
				for (int j = 0; j < classificator.getNumberOfDetectedClasses(); j++) {
					treeMap.put(classificator.getInterAngleOfClasses(classNumberWithMaxOfVectors, j), j);
				}
				dataSet = new DefaultCategoryDataset();
				while (treeMap.size() > 0) {
					entry = treeMap.pollFirstEntry();
					buffer = new StringBuffer();
					buffer.append("(");
					buffer.append(entry.getValue());
					buffer.append("/");
					buffer.append(classificator.getNumberOfVectorsInClass(entry.getValue()));
					buffer.append("/");
					buffer.append(entry.getKey().intValue());
					buffer.append(")");
					dataSet.addValue(classificator.getNumberOfVectorsInClass(entry.getValue()), numberOfVectorsInClass,
							buffer.toString());
				}
				buffer = new StringBuffer();
				buffer.append("(");
				buffer.append(fileName);
				buffer.append("/");
				buffer.append(classificator.getVigilanceParameter());
				buffer.append("/");
				buffer.append(classificator.getNumberOfDetectedClasses());
				buffer.append("/");
				buffer.append(classificator.getNumberOfEpochs());
				buffer.append(")");
				charts.add(chartTool.createBarChart(buffer.toString(), "(Class number/Number Of Vectors/Interangle)",
						"Number Of Vectors", dataSet));
			}
			File file = FileNameGenerator.getNewFile(files.get(0).getParent(), ".pdf", "Art2aClassificationResult");
			chartTool.writeChartAsPDF(file, charts);
			resultFileNames.add(file.getAbsolutePath());
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.PROCESS_ART2A_RESULT_ERROR,
					this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.PROCESS_ART2A_RESULT_ERROR);
		}
		// Set output 
		this.setOutputAsStringList(resultFileNames, this.OUTPUT_PORTS[0]);
	}

	@Override
	public String getActivityName() {
		return ART2aResultAsPDFFileReader.ART2A_RESULT_AS_PDF_FILE_READER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION, ".art2a");
		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION_DESCRIPTION, "ART-2a result file");
		properties.put(CDKTavernaConstants.PROPERTY_SUPPORT_MULTI_FILE, true);
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + ART2aResultAsPDFFileReader.ART2A_RESULT_AS_PDF_FILE_READER_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.ART2A_FOLDER_NAME;
	}

}

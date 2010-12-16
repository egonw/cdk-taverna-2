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
package org.openscience.cdk.applications.taverna.classification.art2a.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.stream.XMLStreamReader;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.jfree.chart.plot.PlotOrientation;
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
 * Class which represents the the ART-2a result considering different origins to PDF activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class ART2aResultConsideringDifferentOriginsAsPDF extends AbstractCDKActivity {

	public static final String ART2A_RESULT_CONSIDERING_DIFFERENT_ORIGINS_AS_PDF_ACTIVITY = "ART-2a Result Considering Different Origins As PDF";

	/**
	 * Creates a new instance.
	 */
	public ART2aResultConsideringDifferentOriginsAsPDF() {
		this.INPUT_PORTS = new String[] { "ART-2a Files", "Relations Table" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, String.class);
		addInput(this.INPUT_PORTS[1], 1, true, null, String.class);
	}

	@Override
	protected void addOutputPorts() {
		// empty
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		List<String> resultFileNames = new ArrayList<String>();
		List<String> pdfTitle = new ArrayList<String>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		List<String> files = (List<String>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), String.class,
				context);
		if (files == null || files.size() == 0) {
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.NO_FILE_CHOSEN);
		}
		ArrayList<String> relationTable = (ArrayList<String>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[1]),
				String.class, context);
		// Prepare relation table data
		ArrayList<String> subjectNames = new ArrayList<String>();
		HashMap<String, Integer> foundSubjectsTable;
		HashMap<String, Integer> numberOfSubjectsInTable = new HashMap<String, Integer>();
		HashMap<UUID, String> subjectTable = new HashMap<UUID, String>();
		String currentName = null;
		String uuidString = null;
		// Read relation table
		for (String entry : relationTable) {
			if (entry.startsWith("> <NAME> ")) {
				currentName = entry.replaceAll("> <NAME> ", "");
				subjectNames.add(currentName);
			} else if (entry.startsWith("> <ENTRY> ")) {
				uuidString = entry.replaceAll("> <ENTRY> ", "");
				UUID uuid = UUID.fromString(uuidString);
				subjectTable.put(uuid, currentName);
				Integer value = numberOfSubjectsInTable.get(currentName);
				if (value == null) {
					value = 1;
				} else {
					value++;
				}
				numberOfSubjectsInTable.put(currentName, value);
			}
		}
		try {
			XMLStreamReader xmlReader;
			Art2aClassificator classificator;
			XMLFileIO xmlFileIO = new XMLFileIO();
			ChartTool chartTool = new ChartTool();
			chartTool.setBarChartHeight(500);
			chartTool.setBarChartWidth(840);
			chartTool.setPlotOrientation(PlotOrientation.VERTICAL);
			chartTool.setDescriptionYAxis("Ratio in percent");
			chartTool.setDescriptionXAxis("(Class number/Number of Vectors/Interangle)");
			chartTool.setRenderXAxisDescriptionDiagonal(true);
			ArrayList<File> tempFileList = new ArrayList<File>();
			pdfTitle.add("This file contains charts from the ART2A Classification");
			pdfTitle.add("The header of each chart contains the following informations: ");
			pdfTitle.add("(Filename/Vigilance Parameter/Number of detected classes/Number of epochs)");
			for (String fileName : files) {
				try {
					xmlReader = xmlFileIO.getXMLStreamReaderWithCompression(fileName);
					xmlReader.next();
					classificator = new Art2aClassificator(xmlReader, true);
					xmlReader.close();
					xmlFileIO.closeXMLStreamReader();
					DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
					int numberOfClasses = classificator.getNumberOfDetectedClasses();
					for (int i = 0; i < numberOfClasses; i++) {
						foundSubjectsTable = new HashMap<String, Integer>();
						List<Object> items = classificator.getCorrespondingObjectsOfClass(i);
						for (Object item : items) {
							UUID uuidItem = UUID.fromString((String) item);
							String subject = subjectTable.get(uuidItem);
							Integer value = foundSubjectsTable.get(subject);
							if (value == null) {
								value = 1;
							} else {
								value++;
							}
							foundSubjectsTable.put(subject, value);
						}
						for (int j = 0; j < subjectNames.size(); j++) {
							String name = subjectNames.get(j);
							double proportion = 0;
							if (foundSubjectsTable.get(name) != null) {
								int value = foundSubjectsTable.get(name);
								proportion = value / (double) numberOfSubjectsInTable.get(name) * 100.0;
							}
							String interangle = null;
							if (i == 0) {
								interangle = String.format("%.2f", 0.00);
							} else {
								interangle = String.format("%.2f", classificator.getInterAngleOfClasses(i - 1, i));
							}
							String xAxisHeader = "(" + (i + 1) + "/" + items.size() + "/" + interangle + ")";
							dataSet.addValue(proportion, name, xAxisHeader);
						}

					}
					String header = "(" + fileName + "/" + classificator.getVigilanceParameter() + "/"
							+ classificator.getNumberOfDetectedClasses() + "/" + classificator.getNumberOfEpochs() + ")";
					tempFileList.add(chartTool.exportToBarChart(dataSet, header));
				} catch (Exception e) {
					ErrorLogger.getInstance().writeError(
							"Error during evaluation of classification results in file: " + fileName, this.getActivityName(), e);
				}
			}
			File file = new File(files.get(0));
			file = FileNameGenerator.getNewFile(file.getParent(), ".pdf", "Art2aMergedClassificationResult");
			chartTool.setPdfPageInPortrait(false);
			chartTool.exportToChartsToPDF(tempFileList, file, pdfTitle);
			resultFileNames.add(file.getAbsolutePath());
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.PROCESS_ART2A_RESULT_ERROR, this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.PROCESS_ART2A_RESULT_ERROR);
		}
		return null;
	}

	@Override
	public String getActivityName() {
		return ART2aResultConsideringDifferentOriginsAsPDF.ART2A_RESULT_CONSIDERING_DIFFERENT_ORIGINS_AS_PDF_ACTIVITY;
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
		return "Description: "
				+ ART2aResultConsideringDifferentOriginsAsPDF.ART2A_RESULT_CONSIDERING_DIFFERENT_ORIGINS_AS_PDF_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.ART2A_FOLDER_NAME;
	}

}

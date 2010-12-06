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

import javax.xml.stream.XMLStreamReader;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.openscience.cdk.applications.art2aclassification.Art2aClassificator;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.io.XMLFileIO;

/**
 * Class which represents the ART-2a result to CSV activity. This worker extracts content from a given set of ART2A classification
 * results and creates a String list in the comma separated value format.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class ART2aResultAsCSV extends AbstractCDKActivity {

	public static final String ART2A_RESULT_AS_CSV_ACTIVITY = "ART-2a Result As CSV";

	/**
	 * Creates a new instance.
	 */
	public ART2aResultAsCSV() {
		this.INPUT_PORTS = new String[] { "ART-2a Files" };
		this.RESULT_PORTS = new String[] { "ART-2a CSV" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, File.class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.RESULT_PORTS[0], 1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		List<String> result = new ArrayList<String>();
		List<String> files = (List<String>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), String.class,
				context);
		if (files == null || files.size() == 0) {
			throw new CDKTavernaException(this.getActivityName(), "Error, no file!");
		}
		try {
			XMLStreamReader xmlReader;
			StringBuffer buffer;
			Art2aClassificator classificator;
			XMLFileIO xmlFileIO = new XMLFileIO();
			int maxNumberOfDetectedClasses = 0;
			result.add("FileName;VigilanceParameter;NumberOfDetectedClasses;ConvergenceFlag;ClassificationComplete;InputScalingFactor;LearningParameter;NumberOfEpochs");
			for (String fileName : files) {
				buffer = new StringBuffer();
				buffer.append(fileName);
				buffer.append(";");

				xmlReader = xmlFileIO.getXMLStreamReaderWithCompression(fileName);
				xmlReader.next();
				classificator = new Art2aClassificator(xmlReader, true);

				buffer.append(classificator.getVigilanceParameter());
				buffer.append(";");
				buffer.append(classificator.getNumberOfDetectedClasses());
				buffer.append(";");
				buffer.append(classificator.getConvergenceFlag());
				buffer.append(";");
				buffer.append(classificator.getClassificationCompleteFlag());
				buffer.append(";");
				buffer.append(classificator.getInputScalingFactor());
				buffer.append(";");
				buffer.append(classificator.getLearningParameter());
				buffer.append(";");
				buffer.append(classificator.getNumberOfEpochs());
				buffer.append(";");

				// Add the number of vectors in class
				for (int i = 0; i < classificator.getNumberOfDetectedClasses(); i++) {
					buffer.append(classificator.getNumberOfVectorsInClass(i));
					buffer.append(";");
				}
				result.add(buffer.toString());
				if (classificator.getNumberOfDetectedClasses() > maxNumberOfDetectedClasses) {
					maxNumberOfDetectedClasses = classificator.getNumberOfDetectedClasses();
				}
				xmlReader.close();
				xmlFileIO.closeXMLStreamReader();
			}
			buffer = new StringBuffer();
			buffer.append(result.get(0));
			for (int i = 0; i < maxNumberOfDetectedClasses; i++) {
				buffer.append(";");
				buffer.append("NumberOfVectorsInClass_");
				buffer.append(i);
			}
			result.set(0, buffer.toString());

			T2Reference containerRef = referenceService.register(result, 1, true, context);
			outputs.put(this.RESULT_PORTS[0], containerRef);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outputs;
	}

	@Override
	public String getActivityName() {
		return ART2aResultAsCSV.ART2A_RESULT_AS_CSV_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_SUPPORT_MULTI_FILE, true);
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + ART2aResultAsCSV.ART2A_RESULT_AS_CSV_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.ART2A_FOLDER_NAME;
	}

}

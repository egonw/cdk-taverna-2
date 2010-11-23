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
package org.openscience.cdk.applications.taverna.stringconverter;

/**
 * Class which represents the structure to MDL SDFile String converter activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.interfaces.IFileReader;
import org.openscience.cdk.io.SDFWriter;

public class StructureToMDLSDFileStringConverterActivity extends AbstractCDKActivity implements IFileReader {

	public static final String MDL_SDFILE_STRING_CONVERTER_ACTIVITY = "Structure to MDL SDFilr String Converter";

	/**
	 * Creates a new instance.
	 */
	public StructureToMDLSDFileStringConverterActivity() {
		this.INPUT_PORTS = new String[] { "Structures" };
		this.RESULT_PORTS = new String[] { "MDL SDFile String", "Not Converted" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		for (String name : this.RESULT_PORTS) {
			addOutput(name, 1);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		LinkedList<CMLChemFile> notConverted = new LinkedList<CMLChemFile>();
		List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), byte[].class,
				context);
		List<CMLChemFile> chemFileList = null;
		try {
			chemFileList = CDKObjectHandler.getChemFileList(dataArray);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error during deserializing object!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		List<String> sdfStringList = new ArrayList<String>();
		StringWriter stringWriter = new StringWriter();
		SDFWriter writer = new SDFWriter(stringWriter);
		for (CMLChemFile cml : chemFileList) {
			try {
				writer.write(cml);
			} catch (Exception e) {
				notConverted.add(cml);
				ErrorLogger.getInstance().writeError("Error converting MDL SD file String!", this.getActivityName(), e);
			}
		}
		try {
			writer.close();
		} catch (IOException e) {
			ErrorLogger.getInstance().writeError("I/O error!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		sdfStringList.add(stringWriter.toString());
		if (sdfStringList.isEmpty()) {
			throw new CDKTavernaException(this.getActivityName(), "Error while converting MDL SDFile Strings");
		}
		T2Reference containerRef = referenceService.register(sdfStringList, 1, true, context);
		outputs.put(this.RESULT_PORTS[0], containerRef);
		containerRef = referenceService.register(notConverted, 1, true, context);
		outputs.put(this.RESULT_PORTS[1], containerRef);
		// Return results
		return outputs;
	}

	@Override
	public String getActivityName() {
		return StructureToMDLSDFileStringConverterActivity.MDL_SDFILE_STRING_CONVERTER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + StructureToMDLSDFileStringConverterActivity.MDL_SDFILE_STRING_CONVERTER_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.STRING_CONVERTER_FOLDER_NAME;
	}

}

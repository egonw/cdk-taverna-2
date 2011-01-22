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
package org.openscience.cdk.applications.taverna.qsar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.interfaces.IFileReader;

/**
 * Class which represents the CSV to QSAR vector activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class CSVToQSARVectorActivity extends AbstractCDKActivity implements IFileReader {

	public static final String CSV_TO_QSAR_VECTOR_ACTIVITY = "CSV To QSAR Vector";

	/**
	 * Creates a new instance.
	 */
	public CSVToQSARVectorActivity() {
		this.OUTPUT_PORTS = new String[] { "Descriptor Vector", "Descriptor Names" };
	}

	@Override
	protected void addInputPorts() {
		// empty
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 0);
		addOutput(this.OUTPUT_PORTS[1], 0);
	}

	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		File[] files = (File[]) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE);
		if (files == null || files.length == 0) {
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.NO_FILE_CHOSEN);
		}
		// No multi file selection supported
		File file = files[0];
		Map<UUID, Map<String, Object>> vectorMap = new HashMap<UUID, Map<String, Object>>();
		Map<String, Object> descriptorResultMap;
		ArrayList<String> descriptorNames = new ArrayList<String>();
		LineNumberReader reader = null;
		try {
			reader = new LineNumberReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			ErrorLogger.getInstance().writeError(file.getPath() + "does not exist!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), file.getPath() + "does not exist!");
		}
		String line = "";
		int idx = 0;
		try {
			boolean readDescriptorNames = true;
			while ((line = reader.readLine()) != null) {
				idx++;
				String[] items = this.getItems(line);
				// First line contains the descriptor names
				if (readDescriptorNames) {
					// Skip first item because it contains only the id
					for (int i = 1; i < items.length; i++) {
						descriptorNames.add(items[i]);
					}
					readDescriptorNames = false;
				} else {
					// Read values
					descriptorResultMap = new HashMap<String, Object>();
					String uuidString = items[0];
					for (int i = 0; i < descriptorNames.size(); i++) {
						String key = descriptorNames.get(i);
						Double value = Double.parseDouble(items[i + 1]);
						descriptorResultMap.put(key, value);
					}
					vectorMap.put(UUID.fromString(uuidString), descriptorResultMap);
				}
			}
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.READ_FILE_ERROR + file.getPath() + "!",
					this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.READ_FILE_ERROR + file.getPath() + "!");
		}
		try {
			byte[] vectorData = CDKObjectHandler.getBytes(vectorMap);
			T2Reference containerRef = referenceService.register(vectorData, 0, true, context);
			outputs.put(this.OUTPUT_PORTS[0], containerRef);
			byte[] nameData = CDKObjectHandler.getBytes(descriptorNames);
			containerRef = referenceService.register(nameData, 0, true, context);
			outputs.put(this.OUTPUT_PORTS[1], containerRef);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.OUTPUT_PORT_CONFIGURATION_ERROR, this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.OUTPUT_PORT_CONFIGURATION_ERROR);
		}
		return outputs;
	}

	/**
	 * Splits a CSV string line into its single items.
	 * 
	 * @param line
	 *            CSV string
	 * @return Items
	 */
	private String[] getItems(String line) {
		String[] items = line.split("\";\"");
		items[0] = items[0].replaceAll("\"", "");
		items[items.length - 1] = items[items.length - 1].replaceAll("\";", "");
		return items;
	}

	@Override
	public String getActivityName() {
		return CSVToQSARVectorActivity.CSV_TO_QSAR_VECTOR_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION, ".csv");
		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION_DESCRIPTION, "Any CSV File");
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + CSVToQSARVectorActivity.CSV_TO_QSAR_VECTOR_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.QSAR_FOLDER_NAME;
	}

}

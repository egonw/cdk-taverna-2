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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.openscience.cdk.applications.taverna.interfaces.IPortNumber;
import org.openscience.cdk.applications.taverna.qsar.utilities.QSARVectorUtility;

/**
 * Class which represents the merge QSAR vectors activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class MergeQSARVectorsActivity extends AbstractCDKActivity implements IPortNumber {

	public static final String MERGE_QSAR_VECTORS_ACTIVITY = "Merge QSAR Vectors";

	/**
	 * Creates a new instance.
	 */
	public MergeQSARVectorsActivity() {
		this.INPUT_PORTS = new String[] { "Descriptor Vector", "Descriptor Names", "Name" };
		this.RESULT_PORTS = new String[] { "Merged Descriptor Vector", "Merged Descriptor Names", "Relations Table" };
	}

	@Override
	protected void addInputPorts() {
		int numberOfPorts = (Integer) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_NUMBER_OF_PORTS);
		for (int i = 1; i <= numberOfPorts; i++) {
			addInput(this.INPUT_PORTS[0] + "_" + i, 0, true, null, byte[].class);
			addInput(this.INPUT_PORTS[1] + "_" + i, 0, true, null, byte[].class);
			addInput(this.INPUT_PORTS[2] + "_" + i, 0, true, null, String.class);
		}
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.RESULT_PORTS[0], 0);
		addOutput(this.RESULT_PORTS[1], 0);
		addOutput(this.RESULT_PORTS[2], 1);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		int numberOfPorts = (Integer) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_NUMBER_OF_PORTS);
		List<Map<UUID, Map<String, Object>>> vectorMapList = new ArrayList<Map<UUID, Map<String, Object>>>();
		List<List<String>> descriptorNameList = new ArrayList<List<String>>();
		String names[] = new String[numberOfPorts];
		for (int i = 1; i <= numberOfPorts; i++) {
			Map<UUID, Map<String, Object>> vectorMap;
			byte[] vectorData = (byte[]) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0] + "_" + i),
					byte[].class, context);
			try {
				vectorMap = (Map<UUID, Map<String, Object>>) CDKObjectHandler.getObject(vectorData);
				vectorMapList.add(vectorMap);
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError("Error during deserializing object!", this.getActivityName(), e);
				throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
			}
			ArrayList<String> descriptorNames;
			byte[] nameData = (byte[]) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[1] + "_" + i), byte[].class,
					context);
			try {
				descriptorNames = (ArrayList<String>) CDKObjectHandler.getObject(nameData);
				descriptorNameList.add(descriptorNames);
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError("Error during deserializing object!", this.getActivityName(), e);
				throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
			}
			String name = (String) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[2] + "_" + i), String.class,
					context);
			names[i - 1] = name;
		}
		QSARVectorUtility qsarVectorUtility = new QSARVectorUtility();
		// Create minimum set of the descriptor names
		ArrayList<String> mergedDescriptorNames = qsarVectorUtility.createMinimumDescriptorNamesList(descriptorNameList);
		// Create merged QSAR vector
		Map<UUID, Map<String, Object>> mergedVectorMap = qsarVectorUtility.mergeQSARVectors(vectorMapList, mergedDescriptorNames);
		// Create id relation table
		ArrayList<String> idTable = new ArrayList<String>();
		for (int i = 0; i < numberOfPorts; i++) {
			Map<UUID, Map<String, Object>> vectorMap = vectorMapList.get(i);
			String name = names[i];
			String line = "> <NAME> " + name;
			idTable.add(line);
			List<UUID> uuids = qsarVectorUtility.getUUIDs(vectorMap);
			for (UUID uuid : uuids) {
				line = "> <ENTRY> " + uuid.toString();
				idTable.add(line);
			}
		}
		try {
			byte[] vectorData = CDKObjectHandler.getBytes(mergedVectorMap);
			T2Reference containerRef = referenceService.register(vectorData, 0, true, context);
			outputs.put(this.RESULT_PORTS[0], containerRef);
			byte[] nameData = CDKObjectHandler.getBytes(mergedDescriptorNames);
			containerRef = referenceService.register(nameData, 0, true, context);
			outputs.put(this.RESULT_PORTS[1], containerRef);
			containerRef = referenceService.register(idTable, 1, true, context);
			outputs.put(this.RESULT_PORTS[2], containerRef);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error during configurating output port!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), "Error while configurating output port!");
		}
		return outputs;
	}

	@Override
	public String getActivityName() {
		return MergeQSARVectorsActivity.MERGE_QSAR_VECTORS_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_NUMBER_OF_PORTS, 2);
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + MergeQSARVectorsActivity.MERGE_QSAR_VECTORS_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.QSAR_FOLDER_NAME;
	}

}

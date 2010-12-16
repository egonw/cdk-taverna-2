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
import java.util.LinkedList;
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
import org.openscience.cdk.applications.taverna.qsar.utilities.QSARVectorUtility;

/**
 * Class which represents the Get Molecular Weight Distribution From QSAR Vector activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class GetMolecularWeightDistributionFromQSARVectorActivity extends AbstractCDKActivity {

	public static final String GET_MOLECULAR_WEIGHT_DISTRIBUTION_FROM_QSAR_VECTOR_ACTIVITY = "Get Molecular Weight Distribution From QSAR Vector";

	/**
	 * Creates a new instance.
	 */
	public GetMolecularWeightDistributionFromQSARVectorActivity() {
		this.INPUT_PORTS = new String[] { "Descriptor Vector", "Descriptor Names" };
		this.RESULT_PORTS = new String[] { "MW Distribution CSV", "MW Molecule IDS CSV" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 0, true, null, byte[].class);
		addInput(this.INPUT_PORTS[1], 0, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.RESULT_PORTS[0], 1);
		addOutput(this.RESULT_PORTS[1], 1);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		Map<UUID, Map<String, Object>> vectorMap;
		byte[] vectorData = (byte[]) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), byte[].class, context);
		try {
			vectorMap = (Map<UUID, Map<String, Object>>) CDKObjectHandler.getObject(vectorData);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.OBJECT_DESERIALIZATION_ERROR, this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		ArrayList<String> descriptorNames;
		byte[] nameData = (byte[]) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[1]), byte[].class, context);
		try {
			descriptorNames = (ArrayList<String>) CDKObjectHandler.getObject(nameData);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.OBJECT_DESERIALIZATION_ERROR, this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		if (!descriptorNames.contains("weight")) {
			throw new CDKTavernaException(this.getActivityName(), "QSAR Vector contains no molecular weight data!");
		}
		ArrayList<String> molIdSWeightCSV = new ArrayList<String>();
		ArrayList<String> weightDistributionCSV = new ArrayList<String>();
		try {
			QSARVectorUtility util = new QSARVectorUtility();
			List<UUID> uuids = util.getUUIDs(vectorMap);
			LinkedList<Double> weigths = new LinkedList<Double>();
			int maxWeight = 0;
			molIdSWeightCSV.add("ID;Molecular Weight (g/mol);");
			for (int i = 0; i < uuids.size(); i++) {
				UUID uuid = uuids.get(i);
				Map<String, Object> values = vectorMap.get(uuid);
				Double weight = (Double) values.get("weight");
				if (weight.isInfinite() || weight.isNaN()) {
					continue;
				}
				weigths.add(weight);
				if (weight.intValue() > maxWeight) {
					maxWeight = weight.intValue();
				}
				molIdSWeightCSV.add(uuid.toString() + ";" + String.format("%.2f", weight) + ";");
			}
			int[] weightDistribution = new int[maxWeight + 1];
			for (Double weight : weigths) {
				int value = weight.intValue();
				weightDistribution[value]++;
			}
			weightDistributionCSV.add("Molecular Weight (g/mol);Number Of Molecules;");
			for (int i = 1; i < weightDistribution.length; i++) {
				weightDistributionCSV.add(i + ";" + weightDistribution[i] + ";");
			}
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error during extraction of molecular weight from QSAR vector!",
					this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		try {
			T2Reference containerRef = referenceService.register(weightDistributionCSV, 1, true, context);
			outputs.put(this.RESULT_PORTS[0], containerRef);
			containerRef = referenceService.register(molIdSWeightCSV, 1, true, context);
			outputs.put(this.RESULT_PORTS[1], containerRef);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.OUTPUT_PORT_CONFIGURATION_ERROR, this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.OUTPUT_PORT_CONFIGURATION_ERROR);
		}
		return outputs;
	}

	@Override
	public String getActivityName() {
		return GetMolecularWeightDistributionFromQSARVectorActivity.GET_MOLECULAR_WEIGHT_DISTRIBUTION_FROM_QSAR_VECTOR_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_QSAR_VECTOR_CURATION_TYPE, QSARVectorUtility.DYNAMIC_CURATION);
		properties.put(CDKTavernaConstants.PROPERTY_QSAR_VECTOR_MIN_MAX_CURATION, true);
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: "
				+ GetMolecularWeightDistributionFromQSARVectorActivity.GET_MOLECULAR_WEIGHT_DISTRIBUTION_FROM_QSAR_VECTOR_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.QSAR_FOLDER_NAME;
	}

}

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
package org.openscience.cdk.applications.taverna.weka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openscience.cdk.applications.art2aclassification.Art2aClassificator;
import org.openscience.cdk.applications.art2aclassification.FingerprintItem;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.qsar.utilities.QSARVectorUtility;
import org.openscience.cdk.applications.taverna.weka.utilities.WekaTools;

import weka.core.Instances;

/**
 * Class which represents the create Weka dataset from QSAR vector activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class CreateWekaDatasetFromQSARVectorActivity extends AbstractCDKActivity {

	public static final String CREATE_WEKA_DATASET_FROM_QSAR_VECTOR_ACTIVITY = "Create Weka Dataset From QSAR Vector";

	/**
	 * Creates a new instance.
	 */
	public CreateWekaDatasetFromQSARVectorActivity() {
		this.INPUT_PORTS = new String[] { "Descriptor Vector", "Descriptor Names" };
		this.OUTPUT_PORTS = new String[] { "Weka Dataset" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 0, true, null, byte[].class);
		addInput(this.INPUT_PORTS[1], 0, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void work() throws Exception {
		// Get input
		Map<UUID, Map<String, Object>> vectorMap;
		try {
			vectorMap = (Map<UUID, Map<String, Object>>) this.getInputAsObject(this.INPUT_PORTS[0]);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.WRONG_INPUT_PORT_TYPE, this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		ArrayList<String> descriptorNames;
		try {
			descriptorNames = (ArrayList<String>) this.getInputAsObject(this.INPUT_PORTS[1]);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.WRONG_INPUT_PORT_TYPE, this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		// Do work
		Instances dataset = null;
		try {
			WekaTools tools = new WekaTools();
			QSARVectorUtility vectorUtility = new QSARVectorUtility();
			List<FingerprintItem> fingerprintList = vectorUtility.createFingerprintItemListFromQSARVector(vectorMap,
					descriptorNames);
			FingerprintItem[] itemArray = fingerprintList.toArray(new FingerprintItem[0]);
			Art2aClassificator.scaleFingerprintVectorComponentsToIntervalZeroOne(itemArray);
			dataset = tools.createInstancesFromFingerprintArray(itemArray, descriptorNames);
		} catch (Exception e) {
			ErrorLogger.getInstance()
					.writeError("Error during fingerprint items creation !", this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		// Set output
		this.setOutputAsObject(dataset, this.OUTPUT_PORTS[0]);
	}

	@Override
	public String getActivityName() {
		return CreateWekaDatasetFromQSARVectorActivity.CREATE_WEKA_DATASET_FROM_QSAR_VECTOR_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + CreateWekaDatasetFromQSARVectorActivity.CREATE_WEKA_DATASET_FROM_QSAR_VECTOR_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.WEKA_FOLDER_NAME;
	}

}

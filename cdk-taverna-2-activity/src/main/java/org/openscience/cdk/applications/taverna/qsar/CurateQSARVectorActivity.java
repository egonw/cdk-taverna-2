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
import java.util.Map;
import java.util.UUID;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.qsar.utilities.QSARVectorUtility;

/**
 * Class which represents the Curate QSAR Vector activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class CurateQSARVectorActivity extends AbstractCDKActivity {

	public static final String CURATE_QSAR_VECTOR_COLUMNS_ACTIVITY = "Curate QSAR Vector";

	/**
	 * Creates a new instance.
	 */
	public CurateQSARVectorActivity() {
		this.INPUT_PORTS = new String[] { "Descriptor Vector", "Descriptor Names" };
		this.OUTPUT_PORTS = new String[] { "Descriptor Vector", "Descriptor Names" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 0, true, null, byte[].class);
		addInput(this.INPUT_PORTS[1], 0, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 0);
		addOutput(this.OUTPUT_PORTS[1], 0);
	}

	@Override
	@SuppressWarnings("unchecked")
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
		Map<UUID, Map<String, Object>> curatedVectorMap = null;
		ArrayList<String> curatedDescriptorNames = null;
		int curationType = (Integer) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_QSAR_VECTOR_CURATION_TYPE);
		boolean curateMinMax = (Boolean) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_QSAR_VECTOR_MIN_MAX_CURATION);
		try {
			QSARVectorUtility vectorUtility = new QSARVectorUtility();
			vectorUtility.curateQSARVector(vectorMap, descriptorNames, curationType, curateMinMax);
			curatedVectorMap = vectorUtility.getCuratedVectorMap();
			curatedDescriptorNames = vectorUtility.getCuratedDescriptorNames();
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error during QSAR vector curation!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		// Set output
		this.setOutputAsObject(curatedVectorMap, this.OUTPUT_PORTS[0]);
		this.setOutputAsObject(curatedDescriptorNames, this.OUTPUT_PORTS[1]);
	}

	@Override
	public String getActivityName() {
		return CurateQSARVectorActivity.CURATE_QSAR_VECTOR_COLUMNS_ACTIVITY;
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
		return "Description: " + CurateQSARVectorActivity.CURATE_QSAR_VECTOR_COLUMNS_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.QSAR_FOLDER_NAME;
	}

}

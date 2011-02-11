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

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.qsar.utilities.QSARVectorUtility;

/**
 * Class which represents the calculate QSAR Vector statistics activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class CalculateQSARVectorStatisticsActivity extends AbstractCDKActivity {

	public static final String CALCULATE_QSAR_VECTOR_STATISTICS_ACTIVITY = "Calculate QSAR Vector Statistics";

	/**
	 * Creates a new instance.
	 */
	public CalculateQSARVectorStatisticsActivity() {
		this.INPUT_PORTS = new String[] { "Descriptor Vector", "Descriptor Names" };
		this.OUTPUT_PORTS = new String[] { "Statistic Strings" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 0, true, null, byte[].class);
		addInput(this.INPUT_PORTS[1], 0, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
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
		List<String> statistics = null;
		try {
			QSARVectorUtility vectorUtility = new QSARVectorUtility();
			statistics = vectorUtility.calculateQSARVectorStatistics(vectorMap, descriptorNames);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error during QSAR vector statistics calculation!",
					this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		// Set output
		this.setOutputAsStringList(statistics, this.OUTPUT_PORTS[0]);
	}

	@Override
	public String getActivityName() {
		return CalculateQSARVectorStatisticsActivity.CALCULATE_QSAR_VECTOR_STATISTICS_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + CalculateQSARVectorStatisticsActivity.CALCULATE_QSAR_VECTOR_STATISTICS_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.QSAR_FOLDER_NAME;
	}

}

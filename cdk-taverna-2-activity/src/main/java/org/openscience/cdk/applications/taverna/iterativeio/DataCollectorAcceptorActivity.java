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
package org.openscience.cdk.applications.taverna.iterativeio;

/**
 * Class which represents the data collector acceptor activity. Its used for gathering data from iterative sources.
 * 
 * @author Andreas Truzskowski
 * 
 */
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;

public class DataCollectorAcceptorActivity extends AbstractCDKActivity {

	public static final String DATA_COLLECTOR_ACCEPTOR_ACTIVITY = "Data Collector Acceptor";

	/**
	 * Creates a new instance.
	 */
	public DataCollectorAcceptorActivity() {
		this.INPUT_PORTS = new String[] { "Data", "UUID" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
		addInput(this.INPUT_PORTS[1], 0, true, null, String.class);
	}

	@Override
	protected void addOutputPorts() {
		// Empty
	}

	@Override
	public void work() throws Exception {
		// Get input
		List<byte[]> dataArray = this.getInputAsList(this.INPUT_PORTS[0], byte[].class);
		UUID id = UUID.fromString(this.getInputAsObject(this.INPUT_PORTS[1], String.class));
		if (id == null) {
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.MOLECULE_NOT_TAGGED_WITH_UUID);
		}
		// Do work
		DataOutputStream dataStream;
		DataOutputStream idxStream;
		try {
			dataStream = DataStreamController.getInstance().getDataCollectorDataStream(id);
			idxStream = DataStreamController.getInstance().createDataCollectorIdxStream(id);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.STREAM_INITIALIZATION_ERROR,
					this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.STREAM_INITIALIZATION_ERROR);
		}
		try {
			for (byte[] data : dataArray) {
				idxStream.writeInt(data.length);
				dataStream.write(data);
			}
			idxStream.flush();
			dataStream.flush();
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.WRITE_CACHE_DATA_ERROR, this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.WRITE_CACHE_DATA_ERROR);
		}
	}

	@Override
	public String getActivityName() {
		return DataCollectorAcceptorActivity.DATA_COLLECTOR_ACCEPTOR_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + DataCollectorAcceptorActivity.DATA_COLLECTOR_ACCEPTOR_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.ITERATIVE_IO_FOLDER_NAME;
	}

}

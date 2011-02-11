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
 * Class which represents the data collector emitter activity. Used in combination with the data collector acceptor activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;

public class DataCollectorEmitterActivity extends AbstractCDKActivity {

	public static final String DATA_COLLECTOR_EMITTER_ACTIVITY = "Data Collector Emitter";

	/**
	 * Creates a new instance.
	 */
	public DataCollectorEmitterActivity() {
		this.INPUT_PORTS = new String[] { "UUID" };
		this.OUTPUT_PORTS = new String[] { "Data" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 0, true, null, String.class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
	}

	@Override
	public void work() throws Exception {
		// Get input
		UUID id = UUID.fromString(this.getInputAsObject(this.INPUT_PORTS[0], String.class));
		if (id == null) {
			throw new CDKTavernaException(DATA_COLLECTOR_EMITTER_ACTIVITY, CDKTavernaException.WRONG_INPUT_PORT_TYPE);
		}
		// Do work
		List<byte[]> dataList = new ArrayList<byte[]>();
		try {
			// close old writer
			DataStreamController.getInstance().closeDataCollectorDataStream(id);
			DataStreamController.getInstance().closeDataCollectorDataStream(id);
			// Read cached data
			File idxFile = new File(DataStreamController.getInstance().createDataCollectorFilename(id, "idx"));
			File datFile = new File(DataStreamController.getInstance().createDataCollectorFilename(id, "dat"));
			DataInputStream idxStream = new DataInputStream(new FileInputStream(idxFile));
			DataInputStream datStream = new DataInputStream(new FileInputStream(datFile));
			do {
				try {
					int offset = idxStream.readInt();
					byte[] data = new byte[offset];
					datStream.read(data);
					dataList.add(data);
				} catch (EOFException e) {
					break;
				}
			} while (true);
			datStream.close();
			idxStream.close();
			// Clean up
			idxFile.delete();
			datFile.delete();
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.READ_CACHE_DATA_ERROR, this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.READ_CACHE_DATA_ERROR);
		}
		// Set output
		this.setOutputAsByteList(dataList, this.OUTPUT_PORTS[0]);
	}

	@Override
	public String getActivityName() {
		return DataCollectorEmitterActivity.DATA_COLLECTOR_EMITTER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + DataCollectorEmitterActivity.DATA_COLLECTOR_EMITTER_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.ITERATIVE_IO_FOLDER_NAME;
	}

}

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
 * Class which represents the data collector acceptor activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
import java.io.DataOutputStream;
import java.io.FileOutputStream;
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
import org.openscience.cdk.applications.taverna.Preferences;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;

public class DataCollectorAcceptorActivity extends AbstractCDKActivity {

	public static final String DATA_COLLECTOR_ACCEPTOR_ACTIVITY = "Data Collector Acceptor";
	private DataOutputStream dataStream = null;
	private DataOutputStream idxStream = null;
	private String filename = "";
	private String indexFilename = "";
	private String dataFilename = "";

	public DataCollectorAcceptorActivity() {
		this.INPUT_PORTS = new String[] { "Data", "UUID" };
		String tmpDir = FileNameGenerator.getTempDir();
		this.filename = FileNameGenerator.getNewFile(tmpDir, "", this.iteration).getPath();
		this.indexFilename = this.filename + ".idx";
		this.dataFilename = this.filename + ".dat";
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
	@SuppressWarnings("unchecked")
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), byte[].class,
				context);
		UUID id = UUID.fromString((String) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[1]), String.class,
				context));
		if (id == null) {
			throw new CDKTavernaException(DATA_COLLECTOR_ACCEPTOR_ACTIVITY, "UUID not set!");
		}
		Preferences.getInstance().setDataCollectorFilename(id, this.filename);
		Preferences.getInstance().setDataCollectorDataStream(id, this.dataStream);
		Preferences.getInstance().setDataCollectorIdxStream(id, this.idxStream);
		try {
			if (this.dataStream == null) {
				this.dataStream = new DataOutputStream(new FileOutputStream(this.dataFilename));
			}
			if (this.idxStream == null) {
				this.idxStream = new DataOutputStream(new FileOutputStream(this.indexFilename));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new CDKTavernaException(DATA_COLLECTOR_ACCEPTOR_ACTIVITY, "Error while initializing data stream!");
		}
		try {
			for (byte[] data : dataArray) {
				this.idxStream.writeInt(data.length);
				this.dataStream.write(data);
			}
		} catch (Exception e) {
			e.printStackTrace();
			comment.add("Error writing data!");
			throw new CDKTavernaException(DATA_COLLECTOR_ACCEPTOR_ACTIVITY, "Error writing data!");
		}
		comment.add("done");
		// Return results
		return outputs;
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

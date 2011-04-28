/*
 * Copyright (C) 2010-2011 by Andreas Truszkowski <ATruszkowski@gmx.de>
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
import java.util.UUID;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Class which represents the create Weka dataset from CSV activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class CreateWekaDatasetFromCSVActivity extends AbstractCDKActivity {

	public static final String CREATE_WEKA_DATASET_FROM_CSV_ACTIVITY = "Create Weka Dataset From CSV";

	/**
	 * Creates a new instance.
	 */
	public CreateWekaDatasetFromCSVActivity() {
		this.INPUT_PORTS = new String[] { "CSV" };
		this.OUTPUT_PORTS = new String[] { "Weka Dataset", "UUID ID CSV" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, String.class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 0);
		addOutput(this.OUTPUT_PORTS[1], 1);
	}

	@Override
	public void work() throws Exception {
		// Get input
		List<String> data = this.getInputAsList(this.INPUT_PORTS[0], String.class);
		// Do work
		Instances dataset = null;
		ArrayList<String> uuidIDCSV = new ArrayList<String>();
		try {
			String header = data.get(0);
			header = header.replaceAll("\"", "");
			String[] heads = header.split(";");
			FastVector attributes = new FastVector(heads.length);
			Attribute idAttr = new Attribute("ID", (FastVector) null);
			attributes.addElement(idAttr);
			for (int i = 1; i < heads.length; i++) {
				attributes.addElement(new Attribute(heads[i]));
			}
			dataset = new Instances("Weka Dataset", attributes, data.size() - 1);
			uuidIDCSV.add("UUID;OriginalID;");
			for (int i = 1; i < data.size(); i++) {
				double[] values = new double[dataset.numAttributes()];
				String dataLine = data.get(i);
				dataLine = dataLine.replace("\"", "");
				String[] d = dataLine.split(";");
				UUID uuid = null;
				try {
					uuid = UUID.fromString(d[0]);
				} catch (Exception e) {
					uuid = UUID.randomUUID();
				}
				values[0] = dataset.attribute(0).addStringValue(uuid.toString());
				uuidIDCSV.add(uuid.toString() + ";" + d[0] + ";");
				for (int j = 1; j < d.length; j++) {
					values[j] = Double.parseDouble(d[j]);
				}
				Instance inst = new Instance(1.0, values);
				dataset.add(inst);
			}
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error during weka dataset creation!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		// Set output
		this.setOutputAsObject(dataset, this.OUTPUT_PORTS[0]);
		this.setOutputAsObject(uuidIDCSV, this.OUTPUT_PORTS[1]);
	}

	@Override
	public String getActivityName() {
		return CreateWekaDatasetFromCSVActivity.CREATE_WEKA_DATASET_FROM_CSV_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + CreateWekaDatasetFromCSVActivity.CREATE_WEKA_DATASET_FROM_CSV_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.WEKA_FOLDER_NAME;
	}

}

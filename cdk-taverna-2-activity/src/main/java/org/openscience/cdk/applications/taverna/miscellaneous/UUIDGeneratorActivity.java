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
package org.openscience.cdk.applications.taverna.miscellaneous;

/**
 * Class which represents the structure to UUID generator activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
import java.util.HashMap;
import java.util.UUID;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;

public class UUIDGeneratorActivity extends AbstractCDKActivity {

	public static final String UUID_GENERATOR_ACTIVITY = "UUID Generator";
	private UUID id = null;

	/**
	 * Creates a new instance.
	 */
	public UUIDGeneratorActivity() {
		this.OUTPUT_PORTS = new String[] { "UUID" };
	}

	@Override
	protected void addInputPorts() {
		// Empty
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 0);
	}

	@Override
	public void work() throws Exception {
		this.id = UUID.randomUUID();
		// Set output
		this.setOutputAsString(this.id.toString(), this.OUTPUT_PORTS[0]);
	}

	@Override
	public String getActivityName() {
		return UUIDGeneratorActivity.UUID_GENERATOR_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + UUIDGeneratorActivity.UUID_GENERATOR_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.MISCELLANEOUS_FOLDER_NAME;
	}

}

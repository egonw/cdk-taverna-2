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
 */package org.openscience.cdk.applications.taverna.miscellaneous;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * Class which represents the tag molecules with UUID activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class TagMoleculesWithUUIDActivity extends AbstractCDKActivity {

	public static final String TAG_MOLECULES_WITH_UUID_ACTIVITY = "Tag Molecules With UUID";

	/**
	 * Creates a new instance.
	 */
	public TagMoleculesWithUUIDActivity() {
		this.INPUT_PORTS = new String[] { "Structures", };
		this.OUTPUT_PORTS = new String[] { "Tagged Structures" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
	}

	@Override
	public String getActivityName() {
		return TagMoleculesWithUUIDActivity.TAG_MOLECULES_WITH_UUID_ACTIVITY;
	}

	@Override
	public String getDescription() {
		return "Descriptions: " + TagMoleculesWithUUIDActivity.TAG_MOLECULES_WITH_UUID_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		return new HashMap<String, Object>();
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.MISCELLANEOUS_FOLDER_NAME;
	}

	@Override
	public void work() throws Exception {
		// Get input
		List<CMLChemFile> chemFileList = this.getInputAsList(this.INPUT_PORTS[0], CMLChemFile.class);
		// Do work
		List<CMLChemFile> taggedMoleculesList = new ArrayList<CMLChemFile>();
		IAtomContainer[] containers;
		try {
			containers = CMLChemFileWrapper.convertCMLChemFileListToAtomContainerArray(chemFileList);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.CML_FILE_CONVERSION_ERROR,
					this.getConfiguration().getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		for (int i = 0; i < containers.length; i++) {
			if (containers[i].getProperty(CDKTavernaConstants.MOLECULEID) == null) {
				UUID uuid = UUID.randomUUID();
				containers[i].setProperty(CDKTavernaConstants.MOLECULEID, uuid.toString());
				taggedMoleculesList.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(containers[i]));
			}
		}
		// Set output
		this.setOutputAsObjectList(taggedMoleculesList, this.OUTPUT_PORTS[0]);
	}
}

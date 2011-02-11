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
package org.openscience.cdk.applications.taverna.stringconverter;

/**
 * Class which represents the MDL Mol String to structure converter activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.io.MDLReader;

public class MDLMolStringToStructureConverterActivity extends AbstractCDKActivity {

	public static final String MDL_MOL_STRING_CONVERTER_ACTIVITY = "MDL Mol String to Structures Converter";

	/**
	 * Creates a new instance.
	 */
	public MDLMolStringToStructureConverterActivity() {
		this.INPUT_PORTS = new String[] { "MDL Mol String" };
		this.OUTPUT_PORTS = new String[] { "Structures", "Not Converted" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, String.class);
	}

	@Override
	protected void addOutputPorts() {
		for (String name : this.OUTPUT_PORTS) {
			addOutput(name, 1);
		}
	}

	@Override
	public void work() throws Exception {
		// Get input
		List<String> molList = this.getInputAsList(this.INPUT_PORTS[0], String.class);
		// Do work
		List<CMLChemFile> cmlChemFileList = new ArrayList<CMLChemFile>();
		LinkedList<String> notConverted = new LinkedList<String>();
		for (String mol : molList) {
			try {
				MDLReader reader = new MDLReader(new ByteArrayInputStream(mol.getBytes()));
				CMLChemFile cmlChemFile = (CMLChemFile) reader.read(new CMLChemFile());
				reader.close();
				cmlChemFileList.addAll(CMLChemFileWrapper.wrapInChemModelList(cmlChemFile));
			} catch (Exception e) {
				notConverted.add(mol);
				ErrorLogger.getInstance().writeError(CDKTavernaException.CONVERTION_ERROR, this.getActivityName(), e);
			}
		}
		// Set output
		this.setOutputAsObjectList(cmlChemFileList, this.OUTPUT_PORTS[0]);
		this.setOutputAsStringList(notConverted, this.OUTPUT_PORTS[1]);
	}

	@Override
	public String getActivityName() {
		return MDLMolStringToStructureConverterActivity.MDL_MOL_STRING_CONVERTER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + MDLMolStringToStructureConverterActivity.MDL_MOL_STRING_CONVERTER_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.STRING_CONVERTER_FOLDER_NAME;
	}

}

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
 * Class which represents the reaction to MDL RXN String converter activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.openscience.cdk.Reaction;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.io.MDLRXNWriter;

public class ReactionToMDLRXNStringConverterActivity extends AbstractCDKActivity {

	public static final String MDL_RXN_STRING_CONVERTER_ACTIVITY = "Reaction to MDL RXN String Converter";

	/**
	 * Creates a new instance.
	 */
	public ReactionToMDLRXNStringConverterActivity() {
		this.INPUT_PORTS = new String[] { "Reactions" };
		this.OUTPUT_PORTS = new String[] { "MDL RXN String", "Not Converted" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
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
		List<Reaction> rxnFileList = this.getInputAsList(this.INPUT_PORTS[0], Reaction.class);
		// Do work
		LinkedList<Reaction> notConverted = new LinkedList<Reaction>();
		List<String> rxnStringList = new ArrayList<String>();
		for (IReaction rxn : rxnFileList) {
			try {
				StringWriter stringWriter = new StringWriter();
				MDLRXNWriter writer = new MDLRXNWriter(stringWriter);
				writer.write(rxn);
				writer.close();
				rxnStringList.add(stringWriter.toString());
			} catch (Exception e) {
				notConverted.add((Reaction) rxn);
				ErrorLogger.getInstance().writeError(CDKTavernaException.CONVERTION_ERROR, this.getActivityName(), e);
			}
		}
		// Set input
		this.setOutputAsStringList(rxnStringList, this.OUTPUT_PORTS[0]);
		this.setOutputAsObjectList(notConverted, this.OUTPUT_PORTS[1]);
	}

	@Override
	public String getActivityName() {
		return ReactionToMDLRXNStringConverterActivity.MDL_RXN_STRING_CONVERTER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + ReactionToMDLRXNStringConverterActivity.MDL_RXN_STRING_CONVERTER_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.STRING_CONVERTER_FOLDER_NAME;
	}

}

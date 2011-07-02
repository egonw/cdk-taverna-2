/* Copyright (C) 2005,2011 Egon Willighagen <egonw@users.sf.net>
 *                         Thomas Kuhn
 *                         Others
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
package org.openscience.cdk.applications.taverna.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 * Class which represents the atom count filter activity.
 */
public class AtomCountFilter extends AbstractCDKActivity {

	public static final String FILTER_ACTIVITY = "AtomCount Filter";

	public AtomCountFilter() {
		this.INPUT_PORTS = new String[] { "Structures", "Max Atom Count", "Min Atom Count" };
		this.OUTPUT_PORTS = new String[] { "matchingStructures", "discardedStructures" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
		addInput(this.INPUT_PORTS[1], 0, true, null, Integer.class);
		addInput(this.INPUT_PORTS[2], 0, true, null, Integer.class);
	}

	@Override
	protected void addOutputPorts() {
		for (String name : this.OUTPUT_PORTS) {
			addOutput(name, 1);
		}
	}

	@Override
	public String getActivityName() {
		return AtomCountFilter.FILTER_ACTIVITY;
	}

	@Override
	public String getDescription() {
		return "Description: " + AtomCountFilter.FILTER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		return new HashMap<String, Object>();
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.FILTER_FOLDER_NAME;
	}

	public void work() throws Exception {
		// Get input
		List<CMLChemFile> inputList = this.getInputAsList(this.INPUT_PORTS[0], CMLChemFile.class);
		// Do work
		List<CMLChemFile> matchedList = new ArrayList<CMLChemFile>();
		List<CMLChemFile> unmatchedList = new ArrayList<CMLChemFile>();
		int maxAtomCount =  
			this.hasInputAsObject(this.INPUT_PORTS[1], Integer.class) ?
				this.getInputAsObject(this.INPUT_PORTS[1], Integer.class) :
                50;
		int minAtomCount = 
			this.hasInputAsObject(this.INPUT_PORTS[2], Integer.class) ?
			    this.getInputAsObject(this.INPUT_PORTS[2], Integer.class) :
			    0;
		try {
			for (CMLChemFile file : inputList) {
				List<IAtomContainer> moleculeList = ChemFileManipulator.getAllAtomContainers(file);
				for (IAtomContainer molecule : moleculeList) {
					if (molecule.getAtomCount() <= maxAtomCount &&
						molecule.getAtomCount() >= minAtomCount) {
						matchedList.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(molecule));
					} else {
						unmatchedList.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(molecule));
					}
				}
			}
		} catch (Exception exception) {
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), exception.getMessage());
		}
		// Set output
		this.setOutputAsObjectList(matchedList, this.OUTPUT_PORTS[0]);
		this.setOutputAsObjectList(unmatchedList, this.OUTPUT_PORTS[1]);
	}

}

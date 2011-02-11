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
package org.openscience.cdk.applications.taverna.curation;

/**
 * Class which represents the curate strange elemnts activity.
 * 
 * @author kalai
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

public class CurateStrangeElementsActivity extends AbstractCDKActivity {

	public static final String SDF_CURATOR_ACTIVITY = "Curate Strange Elements";
	private String[] check = { "C", "H", "N", "O", "P", "S", "Cl", "F", "As", "Se", "Br", "I", "B" };
	private HashSet<String> symbols2Check;

	public CurateStrangeElementsActivity() {
		this.INPUT_PORTS = new String[] { "Structures" };
		this.OUTPUT_PORTS = new String[] { "CURATED", "DISCARDED" };
		symbols2Check = new HashSet<String>(Arrays.asList(check));
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
		List<CMLChemFile> chemFileList = this.getInputAsList(this.INPUT_PORTS[0], CMLChemFile.class);
		// Do work
		ArrayList<CMLChemFile> curated = new ArrayList<CMLChemFile>();
		ArrayList<CMLChemFile> discarded = new ArrayList<CMLChemFile>();
		for (CMLChemFile cml : chemFileList) {
			List<IAtomContainer> moleculeList = ChemFileManipulator.getAllAtomContainers(cml);
			for (IAtomContainer atomContainer : moleculeList) {

				if (shouldRemoveMolecule(atomContainer) == false) {
					curated.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(atomContainer));

				} else {
					discarded.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(atomContainer));
				}
			}
		}
		// Set output
		this.setOutputAsObjectList(curated, this.OUTPUT_PORTS[0]);
		this.setOutputAsObjectList(discarded, this.OUTPUT_PORTS[1]);
	}

	/**
	 * Checks whether the molecule contains a strange element and should be
	 * removed.
	 * 
	 * @param molecule
	 * @return
	 */
	private boolean shouldRemoveMolecule(IAtomContainer molecule) {
		boolean removeMolecule = false;
		String element;
		for (IAtom atom : molecule.atoms()) {
			element = atom.getSymbol();
			if (!symbols2Check.contains(element)) {
				removeMolecule = true;
				break;
			}
		}
		return removeMolecule;
	}

	@Override
	public String getActivityName() {
		return CurateStrangeElementsActivity.SDF_CURATOR_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + CurateStrangeElementsActivity.SDF_CURATOR_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.CURATION_FOLDER_NAME;
	}

}

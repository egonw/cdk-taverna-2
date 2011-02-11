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
 * Class which represents molecule connectivity checker activity.
 * 
 * @author kalai
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

public class MoleculeConnectivityCheckerActivity extends AbstractCDKActivity {

	public static final String CONNECTIVITY_CHECKER_ACTIVITY = "Molecule Connectivity Checker";

	public MoleculeConnectivityCheckerActivity() {
		this.INPUT_PORTS = new String[] { "Structures" };
		this.OUTPUT_PORTS = new String[] { "Accepted", "Rejected" };
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
		Integer cutoffvalue = (Integer) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_ATOM_COUNT_CUTOFF);
		// Do work
		ArrayList<CMLChemFile> accepted = new ArrayList<CMLChemFile>();
		ArrayList<CMLChemFile> rejected = new ArrayList<CMLChemFile>();
		IMolecule nonPartitionedMolecule = null;
		IMoleculeSet molSet = null;
		for (CMLChemFile cml : chemFileList) {
			List<IAtomContainer> moleculeList = ChemFileManipulator.getAllAtomContainers(cml);
			for (IAtomContainer atomContainer : moleculeList) {
				nonPartitionedMolecule = (IMolecule) atomContainer;
				// TODO Copy all properties?
				Map<Object, Object> properties = nonPartitionedMolecule.getProperties();
				molSet = ConnectivityChecker.partitionIntoMolecules(nonPartitionedMolecule);
				for (IAtomContainer molecule : molSet.molecules()) {
					molecule.setProperties(properties);
					if (molecule.getAtomCount() > cutoffvalue) {
						accepted.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(molecule));

					} else {
						rejected.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(molecule));

					}
				}

			}
		}
		// Set output
		this.setOutputAsObjectList(accepted, this.OUTPUT_PORTS[0]);
		this.setOutputAsObjectList(rejected, this.OUTPUT_PORTS[1]);
	}

	@Override
	public String getActivityName() {
		return MoleculeConnectivityCheckerActivity.CONNECTIVITY_CHECKER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_ATOM_COUNT_CUTOFF, 6);
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + MoleculeConnectivityCheckerActivity.CONNECTIVITY_CHECKER_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.CURATION_FOLDER_NAME;
	}
}

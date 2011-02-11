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
 * Class which represents the atom signature activity.
 * 
 * @author kalai
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.signature.AtomSignature;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

public class AtomSignatureActivity extends AbstractCDKActivity {

	public static final String ATOM_SIGNATURE_ACTIVITY = "Generate Atom Signatures";

	public AtomSignatureActivity() {
		this.INPUT_PORTS = new String[] { "Structures" };
		this.OUTPUT_PORTS = new String[] { "Atom Signatures" };

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
	public void work() throws Exception {
		// Get input
		List<CMLChemFile> chemFileList = this.getInputAsList(this.INPUT_PORTS[0], CMLChemFile.class);
		Integer height = (Integer) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_ATOM_SIGNATURE_HEIGHT);
		// Do work	
		ArrayList<String> allAtomSignatures = new ArrayList<String>();
		for (CMLChemFile cml : chemFileList) {
			List<IAtomContainer> moleculeList = ChemFileManipulator.getAllAtomContainers(cml);
			for (IAtomContainer atomContainer : moleculeList) {
				if (atomContainer.getProperty(CDKTavernaConstants.MOLECULEID) == null) {
					throw new CDKTavernaException(this.getActivityName(),
							CDKTavernaException.MOLECULE_NOT_TAGGED_WITH_UUID);
				}
				UUID uuid = (UUID) atomContainer.getProperty(CDKTavernaConstants.MOLECULEID);
				for (IAtom atom : atomContainer.atoms()) {
					AtomSignature atomSignature = new AtomSignature(atom, height, atomContainer);
					String signature = uuid.toString() + " - " + atomSignature.toCanonicalString();
					allAtomSignatures.add(signature);
				}
			}
		}
		// Set output
		this.setOutputAsStringList(allAtomSignatures, this.OUTPUT_PORTS[0]);
	}

	@Override
	public String getActivityName() {
		return AtomSignatureActivity.ATOM_SIGNATURE_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_ATOM_SIGNATURE_HEIGHT, 2);
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + AtomSignatureActivity.ATOM_SIGNATURE_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.CURATION_FOLDER_NAME;
	}
}

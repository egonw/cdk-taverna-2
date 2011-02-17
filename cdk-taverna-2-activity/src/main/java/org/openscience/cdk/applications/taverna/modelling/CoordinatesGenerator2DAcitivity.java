/*
 * Copyright (C) 2011 by Andreas Truszkowski <ATruszkowski@gmx.de>
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
package org.openscience.cdk.applications.taverna.modelling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openscience.cdk.Molecule;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 * Activity for creating 2D-Coordinates for given molecules.
 * 
 * @author kalai
 */
public class CoordinatesGenerator2DAcitivity extends AbstractCDKActivity {

	public static final String COORDINATES_GENERATOR_ACTIVITY = "2D-Coordinates Generator";

	public CoordinatesGenerator2DAcitivity() {
		this.INPUT_PORTS = new String[] { "Structures" };
		this.OUTPUT_PORTS = new String[] { "Structures" };

	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {

		addOutput(this.OUTPUT_PORTS[0], 1);
	}

	public void work() throws Exception {
		// Get input
		List<CMLChemFile> chemfileList = this.getInputAsList(this.INPUT_PORTS[0], CMLChemFile.class);
		// Do work
		ArrayList<CMLChemFile> calculated = new ArrayList<CMLChemFile>();
		for (CMLChemFile cml : chemfileList) {
			List<IAtomContainer> moleculeList = ChemFileManipulator.getAllAtomContainers(cml);
			for (IAtomContainer atomContainer : moleculeList) {
				Map<Object, Object> properties = atomContainer.getProperties();
				StructureDiagramGenerator sdg = new StructureDiagramGenerator();
				sdg.setMolecule(new Molecule(atomContainer));
				try {
					sdg.generateCoordinates();
				} catch (Exception ex) {
					throw new CDKTavernaException(this.getConfiguration().getActivityName(), ex.getMessage());
				}
				Molecule layedOutMol = (Molecule) sdg.getMolecule();
				layedOutMol.setProperties(properties);
				calculated.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(layedOutMol));

			}
		}
		// Set output
		this.setOutputAsObjectList(calculated, this.OUTPUT_PORTS[0]);
	}

	@Override
	public String getActivityName() {
		return CoordinatesGenerator2DAcitivity.COORDINATES_GENERATOR_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + CoordinatesGenerator2DAcitivity.COORDINATES_GENERATOR_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.MODELLING_FOLDER_NAME;
	}

}

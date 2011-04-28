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
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.modeling.builder3d.ModelBuilder3D;
import org.openscience.cdk.modeling.builder3d.TemplateHandler3D;

/**
 * Class which represents the MMFF94 3D-Coordinates Generator activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class MMFF94_3DCoordinatesGeneratorActivity extends AbstractCDKActivity {

	public static final String MMFF94_3D_COORDINATES_GENERATOR_ACTIVITY = "MMFF94 3D-Coordinates Generator";

	/**
	 * Creates a new instance.
	 */
	public MMFF94_3DCoordinatesGeneratorActivity() {
		this.INPUT_PORTS = new String[] { "Structures" };
		this.OUTPUT_PORTS = new String[] { "Calculated Structures", "NOT Calculated Structures" };
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
	public String getActivityName() {
		return MMFF94_3DCoordinatesGeneratorActivity.MMFF94_3D_COORDINATES_GENERATOR_ACTIVITY;
	}

	@Override
	public String getDescription() {
		return "Descriptions: " + MMFF94_3DCoordinatesGeneratorActivity.MMFF94_3D_COORDINATES_GENERATOR_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		return new HashMap<String, Object>();
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.MODELLING_FOLDER_NAME;
	}

	@Override
	public void work() throws Exception {
		// Get input
		List<CMLChemFile> inputList = this.getInputAsList(this.INPUT_PORTS[0], CMLChemFile.class);
		// Do work
		List<CMLChemFile> calculatedList = new ArrayList<CMLChemFile>();
		List<CMLChemFile> notCalculatedList = new ArrayList<CMLChemFile>();
		ModelBuilder3D descriptor;
		try {
			descriptor = ModelBuilder3D.getInstance(TemplateHandler3D.getInstance(), "mmff94");
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.CANNOT_INITIALIZE_3DMODEL_BUILDER,
					this.getActivityName(), e);
			throw new CDKTavernaException(getActivityName(), CDKTavernaException.CANNOT_INITIALIZE_3DMODEL_BUILDER);
		}
		for (CMLChemFile file : inputList) {
			IAtomContainer molecule = CMLChemFileWrapper.wrapChemModelInAtomContainer(file);
			Map<Object, Object> properties = molecule.getProperties();
			IMolecule mol = new Molecule(molecule);
			mol.setProperties(properties);
			try {
				descriptor.generate3DCoordinates(mol, false);
				calculatedList.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(mol));
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError(CDKTavernaException.CANNOT_GENERATE_3DCOORDINATES,
						this.getActivityName(), e);
				notCalculatedList.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(mol));
			}
		}
		// Set output
		this.setOutputAsObjectList(calculatedList, this.OUTPUT_PORTS[0]);
		this.setOutputAsObjectList(notCalculatedList, this.OUTPUT_PORTS[1]);
	}

}

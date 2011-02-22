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
package org.openscience.cdk.applications.taverna.qsar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.IAtomicDescriptor;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 * Abstract class which represents an atomic proton descriptor activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public abstract class AbstractAtomicProtonDescriptor extends AbstractCDKActivity {

	private IAtomicDescriptor descriptor;

	/**
	 * Creates a new instance.
	 */
	public AbstractAtomicProtonDescriptor() {
		super();
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

	public abstract IAtomicDescriptor getDescriptor();

	@Override
	public String getActivityName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getDescription() {
		return "Descriptions: " + this.getClass().getSimpleName();
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		return new HashMap<String, Object>();
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.QSAR_ATOMIC_PROTON_DESCRIPTOR_FOLDER_NAME;
	}

	@Override
	public void work() throws Exception {
		// Get input
		List<CMLChemFile> inputList = this.getInputAsList(this.INPUT_PORTS[0], CMLChemFile.class);

		// Do work
		List<CMLChemFile> calculatedList = new ArrayList<CMLChemFile>();
		List<CMLChemFile> notCalculatedList = new ArrayList<CMLChemFile>();
		if (this.descriptor == null) {
			this.descriptor = getDescriptor();
			if (this.descriptor == null) {
				throw new CDKTavernaException(this.getConfiguration().getActivityName(),
						CDKTavernaException.DESCRIPTOR_INITIALIZION_ERROR);
			}
		}
		for (Iterator<CMLChemFile> iter = inputList.iterator(); iter.hasNext();) {
			CMLChemFile file = iter.next();
			List<IAtomContainer> moleculeList = ChemFileManipulator.getAllAtomContainers(file);
			for (IAtomContainer molecule : moleculeList) {
				try {
					if (molecule.getProperty(CDKTavernaConstants.MOLECULEID) == null) {
						UUID uuid = UUID.randomUUID();
						molecule.setProperty(CDKTavernaConstants.MOLECULEID, uuid.toString());
					}
					for (int j = 0; j < molecule.getAtomCount(); j++) {
						// Calculates only the value if the atom has the symbol H
						if (molecule.getAtom(j).getSymbol().equals("H")) {
							DescriptorValue value = this.descriptor.calculate(molecule.getAtom(j), molecule);
							molecule.getAtom(j).setProperty(value.getSpecification(), value);
							// molecules.setProperty(value.getSpecification(),
							// value);
						}
					}
					calculatedList.add(file);
				} catch (Exception e) {
					ErrorLogger.getInstance().writeError(CDKTavernaException.DESCRIPTOR_CALCULATION_ERROR,
							this.getActivityName(), e);
					notCalculatedList.add(file);
				}
			}
		}
		// Set output
		this.setOutputAsObjectList(calculatedList, this.OUTPUT_PORTS[0]);
		this.setOutputAsObjectList(notCalculatedList, this.OUTPUT_PORTS[1]);
	}

}

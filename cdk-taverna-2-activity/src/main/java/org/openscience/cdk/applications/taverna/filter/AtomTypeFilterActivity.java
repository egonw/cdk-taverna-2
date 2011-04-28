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
 */package org.openscience.cdk.applications.taverna.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;

/**
 * Class which represents the atom type filter activity. It tries to type each
 * atom of the given structures and separates the structures in typeable and not
 * typeable ones.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class AtomTypeFilterActivity extends AbstractCDKActivity {

	public static final String ATOMTYPE_FILTER_ACTIVITY = "Atom Type Filter";

	/**
	 * Creates a new instance.
	 */
	public AtomTypeFilterActivity() {
		this.INPUT_PORTS = new String[] { "Structures", };
		this.OUTPUT_PORTS = new String[] { "Typed Structures", "NOT Typed Structures" };
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
		return AtomTypeFilterActivity.ATOMTYPE_FILTER_ACTIVITY;
	}

	@Override
	public String getDescription() {
		return "Descriptions: " + AtomTypeFilterActivity.ATOMTYPE_FILTER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		return new HashMap<String, Object>();
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.FILTER_FOLDER_NAME;
	}

	@Override
	public void work() throws Exception {
		// Get input
		List<CMLChemFile> chemFileList = this.getInputAsList(this.INPUT_PORTS[0], CMLChemFile.class);
		// Do work
		List<CMLChemFile> typedList = new ArrayList<CMLChemFile>();
		List<CMLChemFile> notTypedList = new ArrayList<CMLChemFile>();
		IAtomContainer[] containers;
		try {
			containers = CMLChemFileWrapper.convertCMLChemFileListToAtomContainerArray(chemFileList);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.CML_FILE_CONVERSION_ERROR,
					this.getConfiguration().getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		for (IAtomContainer cont : containers) {
			IAtom tmpAtom = null;
			try {
				CDKAtomTypeMatcher tmpMatcher = CDKAtomTypeMatcher.getInstance(cont.getBuilder());
				for (int i = 0; i < cont.getAtomCount(); i++) {
					tmpAtom = cont.getAtom(i);
					if (tmpAtom.getAtomTypeName() == null) {
						IAtomType tmpType = tmpMatcher.findMatchingAtomType(cont, tmpAtom);
						AtomTypeManipulator.configure(tmpAtom, tmpType);
					}
				}
			} catch (Exception e) {
				ErrorLogger.getInstance().writeMessage(
						"Atom with element " + tmpAtom.getSymbol() + " could not be typed!");
				notTypedList.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(cont));
				continue;
			}
			typedList.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(cont));
		}
		// Set output
		this.setOutputAsObjectList(typedList, this.OUTPUT_PORTS[0]);
		this.setOutputAsObjectList(notTypedList, this.OUTPUT_PORTS[1]);
	}
}

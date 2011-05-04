/*
 * Copyright (C) 2010 - 2011 by Andreas Truszkowski <ATruszkowski@gmx.de>
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
package org.openscience.cdk.applications.taverna.miscellaneous;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nu.xom.Element;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.interfaces.IIterativeFileWriter;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.smiles.SmilesParser;

import uk.ac.cam.ch.wwmm.opsin.NameToStructure;
import uk.ac.cam.ch.wwmm.opsin.NameToStructureConfig;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult.OPSIN_RESULT_STATUS;

/**
 * Class which represents the name2structure activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class Name2StructureActivity extends AbstractCDKActivity implements IIterativeFileWriter {

	public static final String NAME2STRUCTURE_ACTIVITY = "Name2Structure";
	private File file = null;

	/**
	 * Creates a new instance.
	 */
	public Name2StructureActivity() {
		this.INPUT_PORTS = new String[] { "Name Strings" };
		this.OUTPUT_PORTS = new String[] { "Structures" , "Rejected"};
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, String.class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
		addOutput(this.OUTPUT_PORTS[1], 1);
	}

	@Override
	public void work() throws Exception {
		// Get input
		List<String> names = this.getInputAsList(this.INPUT_PORTS[0], String.class);
		// Do work
		ArrayList<CMLChemFile> results = new ArrayList<CMLChemFile>();
		ArrayList<String> rejected  = new ArrayList<String>();
		try {
			NameToStructure n2s = NameToStructure.getInstance();
			NameToStructureConfig n2sconfig = new NameToStructureConfig();
			for (String name : names) {
				CMLChemFile chemFile = null;
				OpsinResult result = n2s.parseChemicalName(name, n2sconfig);
				if (result.getStatus() == OPSIN_RESULT_STATUS.SUCCESS) {
					String smiles = result.getSmiles();
					try {
						Element cml = result.getCml();
						chemFile = new CMLChemFile(cml.toXML());
					} catch (Exception e) {
						System.out.println(e);
						IMolecule container = new Molecule();
						SmilesParser sp = new SmilesParser(container.getBuilder());
						container = sp.parseSmiles(smiles);
						chemFile = new CMLChemFile();
						chemFile = CMLChemFileWrapper.addAtomContainerToChemModel(chemFile, container);
					}
					chemFile.getChemSequence(0).getChemModel(0).getMoleculeSet().getAtomContainer(0)
							.removeProperty(CDKConstants.FORMULA);
					chemFile.getChemSequence(0).getChemModel(0).getMoleculeSet().getAtomContainer(0)
							.setProperty(CDKConstants.SMILES, smiles);
				}
				if(chemFile == null) {
					rejected.add(name);
					ErrorLogger.getInstance().writeMessage("Unable to parse name: " + name);
				} else {
					results.add(chemFile);
				}
			}
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.WRITE_FILE_ERROR + file.getPath() + "!",
					this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.WRITE_FILE_ERROR + file.getPath()
					+ "!");
		}
		// Set output
		this.setOutputAsObjectList(results, this.OUTPUT_PORTS[0]);
		this.setOutputAsStringList(rejected, this.OUTPUT_PORTS[1]);
	}

	@Override
	public String getActivityName() {
		return Name2StructureActivity.NAME2STRUCTURE_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_ONE_FILE_PER_ITERATION, true);
		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION, ".csv");
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + Name2StructureActivity.NAME2STRUCTURE_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.IO_FOLDER_NAME;
	}
}

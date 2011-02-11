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
package org.openscience.cdk.applications.taverna.io;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;
import net.sf.taverna.t2.reference.impl.external.object.InlineStringReference;

import org.openscience.cdk.MoleculeSet;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.io.SMILESReader;
import org.openscience.cdk.layout.StructureDiagramGenerator;

/**
 * Class which represents the SMILES file reader activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class SMILESFileReaderActivity extends AbstractCDKActivity {

	public static final String SMILES_FILE_READER_ACTIVITY = "SMILES File Reader";

	/**
	 * Creates a new instance.
	 */
	public SMILESFileReaderActivity() {
		this.INPUT_PORTS = new String[] { "Files" };
		this.OUTPUT_PORTS = new String[] { "Structures" };
	}

	@Override
	protected void addInputPorts() {
		List<Class<? extends ExternalReferenceSPI>> expectedReferences = new ArrayList<Class<? extends ExternalReferenceSPI>>();
		expectedReferences.add(FileReference.class);
		expectedReferences.add(InlineStringReference.class);
		addInput(this.INPUT_PORTS[0], 1, false, expectedReferences, null);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
	}

	@Override
	public void work() throws Exception {
		// Get input
		List<File> files = this.getInputAsFileList(this.INPUT_PORTS[0]);
		// Do work
		List<CMLChemFile> chemFileList = new ArrayList<CMLChemFile>();
		for (File file : files) {
			IMoleculeSet som = null;
			try {
				SMILESReader reader = new SMILESReader(new FileReader(file));
				som = (IMoleculeSet) reader.read(new MoleculeSet());
				reader.close();
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError(CDKTavernaException.READ_FILE_ERROR + file.getPath() + "!",
						this.getActivityName(), e);
			}
			IMoleculeSet som2D = new MoleculeSet();
			StructureDiagramGenerator str = new StructureDiagramGenerator();
			for (int i = 0; i < som.getMoleculeCount(); i++) {
				try {
					str.setMolecule(som.getMolecule(i));
					str.generateCoordinates();
					som2D.addMolecule(str.getMolecule());
				} catch (Exception e) {
					ErrorLogger.getInstance().writeError(CDKTavernaException.GENERATE_2D_COORDINATES_ERROR,
							this.getActivityName(), e);
				}
			}
			CMLChemFile cmlChemFile;
			for (int i = 0; i < som2D.getMoleculeCount(); i++) {
				cmlChemFile = CMLChemFileWrapper.wrapInChemModel(som2D.getMolecule(i));
				chemFileList.addAll(CMLChemFileWrapper.wrapInChemModelList(cmlChemFile));
			}
		}
		// Set output
		this.setOutputAsObjectList(chemFileList, this.OUTPUT_PORTS[0]);
	}

	@Override
	public String getActivityName() {
		return SMILESFileReaderActivity.SMILES_FILE_READER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION, "");
		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION_DESCRIPTION, "Any SMILES file");
		properties.put(CDKTavernaConstants.PROPERTY_SUPPORT_MULTI_FILE, true);
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + SMILESFileReaderActivity.SMILES_FILE_READER_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.IO_FOLDER_NAME;
	}

}

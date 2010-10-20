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
import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.openscience.cdk.MoleculeSet;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.interfaces.IFileReader;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.io.SMILESReader;
import org.openscience.cdk.layout.StructureDiagramGenerator;

/**
 * Class which represents the SMILES file reader activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class SMILESFileReaderActivity extends AbstractCDKActivity implements IFileReader {

	public static final String SMILES_FILE_READER_ACTIVITY = "SMILES File Reader";

	/**
	 * Creates a new instance.
	 */
	public SMILESFileReaderActivity() {
		this.RESULT_PORTS = new String[] { "Structures" };
	}

	@Override
	protected void addInputPorts() {
		// empty
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.RESULT_PORTS[0], 1);
	}

	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		CMLChemFile cmlChemFile = new CMLChemFile();
		List<byte[]> dataArray = new ArrayList<byte[]>();
		// Read SMILES file
		File[] files = (File[]) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE);
		if (files == null || files.length == 0) {
			throw new CDKTavernaException(this.getActivityName(), "Error, no file chosen!");
		}
		for (File file : files) {
			IMoleculeSet som = null;
			try {
				SMILESReader reader = new SMILESReader(new FileReader(file));
				som = (IMoleculeSet) reader.read(new MoleculeSet());
				reader.close();
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError("Error while reading SMILES file: " + file.getPath() + "!",
						this.getActivityName(), e);
				comment.add("Error while reading SMILES file: " + file.getPath() + "!");
			}
			IMoleculeSet som2D = new MoleculeSet();
			StructureDiagramGenerator str = new StructureDiagramGenerator();
			for (int i = 0; i < som.getMoleculeCount(); i++) {
				try {
					str.setMolecule(som.getMolecule(i));
					str.generateCoordinates();
					som2D.addMolecule(str.getMolecule());
				} catch (Exception e) {
					ErrorLogger.getInstance().writeError("Error generating 2D Coordinate!", this.getActivityName(), e);
					comment.add("Error generating 2D Coordinate!");
				}
			}
			for (int i = 0; i < som2D.getMoleculeCount(); i++) {
				try {
					cmlChemFile = CMLChemFileWrapper.wrapInChemModel(som2D.getMolecule(i));
					dataArray.addAll(CDKObjectHandler.getBytesList(CMLChemFileWrapper.wrapInChemModelList(cmlChemFile)));
				} catch (Exception e) {
					ErrorLogger.getInstance().writeError("Error while creating/serializing output data!", this.getActivityName(),
							e);
					throw new CDKTavernaException(this.getActivityName(), "Error while creating/serializing output data!");
				}
			}
		}
		T2Reference containerRef = referenceService.register(dataArray, 1, true, context);
		outputs.put(this.RESULT_PORTS[0], containerRef);
		comment.add("done");
		// Return results
		return outputs;
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

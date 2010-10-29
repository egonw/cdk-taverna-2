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
package org.openscience.cdk.applications.taverna.stringconverter;

/**
 * Class which represents the SMILES to structure converter activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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

public class SMILESToStructureConverterActivity extends AbstractCDKActivity implements IFileReader {

	public static final String SMILES_CONVERTER_ACTIVITY = "SMILES to Structures Converter";

	/**
	 * Creates a new instance.
	 */
	public SMILESToStructureConverterActivity() {
		this.INPUT_PORTS = new String[] { "SMILES" };
		this.RESULT_PORTS = new String[] { "Structures", "Not Converted" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, String.class);
	}

	@Override
	protected void addOutputPorts() {
		for (String name : this.RESULT_PORTS) {
			addOutput(name, 1);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		List<byte[]> dataList = new ArrayList<byte[]>();
		LinkedList<String> notConverted = new LinkedList<String>();
		List<String> cmlList = (List<String>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), String.class,
				context);
		for (String cml : cmlList) {
			try {
				IMoleculeSet som = null;
				try {
					SMILESReader reader = new SMILESReader(new ByteArrayInputStream(cml.getBytes()));
					som = (IMoleculeSet) reader.read(new MoleculeSet());
					reader.close();
				} catch (Exception e) {
					ErrorLogger.getInstance().writeError("Error while reading SMILES file!", this.getActivityName(), e);
					throw new CDKTavernaException(this.getActivityName(), "Error while reading SMILES file!");
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
					}
				}
				for (int i = 0; i < som2D.getMoleculeCount(); i++) {
					try {
						CMLChemFile cmlChemFile = CMLChemFileWrapper.wrapInChemModel(som2D.getMolecule(i));
						dataList.add(CDKObjectHandler.getBytes(cmlChemFile));
					} catch (Exception e) {
						ErrorLogger.getInstance().writeError("Error creating output data!", this.getActivityName(), e);
						throw new CDKTavernaException(this.getActivityName(), "Error creating output data!");
					}
				}
			} catch (Exception e) {
				notConverted.add(cml);
				ErrorLogger.getInstance().writeError("Error converting SMILES!", this.getActivityName(), e);
			}
		}
		if (dataList.isEmpty()) {
			throw new CDKTavernaException(this.getActivityName(), "Error while converting SMILES");
		}
		T2Reference containerRef = referenceService.register(dataList, 1, true, context);
		outputs.put(this.RESULT_PORTS[0], containerRef);
		containerRef = referenceService.register(notConverted, 1, true, context);
		outputs.put(this.RESULT_PORTS[1], containerRef);
		// Return results
		return outputs;
	}

	@Override
	public String getActivityName() {
		return SMILESToStructureConverterActivity.SMILES_CONVERTER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + SMILESToStructureConverterActivity.SMILES_CONVERTER_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.STRING_CONVERTER_FOLDER_NAME;
	}

}

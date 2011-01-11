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
 */package org.openscience.cdk.applications.taverna.miscellaneous;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * Class which represents the hueckel aromaticity detector activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class AromaticityDetectorActivity extends AbstractCDKActivity {

	public static final String AROMATICITY_DETECTOR_ACTIVITY = "Hueckel Aromaticity Detector";

	/**
	 * Creates a new instance.
	 */
	public AromaticityDetectorActivity() {
		this.INPUT_PORTS = new String[] { "Structures", };
		this.OUTPUT_PORTS = new String[] { "Aromatic Structures", "NON Aromatic Structures", "All Modified Structures" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
		addOutput(this.OUTPUT_PORTS[1], 1);
		addOutput(this.OUTPUT_PORTS[2], 1);
	}

	@Override
	public String getActivityName() {
		return AromaticityDetectorActivity.AROMATICITY_DETECTOR_ACTIVITY;
	}

	@Override
	public String getDescription() {
		return "Descriptions: " + AromaticityDetectorActivity.AROMATICITY_DETECTOR_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		return new HashMap<String, Object>();
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.MISCELLANEOUS_FOLDER_NAME;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		List<CMLChemFile> aromaticMoleculesList = new ArrayList<CMLChemFile>();
		List<CMLChemFile> nonAromaticMoleculesList = new ArrayList<CMLChemFile>();
		List<CMLChemFile> allMoleculesList = new ArrayList<CMLChemFile>();
		List<CMLChemFile> chemFileList = new ArrayList<CMLChemFile>();
		List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), byte[].class,
				context);
		try {
			chemFileList = CDKObjectHandler.getChemFileList(dataArray);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.OBJECT_DESERIALIZATION_ERROR,
					this.getConfiguration().getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		IAtomContainer[] containers;
		try {
			containers = CMLChemFileWrapper.convertCMLChemFileListToAtomContainerArray(chemFileList);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.CML_FILE_CONVERSION_ERROR,
					this.getConfiguration().getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		for (int i = 0; i < containers.length; i++) {
			try {
				boolean isAromatic = CDKHueckelAromaticityDetector.detectAromaticity(containers[i]);
				CMLChemFile chemFile = CMLChemFileWrapper.wrapAtomContainerInChemModel(containers[i]);
				if (isAromatic) {
					aromaticMoleculesList.add(chemFile);
				} else {
					nonAromaticMoleculesList.add(chemFile);
				}
				allMoleculesList.add(chemFile);
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError("Error detecting Hueckel aromaticity!", this.getActivityName(), e);
			}
		}
		// Congfigure output
		try {
			T2Reference containerRef = referenceService.register(CDKObjectHandler.getBytesList(aromaticMoleculesList), 1, true,
					context);
			outputs.put(this.OUTPUT_PORTS[0], containerRef);
			containerRef = referenceService.register(CDKObjectHandler.getBytesList(nonAromaticMoleculesList), 1, true, context);
			outputs.put(this.OUTPUT_PORTS[1], containerRef);
			containerRef = referenceService.register(CDKObjectHandler.getBytesList(allMoleculesList), 1, true, context);
			outputs.put(this.OUTPUT_PORTS[2], containerRef);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.OUTPUT_PORT_CONFIGURATION_ERROR,
					this.getConfiguration().getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		return outputs;
	}
}

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
 * Class which represents molecule connectivity checker activity.
 * 
 * @author kalai
 */
import java.io.IOException;
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
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.reaction.enumerator.tools.ErrorLogger;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

public class MoleculeConnectivityCheckerActivity extends AbstractCDKActivity {

	public static final String CONNECTIVITY_CHECKER_ACTIVITY = "Molecule Connectivity Checker";

	public MoleculeConnectivityCheckerActivity() {
		this.INPUT_PORTS = new String[] { "Structures" };
		this.RESULT_PORTS = new String[] { "Accepted", "Rejected" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);

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
		ArrayList<CMLChemFile> accepted = new ArrayList<CMLChemFile>();
		ArrayList<CMLChemFile> rejected = new ArrayList<CMLChemFile>();
		IMolecule nonPartitionedMolecule = null;
		IMoleculeSet molSet = null;
		List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), byte[].class,
				context);
		List<CMLChemFile> chemFileList = null;
		try {
			chemFileList = CDKObjectHandler.getChemFileList(dataArray);
		} catch (Exception e) {
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		Integer cutoffvalue = (Integer) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_ATOM_COUNT_CUTOFF);
		for (CMLChemFile cml : chemFileList) {
			List<IAtomContainer> moleculeList = ChemFileManipulator.getAllAtomContainers(cml);
			for (IAtomContainer atomContainer : moleculeList) {
				nonPartitionedMolecule = (IMolecule) atomContainer;
				// TODO Copy all properties?
				Map<Object, Object> properties = nonPartitionedMolecule.getProperties();
				molSet = ConnectivityChecker.partitionIntoMolecules(nonPartitionedMolecule);
				for (IAtomContainer molecule : molSet.molecules()) {
					molecule.setProperties(properties);
					if (molecule.getAtomCount() > cutoffvalue) {
						accepted.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(molecule));

					} else {
						rejected.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(molecule));

					}
				}

			}
		}
		try {
			List<byte[]> acceptedList = CDKObjectHandler.getBytesList(accepted);
			T2Reference containerRef = referenceService.register(acceptedList, 1, true, context);

			outputs.put(this.RESULT_PORTS[0], containerRef);
		} catch (IOException ex) {

			ErrorLogger.getInstance().writeError(CDKTavernaException.OUTPUT_PORT_CONFIGURATION_ERROR, this.getActivityName(), ex);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.OUTPUT_PORT_CONFIGURATION_ERROR);
		}

		try {

			List<byte[]> rejectedList = CDKObjectHandler.getBytesList(rejected);
			T2Reference containerRef2 = referenceService.register(rejectedList, 1, true, context);
			outputs.put(this.RESULT_PORTS[1], containerRef2);
		} catch (IOException ex) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.OUTPUT_PORT_CONFIGURATION_ERROR, this.getActivityName(), ex);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.OUTPUT_PORT_CONFIGURATION_ERROR);
		}
		// Return results
		return outputs;
	}

	@Override
	public String getActivityName() {
		return MoleculeConnectivityCheckerActivity.CONNECTIVITY_CHECKER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_ATOM_COUNT_CUTOFF, 6);
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + MoleculeConnectivityCheckerActivity.CONNECTIVITY_CHECKER_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.CURATION_FOLDER_NAME;
	}
}

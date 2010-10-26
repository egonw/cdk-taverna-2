/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openscience.cdk.applications.taverna.curation;

/**
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

		List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]),
				byte[].class, context);
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
				molSet = ConnectivityChecker.partitionIntoMolecules(nonPartitionedMolecule);
				for (IAtomContainer molecule : molSet.molecules()) {

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

			ErrorLogger.getInstance().writeError("Error while configurating output port!", this.getActivityName(), ex);
			throw new CDKTavernaException(this.getActivityName(), "Error while configurating output port!");
		}

		try {

			List<byte[]> rejectedList = CDKObjectHandler.getBytesList(rejected);
			T2Reference containerRef2 = referenceService.register(rejectedList, 1, true, context);
			outputs.put(this.RESULT_PORTS[1], containerRef2);
		} catch (IOException ex) {
			ErrorLogger.getInstance().writeError("Error while configurating output port!", this.getActivityName(), ex);
			throw new CDKTavernaException(this.getActivityName(), "Error while configurating output port!");
		}
		comment.add("done");
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

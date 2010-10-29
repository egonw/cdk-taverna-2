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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

public class CurateStrangeElementsActivity extends AbstractCDKActivity {

	public static final String SDF_CURATOR_ACTIVITY = "Curate Strange Elements";
	private String[] check = { "C", "H", "N", "O", "P", "S", "Cl", "F", "As", "Se", "Br", "I", "B" };
	private HashSet<String> symbols2Check;

	public CurateStrangeElementsActivity() {
		this.INPUT_PORTS = new String[] { "Structures" };
		this.RESULT_PORTS = new String[] { "CURATED", "DISCARDED" };
		symbols2Check = new HashSet<String>(Arrays.asList(check));
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

		List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), byte[].class,
				context);
		List<CMLChemFile> chemFileList = null;
		ArrayList<CMLChemFile> curated = new ArrayList<CMLChemFile>();
		ArrayList<CMLChemFile> discarded = new ArrayList<CMLChemFile>();
		try {
			chemFileList = CDKObjectHandler.getChemFileList(dataArray);
		} catch (Exception e) {
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}

		for (CMLChemFile cml : chemFileList) {

			List<IAtomContainer> moleculeList = ChemFileManipulator.getAllAtomContainers(cml);
			for (IAtomContainer atomContainer : moleculeList) {

				if (shouldRemoveMolecule(atomContainer) == false) {
					curated.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(atomContainer));

				} else {
					discarded.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(atomContainer));
				}
			}
		}
		try {
			List<byte[]> curatedList = CDKObjectHandler.getBytesList(curated);
			T2Reference containerRef = referenceService.register(curatedList, 1, true, context);

			outputs.put(this.RESULT_PORTS[0], containerRef);
		} catch (IOException ex) {

			ErrorLogger.getInstance().writeError("Error while configurating output port!", this.getActivityName(), ex);
			throw new CDKTavernaException(this.getActivityName(), "Error while configurating output port!");
		}

		try {

			List<byte[]> discardedList = CDKObjectHandler.getBytesList(discarded);
			T2Reference containerRef2 = referenceService.register(discardedList, 1, true, context);
			outputs.put(this.RESULT_PORTS[1], containerRef2);
		} catch (IOException ex) {
			ErrorLogger.getInstance().writeError("Error while configurating output port!", this.getActivityName(), ex);
			throw new CDKTavernaException(this.getActivityName(), "Error while configurating output port!");
		}
		return outputs;
	}

	private boolean shouldRemoveMolecule(IAtomContainer molecule) {
		boolean removeMolecule = false;
		String element;

		for (IAtom atom : molecule.atoms()) {

			element = atom.getSymbol();

			if (!symbols2Check.contains(element)) {
				removeMolecule = true;
				break;
			}
		}
		return removeMolecule;
	}

	@Override
	public String getActivityName() {
		return CurateStrangeElementsActivity.SDF_CURATOR_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + CurateStrangeElementsActivity.SDF_CURATOR_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.CURATION_FOLDER_NAME;
	}

}

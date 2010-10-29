/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openscience.cdk.applications.taverna.curation;

/**
 *
 * @author kalai
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.signature.AtomSignature;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

public class AtomSignatureActivity extends AbstractCDKActivity {

	public static final String ATOM_SIGNATURE_ACTIVITY = "Generate Atom Signatures";

	public AtomSignatureActivity() {
		this.INPUT_PORTS = new String[] { "Structures" };
		this.RESULT_PORTS = new String[] { "Atom Signatures" };

	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {

		addOutput(this.RESULT_PORTS[0], 1);
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
		Integer height = (Integer) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_ATOM_SIGNATURE_HEIGHT);
		ArrayList<String> allAtomSignatures = new ArrayList<String>();
		try {
			chemFileList = CDKObjectHandler.getChemFileList(dataArray);
		} catch (Exception e) {
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		for (CMLChemFile cml : chemFileList) {
			List<IAtomContainer> moleculeList = ChemFileManipulator.getAllAtomContainers(cml);
			for (IAtomContainer atomContainer : moleculeList) {
				if (atomContainer.getProperty(CDKTavernaConstants.MOLECULEID) == null) {
					throw new CDKTavernaException(this.getActivityName(), "Molecule is not tagged with an UUID!");
				}
				UUID uuid = (UUID) atomContainer.getProperty(CDKTavernaConstants.MOLECULEID);
				for (IAtom atom : atomContainer.atoms()) {
					AtomSignature atomSignature = new AtomSignature(atom, height, atomContainer);
					String signature = uuid.toString() + " - " + atomSignature.toCanonicalString();
					allAtomSignatures.add(signature);
				}
			}
		}
		T2Reference containerRef = referenceService.register(allAtomSignatures, 1, true, context);
		outputs.put(this.RESULT_PORTS[0], containerRef);

		return outputs;
	}

	@Override
	public String getActivityName() {
		return AtomSignatureActivity.ATOM_SIGNATURE_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_ATOM_SIGNATURE_HEIGHT, 2);
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + AtomSignatureActivity.ATOM_SIGNATURE_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.CURATION_FOLDER_NAME;
	}
}

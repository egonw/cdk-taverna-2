/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openscience.cdk.applications.taverna.curation;

/**
 *
 * @author kalai
 */
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import org.openscience.cdk.Atom;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

public class CurateStrangeElementsActivity extends AbstractCDKActivity {

	public static final String SDF_CURATOR_ACTIVITY = "Curate strange elements";
	String check = "C H N O P S Cl F As Se Br I";
	String boron = "B";

	public CurateStrangeElementsActivity() {
		this.INPUT_PORTS = new String[] { "Structures" };
		this.RESULT_PORTS = new String[] { "CURATED", "DISCARDED" };
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

		List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]),
				byte[].class, context);
		List<CMLChemFile> chemFileList = null;
		try {
			chemFileList = CDKObjectHandler.getChemFileList(dataArray);
		} catch (Exception e) {
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}

		for (CMLChemFile cml : chemFileList) {

			List<IAtomContainer> moleculeList = ChemFileManipulator.getAllAtomContainers(cml);
			for (IAtomContainer atomContainer : moleculeList) {

				if (shouldRemoveMolecule(atomContainer) == false) {
					T2Reference containerRef = referenceService.register(atomContainer, 1, true, context);
					outputs.put(this.RESULT_PORTS[0], containerRef);
				} else {
					T2Reference containerRef2 = referenceService.register(atomContainer, 1, true, context);
					outputs.put(this.RESULT_PORTS[1], containerRef2);

				}
			}
		}
		comment.add("done");
		// Return results
		return outputs;
	}

	private boolean shouldRemoveMolecule(IAtomContainer molecule) {
		boolean removeMolecule = false;
		String element;

		for (IAtom atom : molecule.atoms()) {

			element = atom.getSymbol();

			boolean checkcontains = check.contains(element);
			if (checkcontains == true & !element.equals(boron)) {
				continue;

			} else if (checkcontains == false | element.equals(boron)) {

				List<IAtom> totalconnectedAtoms = molecule.getConnectedAtomsList(atom);
				int NumberofAtomsConnected = totalconnectedAtoms.size();
				if (NumberofAtomsConnected == 1) {

					int bondorderSum = (int) AtomContainerManipulator.getBondOrderSum(molecule, atom);
					for (IAtom connectedAtom : totalconnectedAtoms) {

						String connectedElement = connectedAtom.getSymbol();
						Atom H = new Atom("H");
						int atomNumberofConnectedAtom;

						int SEatomNUmber;
						SEatomNUmber = molecule.getAtomNumber(atom);
						switch (bondorderSum) {
						case 1:

							atomNumberofConnectedAtom = molecule.getAtomNumber(connectedAtom);
							molecule.removeBond(atom, connectedAtom);
							molecule.removeAtom(SEatomNUmber);
							molecule.setAtom(SEatomNUmber, H);
							molecule.addBond(atomNumberofConnectedAtom, SEatomNUmber, IBond.Order.SINGLE);

							break;
						case 2:

							atomNumberofConnectedAtom = molecule.getAtomNumber(connectedAtom);
							molecule.removeBond(atom, connectedAtom);
							molecule.removeAtom(SEatomNUmber);
							molecule.setAtom(SEatomNUmber, H);
							molecule.addAtom(H);
							// int secondHatomNUmber =
							// molecule.getAtomNumber(H);
							int secondHatomNUmber = 100;
							molecule.addBond(atomNumberofConnectedAtom, SEatomNUmber, IBond.Order.SINGLE);
							molecule.addBond(atomNumberofConnectedAtom, secondHatomNUmber, IBond.Order.SINGLE);

							break;
						default:
							break;
						}

					}

				} else if (NumberofAtomsConnected > 1) {
					removeMolecule = true;
					continue;

				}

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

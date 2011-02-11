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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.interfaces.IRing;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.ringsearch.SSSRFinder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.BondManipulator;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

/**
 * Removes sugar groups from molecules.
 * 
 * @author kalai
 * 
 */
public class SugarGroupRemoverActivity extends AbstractCDKActivity {

	public static final String REMOVE_SUGAR_GROUPS_ACTIVITY = "Remove Sugar groups";

	public SugarGroupRemoverActivity() {
		this.INPUT_PORTS = new String[] { "Structures" };
		this.OUTPUT_PORTS = new String[] { "Curated", "Discarded" };

	}

	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
	}

	protected void addOutputPorts() {
		for (String name : this.OUTPUT_PORTS) {
			addOutput(name, 1);
		}
	}

	List<IAtomContainer> sugarChains = new ArrayList<IAtomContainer>();

	public void work() throws Exception {
		// Get input
		String[] smilesList = { "C(C(C(C(C(C=O)O)O)O)O)O", "C(C(CC(C(CO)O)O)O)(O)=O", "C(C(C(CC(=O)O)O)O)O",
				"C(C(C(C(C(CO)O)O)O)=O)O", "C(C(C(C(C(CO)O)O)O)O)O", "C(C(C(C(CC=O)O)O)O)O" };
		List<CMLChemFile> chemFileList = this.getInputAsList(this.INPUT_PORTS[0], CMLChemFile.class);
		// Do work
		ArrayList<CMLChemFile> curated = new ArrayList<CMLChemFile>();
		ArrayList<CMLChemFile> discarded = new ArrayList<CMLChemFile>();
		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
		try {
			for (String smiles : smilesList) {
				sugarChains.add(sp.parseSmiles(smiles));
			}
		} catch (InvalidSmilesException ex) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.ERROR_WHILE_PARSING_SMILES,
					this.getActivityName(), ex);
		}
		for (CMLChemFile cml : chemFileList) {
			List<IAtomContainer> moleculeList = ChemFileManipulator.getAllAtomContainers(cml);
			for (IAtomContainer molecule : moleculeList) {
				SSSRFinder molecule_ring = new SSSRFinder(molecule);
				IRingSet ringset = molecule_ring.findSSSR();
				for (IAtomContainer one_ring : ringset.atomContainers()) {
					IMolecularFormula molecularFormula = MolecularFormulaManipulator.getMolecularFormula(one_ring);
					String formula = MolecularFormulaManipulator.getString(molecularFormula);
					IBond.Order bondorder = AtomContainerManipulator.getMaximumBondOrder(one_ring);
					if (formula.equals("C5O") | formula.equals("C4O") | formula.equals("C6O")) {
						if (IBond.Order.SINGLE.equals(bondorder)) {
							if (shouldRemoveRing(one_ring, molecule, ringset) == true) {
								for (IAtom atom : one_ring.atoms()) {
									{
										molecule.removeAtomAndConnectedElectronContainers(atom);
									}
								}
							}

						}
					}
				}
				// TODO Copy all properties?
				Map<Object, Object> properties = molecule.getProperties();
				IMoleculeSet molset = ConnectivityChecker.partitionIntoMolecules(molecule);
				for (int i = 0; i < molset.getMoleculeCount(); i++) {
					molset.getMolecule(i).setProperties(properties);
					int size = molset.getMolecule(i).getBondCount();
					if (size >= 5) {
						if (hasSugarChains(molset.getMolecule(i), ringset.getAtomContainerCount()) == false) {
							curated.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(molset.getMolecule(i)));
						} else {
							discarded.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(molset.getMolecule(i)));
						}

					}
				}
			}
		}
		// Set output
		this.setOutputAsObjectList(curated, this.OUTPUT_PORTS[0]);
		this.setOutputAsObjectList(discarded, this.OUTPUT_PORTS[1]);
	}

	private boolean shouldRemoveRing(IAtomContainer ring, IAtomContainer molecule, IRingSet ringset) {
		boolean shouldRemoveRing = true;
		IAtomContainer sugarRing = ring;
		IRingSet sugarRingsSet = ringset;
		IRingSet connectedRings = null;
		connectedRings = sugarRingsSet.getConnectedRings((IRing) ring);
		List<IBond> bonds = new ArrayList<IBond>();
		for (IAtom atom : sugarRing.atoms()) {
			bonds = molecule.getConnectedBondsList(atom);
		}
		if (IBond.Order.SINGLE.equals(BondManipulator.getMaximumBondOrder(bonds))
				&& connectedRings.getAtomContainerCount() == 0) {
			return shouldRemoveRing;
		} else {
			return shouldRemoveRing = false;
		}
	}

	public boolean hasSugarChains(IAtomContainer molecule, int count) {
		IAtomContainer target = molecule;
		boolean isSubstructure = false;
		int ringCount = count;
		if (ringCount == 0) {
			try {
				for (IAtomContainer atomcontainer : sugarChains) {
					IAtomContainer query = atomcontainer;
					isSubstructure = UniversalIsomorphismTester.isSubgraph(target, query);
					return isSubstructure;
				}
			} catch (CDKException ex) {
				ErrorLogger.getInstance().writeError(CDKTavernaException.ERROR_DURING_SUBSTRUCTURE_SEARCH,
						this.getActivityName(), ex);
			}

		}
		return false;
	}

	public String getActivityName() {
		return SugarGroupRemoverActivity.REMOVE_SUGAR_GROUPS_ACTIVITY;
	}

	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	public String getDescription() {
		return "Description: " + SugarGroupRemoverActivity.REMOVE_SUGAR_GROUPS_ACTIVITY;
	}

	public String getFolderName() {
		return CDKTavernaConstants.CURATION_FOLDER_NAME;
	}
}

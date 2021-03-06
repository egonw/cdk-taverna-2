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
package org.openscience.cdk.applications.taverna.reactionenumerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.openscience.cdk.Reaction;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainer;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainerCreator;
import org.openscience.cdk.reaction.enumerator.tools.Tools;
import org.openscience.cdk.reaction.enumerator.tools.VariableRegionChecker;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 * Class which represents the reaction enumerator subgraph filter activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class ReactionEnumeratorSubgraphFilterActivity extends AbstractCDKActivity {

	public static final String REACTION_ENUMERATOR_SUBGRAPH_FILTER_ACTIVITY = "Reaction Enumerator Subgraph Filter";

	/**
	 * Creates a new instance.
	 */
	public ReactionEnumeratorSubgraphFilterActivity() {
		this.INPUT_PORTS = new String[] { "Structures", "Query Structure" };
		this.OUTPUT_PORTS = new String[] { "Calculated Structures", "NOT Calculated Structures" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
		addInput(this.INPUT_PORTS[1], 0, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		for (String name : this.OUTPUT_PORTS) {
			addOutput(name, 1);
		}
	}

	@Override
	public String getActivityName() {
		return ReactionEnumeratorSubgraphFilterActivity.REACTION_ENUMERATOR_SUBGRAPH_FILTER_ACTIVITY;
	}

	@Override
	public String getDescription() {
		return "Descriptions: " + ReactionEnumeratorSubgraphFilterActivity.REACTION_ENUMERATOR_SUBGRAPH_FILTER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		return new HashMap<String, Object>();
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.REACTION_ENUMERATOR_FOLDER_NAME;
	}

	@Override
	public void work() throws Exception {
		// Get input
		List<CMLChemFile> inputList = this.getInputAsList(this.INPUT_PORTS[0], CMLChemFile.class);
		CMLChemFile queryChemFile = this.getInputAsObject(this.INPUT_PORTS[1], CMLChemFile.class);
		// Do work
		List<CMLChemFile> calculatedList = new ArrayList<CMLChemFile>();
		List<CMLChemFile> notCalculatedList = new ArrayList<CMLChemFile>();
		IAtomContainer queryMolecule = null;
		try {
			queryMolecule = CMLChemFileWrapper.wrapChemModelInAtomContainer(queryChemFile);
		} catch (Exception e) {
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		for (Iterator<CMLChemFile> iter = inputList.iterator(); iter.hasNext();) {
			CMLChemFile file = iter.next();
			List<IAtomContainer> moleculeList = ChemFileManipulator.getAllAtomContainers(file);
			for (IAtomContainer molecule : moleculeList) {
				try {
					IMolecule queryClone = (IMolecule) queryMolecule.clone();
					IReaction reaction = new Reaction();
					reaction.addReactant(queryClone);
					reaction = VariableRegionChecker.convertAnyAtomData(reaction);
					VariableRegionChecker tmpVariableRegionChecker = new VariableRegionChecker();
					LinkedList<IAtomContainer[]> reactants = new LinkedList<IAtomContainer[]>();
					reactants.add(new IAtomContainer[] { molecule });
					reactants = tmpVariableRegionChecker.checkForVariableRegions(reaction, reactants);
					if (reactants.get(0).length == 0) {
						notCalculatedList.add(file);
					} else {
						String variableRegionData = (String) reactants.get(0)[0]
								.getProperty(VariableRegionChecker.VARIABLEREGIONDATA);
						if (variableRegionData != null) {
							VariableRegionChecker.expandMoleculeFromVariableRegionData(variableRegionData, queryClone);
						}
						IAtomContainer container = Tools.addExplicitHydrogens(queryClone);
						QueryAtomContainer query = QueryAtomContainerCreator
								.createAnyAtomForPseudoAtomQueryContainer(container);
						if (UniversalIsomorphismTester.isSubgraph(molecule, query)) {
							calculatedList.add(file);
						} else {
							notCalculatedList.add(file);
						}
					}
				} catch (Exception e) {
					notCalculatedList.add(file);
					ErrorLogger.getInstance().writeError("Error during test for subgraph isomorphism!",
							this.getActivityName(), e);
				}
			}
		}
		// Set output
		this.setOutputAsObjectList(calculatedList, this.OUTPUT_PORTS[0]);
		this.setOutputAsObjectList(notCalculatedList, this.OUTPUT_PORTS[1]);
	}

}

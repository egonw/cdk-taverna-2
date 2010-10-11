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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.Reaction;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.applications.taverna.interfaces.IPortNumber;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.reaction.enumerator.ReactionEnumerator;
import org.openscience.cdk.reaction.enumerator.tools.ErrorLogger;

/**
 * Class which represents the reaction enumerator activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class ReactionEnumeratorActivity extends AbstractCDKActivity {

	public static final String REACTION_ENUMERATOR_ACTIVITY = "Reaction Enumerator";
	public static final String REACTANT_PORT = "Reactant";
	public static final String REACTION_PORT = "Reaction";
	public static final String RESULT_PORT = "Resulting Reactions";

	public static final String USE_MULTI_MATCH_CHECKER = "USE_MULTI_MATCH_CHECKER";
	public static final String USE_VARIABLE_REGION_CHECKER = "USE_VARIABLE_REGION_CHECKER";

	@Override
	protected void addInputPorts() {
		int numberOfPorts = (Integer) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_REACTANT_PORTS);
		for (int i = 0; i < numberOfPorts; i++) {
			addInput(ReactionEnumeratorActivity.REACTANT_PORT + " " + (i + 1), 1, true, null, byte[].class);
		}
		addInput(ReactionEnumeratorActivity.REACTION_PORT, 1, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(ReactionEnumeratorActivity.RESULT_PORT, 1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback) {
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		LinkedList<IAtomContainer[]> reactants = new LinkedList<IAtomContainer[]>();
		IReaction reaction = null;
		List<byte[]> dataList;
		int numberOfPorts = (Integer) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_REACTANT_PORTS);
		ReactionEnumerator enumerator = new ReactionEnumerator();
		// TODO remove
		enumerator.setCreate2DCoordinates(true);
//		ErrorLogger.getInstance().setLogMolecules(false);
		enumerator.setUseMultiMatchChecker(this.getConfiguration().getAdditionalProperty(
				ReactionEnumeratorActivity.USE_MULTI_MATCH_CHECKER) != null);
		enumerator.setUseVariableRegionChecker(this.getConfiguration().getAdditionalProperty(
				ReactionEnumeratorActivity.USE_VARIABLE_REGION_CHECKER) != null);
		try {
			// get reactants
			for (int i = 0; i < numberOfPorts; i++) {
				dataList = (List<byte[]>) referenceService.renderIdentifier(
						inputs.get(ReactionEnumeratorActivity.REACTANT_PORT + " " + (i + 1)), byte[].class, context);
				List<CMLChemFile> list = null;
				try {
					list = CDKObjectHandler.getChemFileList(dataList);
				} catch (Exception e) {
					throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
				}
				IAtomContainer[] containerArray = new AtomContainer[list.size()];
				for (int j = 0; j < list.size(); j++) {
					containerArray[j] = CMLChemFileWrapper.wrapChemModelInAtomContainer(list.get(j));
				}
				reactants.add(containerArray);
			}
			// get reaction
			List<byte[]> data = (List<byte[]>) referenceService.renderIdentifier(inputs.get(ReactionEnumeratorActivity.REACTION_PORT),
					byte[].class, context);
			try {
			reaction = CDKObjectHandler.getReactionList(data).get(0);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// enumerate results
			Reaction[] results = enumerator.enumerateReactions(reaction, reactants);
			// prepare output data
			dataList = new ArrayList<byte[]>();
			if (results != null && results.length != 0) {
				for (int i = 0; i < results.length; i++) {
					dataList.add(CDKObjectHandler.getBytes(results[i]));
				}
			}
			T2Reference containerRef = referenceService.register(dataList, 1, true, context);
			outputs.put(RESULT_PORT, containerRef);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO Exception handling
		}
		return outputs;
	}

	@Override
	public String getActivityName() {
		return ReactionEnumeratorActivity.REACTION_ENUMERATOR_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(ReactionEnumeratorActivity.USE_MULTI_MATCH_CHECKER, true);
		properties.put(ReactionEnumeratorActivity.USE_VARIABLE_REGION_CHECKER, true);
		properties.put(CDKTavernaConstants.PROPERTY_REACTANT_PORTS, new Integer(2));
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + ReactionEnumeratorActivity.REACTION_ENUMERATOR_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.REACTION_ENUMERATOR_FOLDER_NAME;
	}

}

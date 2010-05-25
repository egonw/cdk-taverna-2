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
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.Constants;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IReaction;

import enumerator.main.ReactionEnumerator;

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

	@Override
	protected void addInputPorts() {
		int numberOfPorts = (Integer) this.getConfiguration().getAdditionalProperty(Constants.PROPERTY_REACTANT_PORTS);
		for (int i = 0; i < numberOfPorts; i++) {
			addInput(ReactionEnumeratorActivity.REACTANT_PORT + " " + (i + 1), 1, true, null, byte[].class);
		}
		addInput(ReactionEnumeratorActivity.REACTION_PORT, 0, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(ReactionEnumeratorActivity.RESULT_PORT, 1);
	}

	@Override
	protected Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback) {
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		LinkedList<IAtomContainer[]> reactants = new LinkedList<IAtomContainer[]>();
		IReaction reaction;
		List<byte[]> dataList;
		int numberOfPorts = (Integer) this.getConfiguration().getAdditionalProperty(Constants.PROPERTY_REACTANT_PORTS);
		ReactionEnumerator enumerator = new ReactionEnumerator();
		try {
			// get reactants
			for (int i = 0; i < numberOfPorts; i++) {
				dataList = (List<byte[]>) referenceService.renderIdentifier(inputs.get(ReactionEnumeratorActivity.REACTANT_PORT
						+ " " + (i + 1)), byte[].class, context);
				IAtomContainer[] containerArray = new AtomContainer[dataList.size()];
				for (int j = 0; j < dataList.size(); j++) {
					containerArray[j] = CMLChemFileWrapper.wrapChemModelInAtomContainer((CMLChemFile) CDKObjectHandler
							.getObject(dataList.get(j)));
				}
				reactants.add(containerArray);
			}
			// get reaction
			byte[] data = (byte[]) referenceService.renderIdentifier(inputs.get(ReactionEnumeratorActivity.REACTION_PORT),
					byte[].class, context);
			reaction = (IReaction) CDKObjectHandler.getObject(data);
			// enumerate results
			Reaction[] results = enumerator.enumerateReactions(reaction, reactants);
			// prepare output data
			dataList = new ArrayList<byte[]>();
			for (int i = 0; i < results.length; i++) {
				dataList.add(CDKObjectHandler.getBytes(results[i]));
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
		properties.put(Constants.PROPERTY_REACTANT_PORTS, new Integer(2));
		return properties;
	}

	@Override
	public String getConfigurationPanelClass() {
		return "ReactionEnumeratorConfigurationPanel";
	}

	@Override
	public String getDescription() {
		return "Description: " + ReactionEnumeratorActivity.REACTION_ENUMERATOR_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return Constants.REACTION_ENUMERATOR_FOLDER_NAME;
	}

}

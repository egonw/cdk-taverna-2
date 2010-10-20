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
package org.openscience.cdk.applications.taverna.miscellaneous;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.interfaces.IReaction;

/**
 * Class which represents the reaction splitter class. It splits the given reactions depending on their number of reactants.
 * 
 * @author Andreas Truszkowski
 *
 */
public class ReactionSplitterActivity extends AbstractCDKActivity {

	public static final String REACTION_SPLITTER_ACTIVITY = "Reaction Splitter";

	/**
	 * Creates a new instance.
	 */
	public ReactionSplitterActivity() {
		this.INPUT_PORTS = new String[] { "Reactions" };
		this.RESULT_PORTS = new String[] { "1 Reactant", "2 Reactants", "3 Reactants", "greater 3 Reactants" };
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
	public String getActivityName() {
		return ReactionSplitterActivity.REACTION_SPLITTER_ACTIVITY;
	}

	@Override
	public String getDescription() {
		return "Descriptions: " + ReactionSplitterActivity.REACTION_SPLITTER_ACTIVITY;
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
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		List<IReaction> reactionList;
		LinkedList<IReaction>[] resultList = (LinkedList<IReaction>[]) Array.newInstance(LinkedList.class, 4);
		for (int i = 0; i < resultList.length; i++) {
			resultList[i] = new LinkedList<IReaction>();
		}
		List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), byte[].class,
				context);
		try {
			reactionList = CDKObjectHandler.getReactionList(dataArray);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error while deserializing object!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		for (IReaction r : reactionList) {
			if (r.getReactantCount() < 4 && r.getReactantCount() > 0) {
				resultList[r.getReactantCount() - 1].add(r);
			} else {
				resultList[3].add(r);
			}
		}
		comment.add("Calculation done;");
		// Congfigure output
		try {
			for (int i = 0; i < resultList.length; i++) {
				List<byte[]> dataObjects = CDKObjectHandler.getBytesList(resultList[i]);
				T2Reference containerRef = referenceService.register(dataObjects, 1, true, context);
				outputs.put(this.RESULT_PORTS[i], containerRef);
			}
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error while configurating output port!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), "Error while configurating output port!");
		}
		return outputs;
	}
}

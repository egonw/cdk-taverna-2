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
import org.openscience.cdk.applications.taverna.interfaces.IPortNumber;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IReaction;

public class ReactionReactantSplitterActivity extends AbstractCDKActivity implements IPortNumber {

	public static final String REACTION_REACTANT_SPLITTER_ACTIVITY = "Reaction Reactant Splitter";

	public ReactionReactantSplitterActivity() {
		this.INPUT_PORTS = new String[] { "Reaction" };

	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		int numberOfPorts = (Integer) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_REACTANT_PORTS);
		this.RESULT_PORTS = new String[numberOfPorts];
		for (int i = 0; i < numberOfPorts; i++) {
			this.RESULT_PORTS[i] = "Reactant " + (i + 1);
			addOutput(this.RESULT_PORTS[i], 0);
		}
	}

	@Override
	public String getActivityName() {
		return ReactionReactantSplitterActivity.REACTION_REACTANT_SPLITTER_ACTIVITY;
	}

	@Override
	public String getDescription() {
		return "Descriptions: " + ReactionReactantSplitterActivity.REACTION_REACTANT_SPLITTER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_REACTANT_PORTS, 2);
		return properties;
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
		int numberOfPorts = (Integer) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_REACTANT_PORTS);
		CMLChemFile[] resultList = new CMLChemFile[numberOfPorts];
		for (int i = 0; i < resultList.length; i++) {
			resultList[i] = new CMLChemFile();
		}
		List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), byte[].class,
				context);
		try {
			reactionList = CDKObjectHandler.getReactionList(dataArray);
		} catch (Exception e) {
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		for (int i = 0; i < numberOfPorts; i++) {
			IAtomContainer container = reactionList.get(0).getReactants().getAtomContainer(i);
			CMLChemFile chemFile = CMLChemFileWrapper.wrapAtomContainerInChemModel(container);
			resultList[i] = chemFile;
		}
		comment.add("Calculation done;");
		// Congfigure output
		try {
			for (int i = 0; i < resultList.length; i++) {
				byte[] dataObject = CDKObjectHandler.getBytes(resultList[i]);
				T2Reference containerRef = referenceService.register(dataObject, 0, true, context);
				outputs.put(this.RESULT_PORTS[i], containerRef);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// TODO exception handling
		}
		return outputs;
	}
}
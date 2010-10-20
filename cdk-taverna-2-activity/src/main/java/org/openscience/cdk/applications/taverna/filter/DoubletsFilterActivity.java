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
 */package org.openscience.cdk.applications.taverna.filter;

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
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;

/**
 * Class which represents the doublets filter activity. It filter doublets in given structures list.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class DoubletsFilterActivity extends AbstractCDKActivity {

	public static final String DOUBLETS_FILTER_ACTIVITY = "Doublets Filter";

	/**
	 * Creates a new instance.
	 */
	public DoubletsFilterActivity() {
		this.INPUT_PORTS = new String[] { "Structures", };
		this.RESULT_PORTS = new String[] { "Filtered Structures" };
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
	public String getActivityName() {
		return DoubletsFilterActivity.DOUBLETS_FILTER_ACTIVITY;
	}

	@Override
	public String getDescription() {
		return "Descriptions: " + DoubletsFilterActivity.DOUBLETS_FILTER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		return new HashMap<String, Object>();
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.FILTER_FOLDER_NAME;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		List<CMLChemFile> filteredList = new ArrayList<CMLChemFile>();
		List<CMLChemFile> chemFileList = new ArrayList<CMLChemFile>();
		List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), byte[].class,
				context);
		try {
			chemFileList = CDKObjectHandler.getChemFileList(dataArray);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error while deserializing object.", this.getConfiguration().getActivityName(),
					e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		IAtomContainer[] containers;
		try {
			containers = CMLChemFileWrapper.convertCMLChemFileListToAtomContainerArray(chemFileList);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error while converting CML chem file list.",
					this.getConfiguration().getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		ArrayList<IAtomContainer> tempList = new ArrayList<IAtomContainer>();
		for (int i = 0; i < containers.length; i++) {
			IAtomContainer queryContainer = containers[i];
			boolean doublet = false;
			try {
				for (int j = 0; j < tempList.size(); j++) {
					IAtomContainer targetContainer = tempList.get(j);
					if (UniversalIsomorphismTester.isIsomorph(targetContainer, queryContainer)) {
						doublet = true;
						break;
					}
				}
			} catch (Exception e) {
				comment.add("Error!");
				ErrorLogger.getInstance().writeError("Error while searching for isomorphs.",
						this.getConfiguration().getActivityName(), e);
			}
			if (!doublet) {
				tempList.add(queryContainer);
			}
		}

		for (IAtomContainer c : tempList) {
			filteredList.add(CMLChemFileWrapper.wrapAtomContainerInChemModel(c));
		}
		comment.add("Calculation done;");
		// Congfigure output
		try {
			T2Reference containerRef = referenceService.register(CDKObjectHandler.getBytesList(filteredList), 1, true, context);
			outputs.put(this.RESULT_PORTS[0], containerRef);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error while configuring output ports.",
					this.getConfiguration().getActivityName(), e);
			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
		}
		return outputs;
	}
}

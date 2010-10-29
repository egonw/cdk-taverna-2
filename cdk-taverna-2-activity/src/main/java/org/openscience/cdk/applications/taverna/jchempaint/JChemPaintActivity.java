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
package org.openscience.cdk.applications.taverna.jchempaint;

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

/**
 * Class which represents the JChemPaint activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class JChemPaintActivity extends AbstractCDKActivity {

	public static final String JCHEMPAINT_ACTIVITY = "JChemPaint";

	/**
	 * Creates a new instance.
	 */
	public JChemPaintActivity() {
		this.RESULT_PORTS = new String[] { "Structures" };
	}

	@Override
	protected void addInputPorts() {
		// Nothing to add
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.RESULT_PORTS[0], 1);
	}

	@Override
	public String getActivityName() {
		return JChemPaintActivity.JCHEMPAINT_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		return new HashMap<String, Object>();
	}

	@Override
	public String getDescription() {
		return "Description: " + JChemPaintActivity.JCHEMPAINT_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.JCHEMPAINT_FOLDER_NAME;
	}

	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		List<CMLChemFile> cmlChemFileList = null;
		List<byte[]> dataList = new ArrayList<byte[]>();
		// Read ChemFile
		try {
			CMLChemFile chemFile = (CMLChemFile) this.getConfiguration().getAdditionalProperty(
					CDKTavernaConstants.PROPERTY_CMLCHEMFILE);
			if (chemFile == null) {
				throw new CDKTavernaException(JChemPaintActivity.JCHEMPAINT_ACTIVITY, "No molecules found!");
			}
			cmlChemFileList = CMLChemFileWrapper.wrapInChemModelList(chemFile);
			if (cmlChemFileList.isEmpty()) {
				throw new CDKTavernaException(JChemPaintActivity.JCHEMPAINT_ACTIVITY, "No molecules found!");
			}
			// Congfigure output
			for (CMLChemFile c : cmlChemFileList) {
				dataList.add(CDKObjectHandler.getBytes(c));
			}
			T2Reference containerRef = referenceService.register(dataList, 1, true, context);
			outputs.put(this.RESULT_PORTS[0], containerRef);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Return results
		return outputs;

	}

}

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

/**
 * Class which represents the structure to CML String converter activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.interfaces.IFileReader;

public class UUIDGeneratorActivity extends AbstractCDKActivity implements IFileReader {

	public static final String UUID_GENERATOR_ACTIVITY = "UUID Generator";
	private UUID id = null;

	public UUIDGeneratorActivity() {
		this.RESULT_PORTS = new String[] { "UUID" };
		this.id = UUID.randomUUID();
	}

	@Override
	protected void addInputPorts() {
		// Empty
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.RESULT_PORTS[0], 0);
	}

	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		T2Reference containerRef = referenceService.register(this.id.toString(), 0, true, context);
		outputs.put(this.RESULT_PORTS[0], containerRef);
		comment.add("done");
		// Return results
		return outputs;
	}

	@Override
	public String getActivityName() {
		return UUIDGeneratorActivity.UUID_GENERATOR_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + UUIDGeneratorActivity.UUID_GENERATOR_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.MISCELLANEOUS_FOLDER_NAME;
	}

}

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
package org.openscience.cdk.applications.taverna.io;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.openscience.cdk.Reaction;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.Constants;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.interfaces.IFileReader;
import org.openscience.cdk.applications.taverna.io.MDLMolFileReaderActivity;
import org.openscience.cdk.io.MDLRXNReader;

/**
 * Class which represents the MDL RXN file reader activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class MDLRXNFileReaderActivity extends AbstractCDKActivity implements IFileReader {

	public static final String RXN_FILE_READER_ACTIVITY = "RXN file reader";

	public MDLRXNFileReaderActivity() {
		this.RESULT_PORTS = new String[] { "Reactions" };
	}

	@Override
	protected void addInputPorts() {
		// empty
	}

	@Override
	protected void addOutputPorts() {
		this.addOutput(this.RESULT_PORTS[0], 0);
	}

	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback) {
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		Reaction reaction = new Reaction();
		try {
			// Read RXN file
			File file = (File) this.getConfiguration().getAdditionalProperty(Constants.PROPERTY_FILE);
			MDLRXNReader reader = new MDLRXNReader(new FileReader(file));
			reaction = (Reaction) reader.read(reaction);
			// Congfigure output
			T2Reference containerRef = referenceService.register(CDKObjectHandler.getBytes(reaction), 0, true, context);
			outputs.put(this.RESULT_PORTS[0], containerRef);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Return results
		return outputs;
	}

	@Override
	public String getActivityName() {
		return MDLRXNFileReaderActivity.RXN_FILE_READER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(Constants.PROPERTY_FILE_EXTENSION, ".rxn");
		properties.put(Constants.PROPERTY_FILE_EXTENSION_DESCRIPTION, "MDL RXN file");
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + MDLRXNFileReaderActivity.RXN_FILE_READER_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return Constants.IO_FOLDER_NAME;
	}
}

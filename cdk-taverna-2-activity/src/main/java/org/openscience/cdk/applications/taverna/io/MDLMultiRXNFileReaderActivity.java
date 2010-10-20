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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.openscience.cdk.Reaction;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.interfaces.IFileReader;
import org.openscience.cdk.io.MDLRXNReader;

/**
 * Class which represents the MDL Multi RXN file reader activity. (Not official supported file format. Only MDL RXN Strings separated
 * by "$$$$".)
 * 
 * @author Andreas Truzskowski
 * 
 */
public class MDLMultiRXNFileReaderActivity extends AbstractCDKActivity implements IFileReader {

	public static final String MULTI_RXN_FILE_READER_ACTIVITY = "Mutli RXN File Reader";

	/**
	 * Creates a new instance.
	 */
	public MDLMultiRXNFileReaderActivity() {
		this.RESULT_PORTS = new String[] { "Reactions" };
	}

	@Override
	protected void addInputPorts() {
		// empty
	}

	@Override
	protected void addOutputPorts() {
		this.addOutput(this.RESULT_PORTS[0], 1);
	}

	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		LinkedList<Reaction> reactionList = new LinkedList<Reaction>();
		// Read RXN file
		File file = (File) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE);
		if (file == null) {
			throw new CDKTavernaException(this.getActivityName(), "Error, no file chosen!");
		}
		try {
			LineNumberReader lineReader = new LineNumberReader(new FileReader(file));
			String line = "";
			String reactionString = "";
			while ((line = lineReader.readLine()) != null) {
				if (line.contains("$$$$")) {
					try {
						MDLRXNReader reader = new MDLRXNReader(new ByteArrayInputStream(reactionString.getBytes()));
						Reaction reaction = (Reaction) reader.read(new Reaction());
						reactionList.add(reaction);
						reader.close();
					} catch (Exception e) {
						ErrorLogger.getInstance().writeError("Error reading RXN part: \n" + reactionString,
								this.getActivityName(), e);
						comment.add("Error reading RXN part: \n" + reactionString);
					} finally {
						reactionString = "";
					}
				} else {
					reactionString += line + "\n";
				}
			}
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error reading multi RXN file: " + file.getPath(), this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), "Error reading multi RXN file: " + file.getPath());
		}
		// Congfigure output
		try {
			T2Reference containerRef = referenceService.register(CDKObjectHandler.getBytesList(reactionList), 1, true, context);
			outputs.put(this.RESULT_PORTS[0], containerRef);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error while configurating output port!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), "Error while configurating output port!");
		}
		comment.add("done");
		// Return results
		return outputs;
	}

	@Override
	public String getActivityName() {
		return MDLMultiRXNFileReaderActivity.MULTI_RXN_FILE_READER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION, ".rxn");
		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION_DESCRIPTION, "MDL RXN file");
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + MDLMultiRXNFileReaderActivity.MULTI_RXN_FILE_READER_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.IO_FOLDER_NAME;
	}
}

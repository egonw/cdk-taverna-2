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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;
import net.sf.taverna.t2.reference.impl.external.object.InlineStringReference;

import org.openscience.cdk.Reaction;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.io.MDLRXNReader;

/**
 * Class which represents the MDL Multi RXN file reader activity. (Not official
 * supported file format. Only MDL RXN Strings separated by "$$$$".)
 * 
 * @author Andreas Truzskowski
 * 
 */
public class MDLMultiRXNFileReaderActivity extends AbstractCDKActivity {

	public static final String MULTI_RXN_FILE_READER_ACTIVITY = "Mutli RXN File Reader";

	/**
	 * Creates a new instance.
	 */
	public MDLMultiRXNFileReaderActivity() {
		this.INPUT_PORTS = new String[] { "File" };
		this.OUTPUT_PORTS = new String[] { "Reactions" };
	}

	@Override
	protected void addInputPorts() {
		List<Class<? extends ExternalReferenceSPI>> expectedReferences = new ArrayList<Class<? extends ExternalReferenceSPI>>();
		expectedReferences.add(FileReference.class);
		expectedReferences.add(InlineStringReference.class);
		addInput(this.INPUT_PORTS[0], 0, false, expectedReferences, null);
	}

	@Override
	protected void addOutputPorts() {
		this.addOutput(this.OUTPUT_PORTS[0], 1);
	}

	@Override
	public void work() throws Exception {
		// Get input
		File file = this.getInputAsFile(this.INPUT_PORTS[0]);
		// Do work
		LinkedList<IReaction> reactionList = new LinkedList<IReaction>();
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
					} finally {
						reactionString = "";
					}
				} else {
					reactionString += line + "\n";
				}
			}
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.READ_FILE_ERROR + file, this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.READ_FILE_ERROR + file);
		}
		// Set output
		this.setOutputAsObjectList(reactionList, this.OUTPUT_PORTS[0]);
	}

	@Override
	public String getActivityName() {
		return MDLMultiRXNFileReaderActivity.MULTI_RXN_FILE_READER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
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

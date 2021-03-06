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
package org.openscience.cdk.applications.taverna.iterativeio;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;
import net.sf.taverna.t2.reference.impl.external.object.InlineStringReference;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.io.MDLV2000Reader;

/**
 * Class which represents the iterative loop SDFile reader.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class IterativeSDFileReaderActivity extends AbstractCDKActivity {

	public static final String ITERATIVE_SD_FILE_READER_ACTIVITY = "Iterative SDfile Reader";

	public IterativeSDFileReaderActivity() {
		this.INPUT_PORTS = new String[] { "File", "# Of Structures Per Iteration" };
		this.OUTPUT_PORTS = new String[] { "Structures" };
	}

	@Override
	protected void addInputPorts() {
		List<Class<? extends ExternalReferenceSPI>> expectedReferences = new ArrayList<Class<? extends ExternalReferenceSPI>>();
		expectedReferences.add(FileReference.class);
		expectedReferences.add(InlineStringReference.class);
		addInput(this.INPUT_PORTS[0], 0, false, expectedReferences, null);
		addInput(this.INPUT_PORTS[1], 0, true, null, Integer.class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1, 0);
	}

	@Override
	public String getActivityName() {
		return IterativeSDFileReaderActivity.ITERATIVE_SD_FILE_READER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + IterativeSDFileReaderActivity.ITERATIVE_SD_FILE_READER_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.ITERATIVE_IO_FOLDER_NAME;
	}

	@Override
	public void work() throws Exception {
		// Get input
		File file = this.getInputAsFile(this.INPUT_PORTS[0]);
		int readSize = this.getInputAsObject(this.INPUT_PORTS[1], Integer.class);
		// Do work
		List<T2Reference> outputList = new ArrayList<T2Reference>();
		int index = 0;
		try {
			LineNumberReader lineReader = new LineNumberReader(new FileReader(file));
			String line;
			String SDFilePart = "";
			List<CMLChemFile> resultList = new ArrayList<CMLChemFile>();
			do {
				line = lineReader.readLine();
				if (line != null) {
					SDFilePart += line + "\n";
					if (line.contains("$$$$")) {
						try {
							CMLChemFile cmlChemFile = new CMLChemFile();
							MDLV2000Reader tmpMDLReader = new MDLV2000Reader(new ByteArrayInputStream(
									SDFilePart.getBytes()));
							tmpMDLReader.read(cmlChemFile);
							tmpMDLReader.close();
							resultList.add(cmlChemFile);
						} catch (Exception e) {
							ErrorLogger.getInstance().writeError("Error reading molecule in SD file:",
									this.getActivityName(), e);
							ErrorLogger.getInstance().writeMessage(SDFilePart);
						} finally {
							SDFilePart = "";
						}
					}
				}
				if (line == null || resultList.size() >= readSize) {
					T2Reference containerRef = this.setIterativeOutputAsList(resultList, this.OUTPUT_PORTS[0], index);
					outputList.add(index, containerRef);
					index++;
					resultList.clear();
				}
			} while (line != null);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.READ_FILE_ERROR + file.getPath(),
					this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.READ_FILE_ERROR + file.getPath());
		}
		// Set output
		this.setIterativeReferenceList(outputList, this.OUTPUT_PORTS[0]);

	}

}

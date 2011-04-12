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

/**
 * Class which represents the MDL SDFile reader activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;
import net.sf.taverna.t2.reference.impl.external.object.InlineStringReference;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.io.MDLV2000Reader;

public class MDLSDFileReaderActivity extends AbstractCDKActivity {

	public static final String SD_FILE_READER_ACTIVITY = "SDFile Reader";

	/**
	 * Creates a new instance.
	 */
	public MDLSDFileReaderActivity() {
		this.INPUT_PORTS = new String[] { "Files" };
		this.OUTPUT_PORTS = new String[] { "Structures" };
	}

	@Override
	protected void addInputPorts() {
		List<Class<? extends ExternalReferenceSPI>> expectedReferences = new ArrayList<Class<? extends ExternalReferenceSPI>>();
		expectedReferences.add(FileReference.class);
		expectedReferences.add(InlineStringReference.class);
		addInput(this.INPUT_PORTS[0], 1, false, expectedReferences, null);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
	}

	@Override
	public void work() throws Exception {
		// Get input
		List<File> files = this.getInputAsFileList(this.INPUT_PORTS[0]);
		// Do work
		List<CMLChemFile> chemFileList = new ArrayList<CMLChemFile>();
		for (File file : files) {
			try {
				LineNumberReader lineReader = new LineNumberReader(new FileReader(file));
				String line;
				String SDFilePart = "";
				while ((line = lineReader.readLine()) != null) {
					SDFilePart += line + "\n";
					if (line.contains("$$$$")) {
						try {
							CMLChemFile cmlChemFile = new CMLChemFile();
							MDLV2000Reader tmpMDLReader = new MDLV2000Reader(new ByteArrayInputStream(
									SDFilePart.getBytes()));
							tmpMDLReader.read(cmlChemFile);
							tmpMDLReader.close();
							chemFileList.add(cmlChemFile);
						} catch (Exception e) {
							ErrorLogger.getInstance().writeError("Error in SD file: " + file + "!\n" + SDFilePart,
									this.getActivityName(), e);
						} finally {
							SDFilePart = "";
						}
					}
				}
				lineReader.close();
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError(CDKTavernaException.READ_FILE_ERROR + file + "!",
						this.getActivityName(), e);
			}
		}
		// Set output
		this.setOutputAsObjectList(chemFileList, this.OUTPUT_PORTS[0]);
	}

	@Override
	public String getActivityName() {
		return MDLSDFileReaderActivity.SD_FILE_READER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + MDLSDFileReaderActivity.SD_FILE_READER_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.IO_FOLDER_NAME;
	}

}

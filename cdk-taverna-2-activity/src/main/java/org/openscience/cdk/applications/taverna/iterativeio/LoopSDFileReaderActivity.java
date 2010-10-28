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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
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
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.interfaces.IIterativeFileReader;
import org.openscience.cdk.io.MDLV2000Reader;

/**
 * Class which represents the iterative loop sd file reader.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class LoopSDFileReaderActivity extends AbstractCDKActivity implements IIterativeFileReader {

	public static final String LOOP_SD_FILE_READER_ACTIVITY = "Loop SDfile Reader";
	public static final String RUNNING = "RUNNING";
	public static final String FINISHED = "FINISHED";

	private LineNumberReader lineReader = null;

	/**
	 * Creates a new instance.
	 */
	public LoopSDFileReaderActivity() {
		this.RESULT_PORTS = new String[] { "Structures", "State" };
	}

	@Override
	protected void addInputPorts() {
		// Nothing to add
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.RESULT_PORTS[0], 1);
		addOutput(this.RESULT_PORTS[1], 0);
	}

	@Override
	public String getActivityName() {
		return LoopSDFileReaderActivity.LOOP_SD_FILE_READER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION, ".sdf");
		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION_DESCRIPTION, "MDL SDFile");
		properties.put(CDKTavernaConstants.PROPERTY_ITERATIVE_READ_SIZE, 50);
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + LoopSDFileReaderActivity.LOOP_SD_FILE_READER_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.ITERATIVE_IO_FOLDER_NAME;
	}

	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException, FileNotFoundException {
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		int readSize = (Integer) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_ITERATIVE_READ_SIZE);
		String state = RUNNING;
		// Read SDfile
		File file = ((File[]) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE))[0];
		if (file == null) {
			throw new CDKTavernaException(this.getActivityName(), "Error, no file chosen!");
		}
		// clear comments
		comment.clear();
		ArrayList<byte[]> dataList = new ArrayList<byte[]>();
		try {
			if (this.lineReader == null) {
				this.lineReader = new LineNumberReader(new FileReader(file));
			}
			String line;
			String SDFilePart = "";
			int counter = 0;
			do {
				line = lineReader.readLine();
				if (line != null) {
					SDFilePart += line + "\n";
					if (line.contains("$$$$")) {
						try {
							CMLChemFile cmlChemFile = new CMLChemFile();
							MDLV2000Reader tmpMDLReader = new MDLV2000Reader(new StringReader(SDFilePart));
							tmpMDLReader.read(cmlChemFile);
							tmpMDLReader.close();
							dataList.add(CDKObjectHandler.getBytes(cmlChemFile));
							counter++;
						} catch (Exception e) {
							ErrorLogger.getInstance().writeError("Error while reading molecule: \n" + SDFilePart,
									this.getActivityName(), e);
							comment.add("Error while reading molecule: \n" + SDFilePart);
						} finally {
							SDFilePart = "";
						}
					}
				}
				if (line == null || counter >= readSize) {
					if (line == null) {
						state = FINISHED;
						comment.add("All done!");
						this.lineReader.close();
						this.lineReader = null;
					} else {
						comment.add("Has next iteration!");
					}
				}
			} while (line != null && counter < readSize);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error while reading SDF files!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), "Error while reading SDF files!");
		}
		T2Reference reference = referenceService.register(dataList, 1, true, context);
		outputs.put(this.RESULT_PORTS[0], reference);
		T2Reference containerRef = referenceService.register(state, 0, true, context);
		outputs.put(this.RESULT_PORTS[1], containerRef);
		// Return results
		return outputs;
	}
}

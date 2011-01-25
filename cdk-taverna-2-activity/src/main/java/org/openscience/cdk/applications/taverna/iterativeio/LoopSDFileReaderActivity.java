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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.openscience.cdk.io.MDLV2000Reader;

/**
 * Class which represents the iterative loop sd file reader.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class LoopSDFileReaderActivity extends AbstractCDKActivity {

	public static final String LOOP_SD_FILE_READER_ACTIVITY = "Loop SDfile Reader";
	public static final String RUNNING = "RUNNING";
	public static final String FINISHED = "FINISHED";

	private LineNumberReader lineReader = null;

	/**
	 * Creates a new instance.
	 */
	public LoopSDFileReaderActivity() {
		this.INPUT_PORTS = new String[] { "File", "# Of Structures Per Iteration" };
		this.OUTPUT_PORTS = new String[] { "Structures", "State" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 0, true, null, String.class);
		addInput(this.INPUT_PORTS[1], 0, true, null, Integer.class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
		addOutput(this.OUTPUT_PORTS[1], 0);
	}

	@Override
	public String getActivityName() {
		return LoopSDFileReaderActivity.LOOP_SD_FILE_READER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
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
		int readSize;
		try {
			readSize = (Integer) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[1]), Integer.class, context);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.WRONG_INPUT_PORT_TYPE, this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.WRONG_INPUT_PORT_TYPE);
		}
		String state = RUNNING;
		// Read SDfile
		String file = (String) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), String.class, context);
		if (file == null) {
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.NO_FILE_CHOSEN);
		}
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
							ErrorLogger.getInstance().writeError("Error reading molecule: \n" + SDFilePart,
									this.getActivityName(), e);
						} finally {
							SDFilePart = "";
						}
					}
				}
				if (line == null || counter >= readSize) {
					if (line == null) {
						state = FINISHED;
						this.lineReader.close();
						this.lineReader = null;
					}
				}
			} while (line != null && counter < readSize);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.READ_FILE_ERROR + file, this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.READ_FILE_ERROR + file);
		}
		T2Reference reference = referenceService.register(dataList, 1, true, context);
		outputs.put(this.OUTPUT_PORTS[0], reference);
		T2Reference containerRef = referenceService.register(state, 0, true, context);
		outputs.put(this.OUTPUT_PORTS[1], containerRef);
		// Return results
		return outputs;
	}
}

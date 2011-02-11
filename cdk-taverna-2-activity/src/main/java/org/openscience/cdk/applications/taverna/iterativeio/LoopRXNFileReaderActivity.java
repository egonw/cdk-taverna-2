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

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.openscience.cdk.io.MDLRXNV2000Reader;

/**
 * Class which represents the iterative loop rxn file reader.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class LoopRXNFileReaderActivity extends AbstractCDKActivity {

	public static final String LOOP_RXN_FILE_READER_ACTIVITY = "Loop RXN file Reader";
	public static final String RUNNING = "RUNNING";
	public static final String FINISHED = "FINISHED";

	private List<File> fileList = null;

	/**
	 * Creates a new instance.
	 */
	public LoopRXNFileReaderActivity() {
		this.INPUT_PORTS = new String[] { "Files", "# Of Structures Per Iteration" };
		this.OUTPUT_PORTS = new String[] { "Reactions", "State" };
	}

	@Override
	protected void addInputPorts() {
		List<Class<? extends ExternalReferenceSPI>> expectedReferences = new ArrayList<Class<? extends ExternalReferenceSPI>>();
		expectedReferences.add(FileReference.class);
		expectedReferences.add(InlineStringReference.class);
		addInput(this.INPUT_PORTS[0], 1, false, expectedReferences, null);
		addInput(this.INPUT_PORTS[1], 0, true, null, Integer.class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
		addOutput(this.OUTPUT_PORTS[1], 0);
	}

	@Override
	public String getActivityName() {
		return LoopRXNFileReaderActivity.LOOP_RXN_FILE_READER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + LoopRXNFileReaderActivity.LOOP_RXN_FILE_READER_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.ITERATIVE_IO_FOLDER_NAME;
	}

	@Override
	public void work() throws Exception {
		// Get input
		String state = RUNNING;
		int readSize = this.getInputAsObject(this.INPUT_PORTS[1], Integer.class);
		if (this.fileList == null) {
			this.fileList = this.getInputAsFileList(this.INPUT_PORTS[0]);
		}
		// Do work
		List<IReaction> reactionList = new ArrayList<IReaction>();
		for (int i = 0; i < readSize; i++) {
			File file = fileList.remove(0);
			try {
				MDLRXNV2000Reader reader = new MDLRXNV2000Reader(new FileReader(file));
				Reaction reaction = (Reaction) reader.read(new Reaction());
				reader.close();
				reactionList.add(reaction);
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError(CDKTavernaException.READ_FILE_ERROR + file.getPath() + "!",
						this.getActivityName(), e);
				throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.READ_FILE_ERROR
						+ file.getPath() + "!");
			}
			if (fileList.isEmpty()) {
				state = FINISHED;
				break;
			}
		}
		// Set output
		this.setOutputAsObjectList(reactionList, this.OUTPUT_PORTS[0]);
		this.setOutputAsString(state, this.OUTPUT_PORTS[1]);
	}
}

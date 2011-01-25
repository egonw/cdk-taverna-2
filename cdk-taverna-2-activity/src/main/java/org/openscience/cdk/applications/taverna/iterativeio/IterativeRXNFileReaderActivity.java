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

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
import org.openscience.cdk.io.MDLRXNV2000Reader;

/**
 * Class which represents the iterative rxn file reader.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class IterativeRXNFileReaderActivity extends AbstractCDKActivity {

	public static final String ITERATIVE_RXN_FILE_READER_ACTIVITY = "Iterative RXN File Reader";

	/**
	 * Creates a new instance.
	 */
	public IterativeRXNFileReaderActivity() {
		this.INPUT_PORTS = new String[] { "Files", "# Of Reactions Per Iteration" };
		this.OUTPUT_PORTS = new String[] { "Reaction(s)" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, String.class);
		addInput(this.INPUT_PORTS[1], 0, true, null, Integer.class);
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1, 0);
	}

	@Override
	public String getActivityName() {
		return IterativeRXNFileReaderActivity.ITERATIVE_RXN_FILE_READER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + IterativeRXNFileReaderActivity.ITERATIVE_RXN_FILE_READER_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.ITERATIVE_IO_FOLDER_NAME;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		// Read RXNfile
		int readSize;
		try {
			readSize = (Integer) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[1]), Integer.class, context);
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.WRONG_INPUT_PORT_TYPE, this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.WRONG_INPUT_PORT_TYPE);
		}
		List<String> files = (List<String>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), String.class,
				context);
		if (files == null || files.size() == 0) {
			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.NO_FILE_CHOSEN);
		}
		List<T2Reference> outputList = new ArrayList<T2Reference>();
		try {
			List<Reaction> reactions = new LinkedList<Reaction>();
			int counter = 0;
			for (int i = 0; i < files.size(); i++) {
				try {
					MDLRXNV2000Reader reader = new MDLRXNV2000Reader(new FileReader(files.get(i)));
					reactions.add((Reaction) reader.read(new Reaction()));
					reader.close();
				} catch (Exception e) {
					ErrorLogger.getInstance().writeError(CDKTavernaException.READ_FILE_ERROR + files.get(i) + "!",
							this.getActivityName(), e);
				}
				counter++;
				if (i == files.size() - 1 || counter >= readSize) {
					List<byte[]> dataList = new ArrayList<byte[]>();
					dataList = CDKObjectHandler.getBytesList(reactions);
					T2Reference containerRef = referenceService.register(dataList, 1, true, context);
					outputList.add(i, containerRef);
					outputs.put(this.OUTPUT_PORTS[0], containerRef);
					callback.receiveResult(outputs, new int[] { i });
					reactions.clear();
					counter = 0;
				}
			}
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error reading RXN files!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), "Error reading RXN files!");
		}
		T2Reference containerRef = referenceService.register(outputList, 1, true, context);
		outputs.put(this.OUTPUT_PORTS[0], containerRef);
		// Return results
		return outputs;
	}

}

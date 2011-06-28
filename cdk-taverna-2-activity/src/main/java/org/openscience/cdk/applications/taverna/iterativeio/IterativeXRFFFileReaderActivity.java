/*
 * Copyright (C) 2011 by Andreas Truszkowski <ATruszkowski@gmx.de>
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;
import net.sf.taverna.t2.reference.impl.external.object.InlineStringReference;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;

import weka.core.Instances;
import weka.core.converters.XRFFLoader;

/**
 * Class which represents the iterative loop XRFF file reader.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class IterativeXRFFFileReaderActivity extends AbstractCDKActivity {

	public static final String ITERATIVE_XRFF_FILE_READER_ACTIVITY = "Iterative XRFF File Reader";

	public IterativeXRFFFileReaderActivity() {
		this.INPUT_PORTS = new String[] { "File", "# Of Files Per Iteration" };
		this.OUTPUT_PORTS = new String[] { "Weka Datasets" };
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
		addOutput(this.OUTPUT_PORTS[0], 1, 0);
	}

	@Override
	public String getActivityName() {
		return IterativeXRFFFileReaderActivity.ITERATIVE_XRFF_FILE_READER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + IterativeXRFFFileReaderActivity.ITERATIVE_XRFF_FILE_READER_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.ITERATIVE_IO_FOLDER_NAME;
	}

	@Override
	public void work() throws Exception {
		// Get input
		List<File> files = this.getInputAsFileList(this.INPUT_PORTS[0]);
		int readSize = this.getInputAsObject(this.INPUT_PORTS[1], Integer.class);
		// Do work
		List<T2Reference> outputList = new ArrayList<T2Reference>();
		int index = 0;
		List<Instances> datasets = new LinkedList<Instances>();
		XRFFLoader loader = new XRFFLoader();
		while (!files.isEmpty()) {
			File file = files.remove(0);
			try {
				loader.setSource(file);
				Instances instances = loader.getDataSet();
				datasets.add(instances);
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError(CDKTavernaException.READ_FILE_ERROR + file,
						this.getActivityName(), e);
			}
			if (files.isEmpty() || datasets.size() >= readSize) {
				T2Reference containerRef = this.setIterativeOutputAsList(datasets, this.OUTPUT_PORTS[0], index);
				outputList.add(index, containerRef);
				index++;
				datasets.clear();
			}
		}
		// Set output
		this.setIterativeReferenceList(outputList, this.OUTPUT_PORTS[0]);

	}

}

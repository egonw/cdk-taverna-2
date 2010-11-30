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
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.interfaces.IIterativeFileWriter;

/**
 * Class which represents the CSV file writer activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class CSVFileWriterActivity extends AbstractCDKActivity implements IIterativeFileWriter {

	public static final String CSV_FILE_WRITER_ACTIVITY = "CSV File Writer";
	private File file = null;

	/**
	 * Creates a new instance.
	 */
	public CSVFileWriterActivity() {
		this.INPUT_PORTS = new String[] { "Strings" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, String.class);
	}

	@Override
	protected void addOutputPorts() {
		// Nothing to add
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, T2Reference> work(final Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		List<String> strings = (List<String>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), String.class,
				context);
		File directory = (File) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE);
		if (directory == null) {
			throw new CDKTavernaException(this.getActivityName(), "Error, no output directory chosen!");
		}
		String extension = (String) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE_EXTENSION);
		Boolean oneFilePerIteration = (Boolean) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_ONE_FILE_PER_ITERATION);
		if (oneFilePerIteration) {
			this.file = FileNameGenerator.getNewFile(directory.getPath(), extension, this.iteration);
		} else {
			if (this.file == null) {
				this.file = FileNameGenerator.getNewFile(directory.getPath(), extension);
			}
		}
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(file, !oneFilePerIteration));
			for (int i = 0; i < strings.size(); i++) {
				String s = strings.get(i);
				if (this.iteration > 1 && s.startsWith("\"ID\"")&& s.startsWith("Descriptor Name") && !oneFilePerIteration && i == 0) {
					continue;
				}
				writer.write(s + "\n");
			}
			writer.close();
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Error writing csv file: " + file.getPath() + "!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), "Error writing csv file: " + file.getPath() + "!");
		}
		return null;
	}

	@Override
	public String getActivityName() {
		return CSVFileWriterActivity.CSV_FILE_WRITER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_ONE_FILE_PER_ITERATION, true);
		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION, ".csv");
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + CSVFileWriterActivity.CSV_FILE_WRITER_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.IO_FOLDER_NAME;
	}
}

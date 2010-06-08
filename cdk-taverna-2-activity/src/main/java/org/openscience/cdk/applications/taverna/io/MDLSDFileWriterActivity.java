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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.Constants;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.interfaces.IFileWriter;
import org.openscience.cdk.io.SDFWriter;

/**
 * Class which represents the MDL SDFile writer activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class MDLSDFileWriterActivity extends AbstractCDKActivity implements IFileWriter {

	public static final String SD_FILE_WRITER_ACTIVITY = "SDfile writer";

	public MDLSDFileWriterActivity() {
		this.INPUT_PORTS = new String[] { "Structures" };
	}

	@Override
	protected void addInputPorts() {
		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
	}

	@Override
	protected void addOutputPorts() {
		// empty
	}

	@Override
	public Map<String, T2Reference> work(Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
			throws CDKTavernaException {
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();
		List<CMLChemFile> chemFileList = new ArrayList<CMLChemFile>();
		try {
			List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]),
					byte[].class, context);
			for (byte[] data : dataArray) {
				Object obj = CDKObjectHandler.getObject(data);
				if (obj instanceof CMLChemFile) {
					chemFileList.add((CMLChemFile) obj);
				} else {
					throw new CDKTavernaException(this.getConfiguration().getActivityName(),
							CDKTavernaException.WRONG_INPUT_PORT_TYPE);
				}
			}
		} catch (Exception e) {
			if (e instanceof CDKTavernaException) {
				throw (CDKTavernaException) e;
			} else {
				throw new CDKTavernaException(this.getConfiguration().getActivityName(),
						CDKTavernaException.WRONG_INPUT_PORT_TYPE);
			}
		}
		File directory = (File) this.getConfiguration().getAdditionalProperty(Constants.PROPERTY_FILE);
		if (directory == null) {
			throw new CDKTavernaException(this.getActivityName(), "Error, no output directory chosen!");
		}
		String extension = (String) this.getConfiguration().getAdditionalProperty(Constants.PROPERTY_FILE_EXTENSION);
		String filename = FileNameGenerator.getNewFile(directory.getPath(), extension);
		try {
			SDFWriter writer = new SDFWriter(new FileWriter(new File(filename)));
			for (CMLChemFile cmlChemFile : chemFileList) {
				writer.write(cmlChemFile);
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			comment.add("Error writing file: " + filename + "!");
		}
		comment.add("done");
		return null;
	}

	@Override
	public String getActivityName() {
		return MDLSDFileWriterActivity.SD_FILE_WRITER_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(Constants.PROPERTY_FILE_EXTENSION, ".sdf");
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + MDLSDFileWriterActivity.SD_FILE_WRITER_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return Constants.IO_FOLDER_NAME;
	}
}

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
package org.openscience.cdk.applications.taverna.qsar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;
import net.sf.taverna.t2.reference.impl.external.object.InlineStringReference;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;

/**
 * Class which represents the merge CSV To QSAR vector activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class MergeCSVFilesActivity extends AbstractCDKActivity {

	public static final String MERGE_CSV_TO_QSAR_VECTOR_ACTIVITY = "Merge CSV Files";

	/**
	 * Creates a new instance.
	 */
	public MergeCSVFilesActivity() {
		this.INPUT_PORTS = new String[] { "Files" };
		this.OUTPUT_PORTS = new String[] { "Files" };
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
		HashSet<String> descriptorNamesSet = new HashSet<String>();
		ArrayList<String> descriptorNamesList = new ArrayList<String>();
		ArrayList<String> resultFiles = new ArrayList<String>();
		// Read available descriptor names
		for (File file : files) {
			try {
				LineNumberReader reader = null;
				try {
					reader = new LineNumberReader(new FileReader(file));
				} catch (FileNotFoundException e) {
					ErrorLogger.getInstance().writeError(file.getPath() + "does not exist!", this.getActivityName(), e);
					throw new CDKTavernaException(this.getActivityName(), file.getPath() + "does not exist!");
				}
				String line = reader.readLine();
				if (line == null) {
					continue;
				}
				String[] items = this.getItems(line);
				// Skip first item because it contains only the id
				for (int i = 1; i < items.length; i++) {
					if (!descriptorNamesSet.contains(items[i])) {
						descriptorNamesSet.add(items[i]);
						descriptorNamesList.add(items[i]);
					}
				}
				reader.close();
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError(CDKTavernaException.READ_FILE_ERROR + file.getPath() + "!",
						this.getActivityName(), e);
				throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.READ_FILE_ERROR
						+ file.getPath() + "!");
			}
		}
		File outputFile = FileNameGenerator.getNewFile(files.get(0).getParent(), ".csv", "MergedFile");
		PrintWriter writer = null;
		try {
			// write csv file
			writer = new PrintWriter(outputFile);
		} catch (FileNotFoundException e) {
			ErrorLogger.getInstance().writeError(outputFile.getPath() + "does not exist!", this.getActivityName(), e);
			throw new CDKTavernaException(this.getActivityName(), outputFile.getPath() + "does not exist!");
		}
		// write header
		writer.write("\"ID\";");
		for (String name : descriptorNamesList) {
			writer.write("\"" + name + "\";");
		}
		writer.write("\n");
		// write data
		for (File file : files) {
			try {
				LineNumberReader reader = null;
				try {
					reader = new LineNumberReader(new FileReader(file));
				} catch (FileNotFoundException e) {
					ErrorLogger.getInstance().writeError(file.getPath() + "does not exist!", this.getActivityName(), e);
					throw new CDKTavernaException(this.getActivityName(), file.getPath() + "does not exist!");
				}
				// write id
				String line = "";
				int[] idxMap = null;
				boolean readDescriptorNames = true;
				while ((line = reader.readLine()) != null) {
					String[] items = this.getItems(line);
					String[] values = new String[descriptorNamesSet.size()];
					// First line contains the descriptor names
					if (readDescriptorNames) {
						// Skip first item because it contains only the id
						idxMap = new int[items.length - 1];
						for (int i = 1; i < items.length; i++) {
							idxMap[i - 1] = descriptorNamesList.indexOf(items[i]);
						}
						readDescriptorNames = false;
						continue;
					} else {
						// Read values
						for (int i = 0; i < values.length; i++) {
							values[i] = "" + Double.NaN;
						}
						for (int i = 1; i < items.length; i++) {
							values[idxMap[i - 1]] = items[i];
						}
					}
					// write id
					writer.write("\"" + items[0] + "\";");
					// write values
					for (String v : values) {
						writer.write("\"" + v + "\";");
					}
					writer.write("\n");
				}
				reader.close();
				writer.flush();
			} catch (Exception e) {
				ErrorLogger.getInstance().writeError(CDKTavernaException.READ_FILE_ERROR + file.getPath() + "!",
						this.getActivityName(), e);
				throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.READ_FILE_ERROR
						+ file.getPath() + "!");
			}
		}
		writer.close();
		resultFiles.add(outputFile.getPath());
		// Set output
		this.setOutputAsStringList(resultFiles, this.OUTPUT_PORTS[0]);
	}

	/**
	 * Splits a CSV string line into its single items.
	 * 
	 * @param line
	 *            CSV string
	 * @return Items
	 */
	private String[] getItems(String line) {
		String[] items = line.split("\";\"");
		items[0] = items[0].replaceAll("\"", "");
		items[items.length - 1] = items[items.length - 1].replaceAll("\";", "");
		return items;
	}

	@Override
	public String getActivityName() {
		return MergeCSVFilesActivity.MERGE_CSV_TO_QSAR_VECTOR_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION, ".csv");
		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION_DESCRIPTION, "Any CSV File");
		properties.put(CDKTavernaConstants.PROPERTY_SUPPORT_MULTI_FILE, true);
		return properties;
	}

	@Override
	public String getDescription() {
		return "Description: " + MergeCSVFilesActivity.MERGE_CSV_TO_QSAR_VECTOR_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.QSAR_FOLDER_NAME;
	}

}

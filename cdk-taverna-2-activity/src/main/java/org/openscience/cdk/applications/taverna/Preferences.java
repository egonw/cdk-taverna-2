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
package org.openscience.cdk.applications.taverna;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;

/**
 * Singleton class holding configuration properties.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class Preferences {

	private static Preferences instance = null;

	private String currentDirectory = ".";
	private HashMap<UUID, DataOutputStream> dataCollectorIdxStreamMap = new HashMap<UUID, DataOutputStream>();
	private HashMap<UUID, DataOutputStream> dataCollectorDataStreamMap = new HashMap<UUID, DataOutputStream>();

	/**
	 * Creates a new instance.
	 */
	private Preferences() {
	}

	/**
	 * @return instance of the preferences class.
	 */
	public synchronized static Preferences getInstance() {
		if (instance == null) {
			instance = new Preferences();
		}
		return instance;
	}

	/**
	 * @return last browsed file chooser directory.
	 */
	public String getCurrentDirectory() {
		return currentDirectory;
	}

	/**
	 * Sets the last browsed file chooser directory.
	 * 
	 * @param currentDirectory
	 */
	public void setCurrentDirectory(String currentDirectory) {
		this.currentDirectory = currentDirectory;
	}

	/**
	 * Creates a filename from given id and extension for the data collector activity.
	 * 
	 * @param id
	 *            of the stream
	 * @param extension
	 * @return
	 */
	public String createDataCollectorFilename(UUID id, String extension) {
		String filename = FileNameGenerator.getCacheDir();
		filename += id.toString();
		filename += "." + extension;
		return filename;
	}

	/**
	 * Creates a new data output stream for the data collector activity from the given id.
	 * 
	 * @param id
	 *            of the stream
	 * @return new stream
	 * @throws FileNotFoundException
	 */
	public DataOutputStream getDataCollectorDataStream(UUID id) throws FileNotFoundException {
		DataOutputStream stream = this.dataCollectorDataStreamMap.get(id);
		if (stream == null) {
			String filename = this.createDataCollectorFilename(id, "dat");
			File file = new File(filename);
			if (file.exists()) {
				file.delete();
			}
			stream = new DataOutputStream(new FileOutputStream(file, true));
			this.setDataCollectorDataStream(id, stream);
		}
		return stream;
	}

	/**
	 * Closes the data output stream with the given data.
	 * 
	 * @param id
	 *            of the stream
	 * @throws IOException
	 */
	public void closeDataCollectorDataStream(UUID id) throws IOException {
		DataOutputStream stream = dataCollectorIdxStreamMap.get(id);
		stream.close();
		this.dataCollectorDataStreamMap.remove(id);
	}

	/**
	 * Stores the data output stream.
	 * 
	 * @param id
	 *            of the stream
	 * @param stream
	 */
	private void setDataCollectorDataStream(UUID id, DataOutputStream stream) {
		this.dataCollectorDataStreamMap.put(id, stream);
	}

	/**
	 * Creates a new index output stream for the data collector activity from given id.
	 * 
	 * @param id
	 * @return
	 * @throws FileNotFoundException
	 */
	public DataOutputStream createDataCollectorIdxStream(UUID id) throws FileNotFoundException {
		DataOutputStream stream = this.dataCollectorIdxStreamMap.get(id);
		if (stream == null) {
			String filename = this.createDataCollectorFilename(id, "idx");
			File file = new File(filename);
			if (file.exists()) {
				file.delete();
			}
			stream = new DataOutputStream(new FileOutputStream(file, true));
			this.setDataCollectorIdxStream(id, stream);
		}
		return stream;
	}

	/**
	 * Closes an index output stream.
	 * 
	 * @param id
	 *            of the stream
	 * @throws IOException
	 */
	public void closeDataCollectorIdxStream(UUID id) throws IOException {
		DataOutputStream stream = dataCollectorIdxStreamMap.get(id);
		stream.close();
		this.dataCollectorDataStreamMap.remove(id);
	}

	/**
	 * Stores the index output stream.
	 * 
	 * @param id
	 *            of the stream
	 * @param stream
	 */
	private void setDataCollectorIdxStream(UUID id, DataOutputStream stream) {
		this.dataCollectorIdxStreamMap.put(id, stream);
	}
}

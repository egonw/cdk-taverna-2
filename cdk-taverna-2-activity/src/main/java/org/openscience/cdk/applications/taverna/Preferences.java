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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

	private Preferences() {
	}

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

	public DataOutputStream getDataCollectorDataStream(UUID id) throws FileNotFoundException {
		DataOutputStream stream = dataCollectorDataStreamMap.get(id);
		if (stream == null) {
			String filename = FileNameGenerator.getTempDir();
			filename += id.toString();
			filename += ".dat";
			stream = new DataOutputStream(new FileOutputStream(filename));
		}
		return stream;
	}

	public void setDataCollectorDataStream(UUID id, DataOutputStream stream) {
		this.dataCollectorDataStreamMap.put(id, stream);
	}

	public DataOutputStream getDataCollectorIdxStream(UUID id) throws FileNotFoundException {
		DataOutputStream stream = dataCollectorIdxStreamMap.get(id);
		if (stream == null) {
			String filename = FileNameGenerator.getTempDir();
			filename += id.toString();
			filename += ".idx";
			stream = new DataOutputStream(new FileOutputStream(filename));
		}
		return stream;
	}

	public void setDataCollectorIdxStream(UUID id, DataOutputStream stream) {
		this.dataCollectorIdxStreamMap.put(id, stream);
	}
}

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
package org.openscience.cdk.applications.taverna.basicutilities;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class which provides methods for a centralized file name handling within the cdk-taverna project
 * 
 * @author Andreas Truszkowski
 * 
 */
public class FileNameGenerator {

	/**
	 * Generates a unique filename from the given parameters.
	 * 
	 * @param path
	 *            Path to the file
	 * @param extension
	 *            The file extension
	 * @return
	 */
	public synchronized static File getNewFile(String path, String extension) {
		String filename = "";
		File file = null;
		int idx = 1;
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
		filename += path;
		if (!path.endsWith(File.separator)) {
			filename += File.separator;
		}
		filename += dateformat.format(new Date());
		String temp;
		do {
			temp = "_" + idx;
			file = new File(filename + temp + extension);
			idx++;
		} while (file.exists());
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file;
	}

	public synchronized static String getTempDir() {
		String tmpDir = System.getProperty("java.io.tmpdir") + File.separator + "CDKTaverna";
		File file = new File(tmpDir);
		if (!file.exists()) {
			file.mkdir();
		}
		return tmpDir;
	}
}

/* 
 * $Author: egonw $
 * $Date: 2008-05-05 12:58:11 +0200 (Mo, 05 Mai 2008) $
 * $Revision: 10819 $
 * 
 * Copyright (C) 2006 - 2007 by Thomas Kuhn <thomaskuhn@users.sourceforge.net>
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.math.RandomNumbersTool;

/**
 * @author Egon Willighagen, Andreas Truszkowski
 * 
 */
public class CDKIOFileWriter {
	/**
	 * Method to write a CMLChemfile-Array to a file after it is converted to a cml-string Mainly for debugging purpose
	 * 
	 * @param cmlChemFile
	 * @param fileName
	 *            The name of the file in which the
	 * @throws Exception
	 */
	public static void writeCMLChemFileToFile(CMLChemFile[] cmlChemFile, String fileName) throws Exception {
		if (fileName == null || fileName.length() == 0) {
			fileName = "NoFileNameAvailable";
		}
		fileName += ".cml";
		String[] cml = new String[cmlChemFile.length];
		for (int i = 0; i < cmlChemFile.length; i++) {
			cml[i] = cmlChemFile[i].toCML();
		}
		writeFile(cml, fileName);
	}

	/**
	 * Method to write a List of CMLChemfiles to a file after it is converted to a cml-string Mainly for debugging purpose
	 * 
	 * @param List
	 *            of CMLChemFiles
	 * @param fileName
	 *            The name of the file in which the
	 * @throws Exception
	 */
	public static void writeListOfCMLChemFilesToFile(List<CMLChemFile> list, String fileName, String path) throws Exception {
		if (fileName == null || fileName.length() == 0) {
			fileName = "NoFileNameAvailable";
		}
		fileName += ".cml";
		CMLChemFile cmlChemFile;
		String[] cml = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			cmlChemFile = list.get(i);
			cml[i] = cmlChemFile.toCML();
		}
		writeFile(cml, fileName, path);
	}

	/**
	 * Method which write the content of a String[] to a file
	 * 
	 * @param content
	 *            String[] which will be writen to a file
	 * @param fileName
	 *            File name
	 * @param path
	 *            Path which will contain the new file
	 * @throws Exception
	 */
	public static void writeFile(String[] content, String fileName, String path) throws Exception {
		PrintWriter pw = null;
		File file = null;
		try {
			// Splits the filename because it could contain directories
			// FIXME Check for linux!!!
			String[] fileNameArray = fileName.split("\\\\");
			// Loop not over the whole array => the last array position contains the file name
			// The rest will be added to the path!
			for (int i = 0; i < fileNameArray.length - 1; i++) {
				path += File.separator + fileNameArray[i];
			}
			fileName = fileNameArray[fileNameArray.length - 1];
			checkPath(path);
			String uniquifyFileName = "_" + System.currentTimeMillis() + "_" + RandomNumbersTool.randomInt(0, 100000);
			fileName = fileName.substring(0, fileName.length() - 4) + uniquifyFileName
					+ fileName.substring(fileName.length() - 4);

			file = new File(path + fileName);
			Writer fw = new FileWriter(file);
			Writer bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			for (int i = 0; i < content.length; i++) {
				pw.print(content[i]);
			}
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.WRITE_FILE_ERROR + file.getPath(), "CDKIOFileWriter", e);
			throw new CDKTavernaException("CDKIOFileWriter", CDKTavernaException.WRITE_FILE_ERROR + file.getPath());
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

	public static void writeFile(String[] content, String filename) throws Exception {
		PrintWriter pw = null;
		try {
			String uniquifyFileName = "_" + System.currentTimeMillis() + "_" + RandomNumbersTool.randomInt(0, 100000);
			filename = filename.substring(0, filename.length() - 4) + uniquifyFileName
					+ filename.substring(filename.length() - 4);
			Writer fw = new FileWriter(filename);
			Writer bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			for (int i = 0; i < content.length; i++) {
				pw.print(content[i]);
			}
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.WRITE_FILE_ERROR + filename, "CDKIOFileWriter", e);
			throw new CDKTavernaException("CDKIOFileWriter", CDKTavernaException.WRITE_FILE_ERROR + filename);
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

	/**
	 * Method which checks if the path exists and if not if it is possible to build. If it is possible to build the path it will
	 * be done.
	 * 
	 * @param path
	 *            Path which will be check
	 * @throws Exception
	 */
	public static void checkPath(String path) throws Exception {
		File file = new File(path);
		if (!file.exists()) {
			if (!file.mkdirs()) {
				throw new CDKTavernaException("CDKIOFileWriter", CDKTavernaException.CREATE_DIRECTORY_ERROR + file.getPath());
			}
		}
	}

	/**
	 * Method which returns the absolute path. This method checks also whether the given path is correct or not. If the path don't
	 * exist will it be created.
	 * 
	 * @param path
	 *            The relative path
	 * @return The absolute path
	 * @throws Exception
	 */
	public static String getAbsolutPath(String path) throws Exception {
		checkPath(path);
		File file = new File(path);
		return file.getAbsolutePath();
	}
}

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
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Class for logging the progress of activities.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class ProgressLogger {

	private static ProgressLogger instance = null;
	private HashMap<String, File> activityFileMap = null;

	/**
	 * Creates a new instancce.
	 */
	private ProgressLogger() {
		this.activityFileMap = new HashMap<String, File>();
	}

	/**
	 * @return An instance of the progress logger class.
	 */
	public static synchronized ProgressLogger getInstance() {
		if (instance == null) {
			instance = new ProgressLogger();
		}
		return instance;
	}

	/**
	 * Writes the progress into target log file.
	 * 
	 * @param activityName
	 *            Name of logged activity.
	 * @param progress
	 *            The progress.
	 */
	public void writeProgress(String activityName, String progress) {
		Date tmpDate = Calendar.getInstance().getTime();
		File file = this.activityFileMap.get(activityName);
		PrintWriter writer;
		try {
			if (file == null || !file.exists()) {
				String name = activityName.replaceAll("/", "").replaceAll(" ", "").replaceAll("-", "");
				file = FileNameGenerator.getNewFile(FileNameGenerator.getLogDir(), ".log", name);
				this.activityFileMap.put(activityName, file);
				writer = new PrintWriter(new FileOutputStream(file, false));
				writer.append(tmpDate.toString() + ": **" + activityName.replaceAll(".", "*") + "**\n");
				writer.append(tmpDate.toString() + ": * " + activityName + " *\n");
				writer.append(tmpDate.toString() + ": **" + activityName.replaceAll(".", "*") + "**\n");
				writer.append("\n");
			} else {
				writer = new PrintWriter(new FileOutputStream(file, true));
			}
			String[] strings = progress.split("\\n");
			for (String s : strings) {
				writer.append(tmpDate.toString() + ": " + s + "\n");
			}
			writer.close();
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Could not write progress!", "ProgressLogger", e);
		}
	}

	/**
	 * Starts a new log file for target activity.
	 * 
	 * @param activityName
	 *            The name of target activity.
	 */
	public void newFile(String activityName) {
		this.activityFileMap.remove(activityName);
	}
}

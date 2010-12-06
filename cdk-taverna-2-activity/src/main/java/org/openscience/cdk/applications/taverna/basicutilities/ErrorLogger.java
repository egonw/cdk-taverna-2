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
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.SDFWriter;

/**
 * Writes the submitted error to the error log file. This class is singleton. Get an instance over the getInstance() method.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class ErrorLogger {

	// Region: Private variables

	/**
	 * Holds the instance of the errorLogger.
	 */
	private static final ErrorLogger instance = new ErrorLogger();
	/**
	 * Filename of the log file.
	 */
	private String filename = null;
	/**
	 * Directory to save log files in.
	 */
	private String reportsFolderName = null;
	/**
	 * Stream to write string data.
	 */
	private PrintStream writer = null;
	/**
	 * The error.log file.
	 */
	private File file = null;
	/**
	 * Set to true if an error occured.
	 */
	private boolean errorOccured = false;
	/**
	 * True if molecules should be logged.
	 */
	private boolean logMolecules = true;

	// End of region

	// Region: Constructor

	/**
	 * Creates a new instance.
	 */
	private ErrorLogger() {
		String path = FileNameGenerator.getLogDir();
		this.setPath(path);
	}

	// End of region

	// Region: Methods

	/**
	 * This method resets the path of the log file.
	 */
	public void setPath() {
		this.setPath(this.reportsFolderName);
	}

	/**
	 * This method resets the path of the log file.
	 * 
	 * @param path
	 *            New path.
	 */
	public void setPath(String path) {
		this.reportsFolderName = path;
		File tmpReportsFolder = new File(this.reportsFolderName);
		tmpReportsFolder = new File(this.reportsFolderName);
		if (!tmpReportsFolder.exists()) {
			tmpReportsFolder.mkdir();
		}
		SimpleDateFormat tmpSDF = new SimpleDateFormat("dd.MM.yyyy_HH.mm.ss");
		this.filename = this.reportsFolderName + File.separator + "error";
		this.filename += tmpSDF.format(new Date());
		this.filename += ".log";
		this.file = null;
	}

	/**
	 * Writes an error to the log file.
	 * 
	 * @param aError
	 *            Error description.
	 * @param aName
	 *            Name of the class.
	 * @param aException
	 *            The exception.
	 */
	public void writeError(final String aError, final String aName, final Exception aException) {
		Date tmpDate = Calendar.getInstance().getTime();
		this.errorOccured = true;
		try {
			if (this.file == null) {
				this.file = new File(this.filename);
				if (!this.file.exists()) {
					this.file.createNewFile();
				}
				this.writer = new PrintStream(this.file);
			}
		} catch (Exception e) {
			// Very bad! Only print stack trace and return.
			e.printStackTrace();
			return;
		}
		this.writer.println();
		this.writer.println(tmpDate.toString() + ":");
		this.writer.println("Error  : " + aError);
		this.writer.println("Class  : " + aName);
		this.writer.println("Message: " + aException.getMessage());
		this.writer.println();
		aException.printStackTrace(writer);
		this.writer.println();
		this.writer.flush();
		// Also print to the console.
		aException.printStackTrace();
	}

	/**
	 * Writes an error to the log file.
	 * 
	 * @param aError
	 *            Error description.
	 * @param aName
	 *            Name of the class.
	 */
	public void writeError(final String aError, final String aName) {
		Date tmpDate = Calendar.getInstance().getTime();
		this.errorOccured = true;
		try {
			if (this.file == null) {
				this.file = new File(this.filename);
				if (!this.file.exists()) {
					this.file.createNewFile();
				}
				this.writer = new PrintStream(this.file);
			}
		} catch (Exception e) {
			// Very bad! Only print stack trace and return.
			e.printStackTrace();
			return;
		}
		this.writer.println();

		this.writer.println();
		this.writer.flush();
		// Write to console
		System.out.println(tmpDate.toString() + ":");
		System.out.println("Error  : " + aError);
		System.out.println("Class  : " + aName);
	}

	/**
	 * Writes an error to the log file and writes the molecule to hard disk.
	 * 
	 * @param aError
	 *            Error description.
	 * @param aName
	 *            Name of the class.
	 * @param aException
	 *            The exception.
	 * @param aMolecule
	 *            The molecule which produces the error.
	 */
	public void writeError(final String aError, final String aName, final Exception aException, final IAtomContainer aMolecule) {
		this.writeError(aError, aName, aException);
		SimpleDateFormat tmpSDF = new SimpleDateFormat("dd.MM.yyyy_HH.mm.ss");
		String tmpDate = tmpSDF.format(new Date());
		this.writeMolecule(aMolecule, "error" + tmpDate);
	}

	/**
	 * Writes a message to the log file.
	 * 
	 * @param aMessage
	 *            The message.
	 */
	public void writeMessage(String aMessage) {
		errorOccured = true;
		Date tmpDate = Calendar.getInstance().getTime();
		try {
			if (this.file == null) {
				this.file = new File(this.filename);
				if (!this.file.exists()) {
					this.file.createNewFile();
				}
				this.writer = new PrintStream(this.file);
			}
		} catch (Exception e) {
			// Very bad! Only print stack trace and return.
			e.printStackTrace();
			return;
		}
		// Print in file
		this.writer.print(tmpDate.toString() + " ");
		this.writer.println("Message: " + aMessage);
		this.writer.flush();
	}

	/**
	 * Writes the given Molecule to hard disk. The name of the file is the current time. If the file exists an index will be added
	 * to the name so that no files will be overridden.
	 * 
	 * @param aMolecule
	 *            Molcelue to write to hard disk
	 */
	public void writeMolecule(IAtomContainer aMolecule) {
		this.writeMolecule(aMolecule, "", true);
	}

	/**
	 * Writes the given Molecule to hard disk. If the file exists an index will be added to the name so that no files will be
	 * overridden.
	 * 
	 * @param aMolecule
	 *            Molecule to write to hard disk
	 * @param aName
	 *            Name of the molecule on hard disk.
	 */
	public void writeMolecule(IAtomContainer aMolecule, String aName) {
		this.writeMolecule(aMolecule, aName, false);
	}

	/**
	 * Writes the given Molecule to hard disk. If the file exists an index will be added to the name so that no files will be
	 * overridden.
	 * 
	 * @param aMolecule
	 *            Molecule to write to hard disk
	 * @param aName
	 *            Name of the molecule on hard disk.
	 * @param aAddTimestamp
	 *            True if the current time shall be added to the name.
	 */
	public void writeMolecule(IAtomContainer aMolecule, String aName, boolean aAddTimestamp) {
		errorOccured = true;
		if (!this.logMolecules) {
			return;
		}
		try {
			PrintWriter tmpPrintWriter;
			File tmpMolFile = null;
			String tmpMolString = convertIAtomContainerToMolString(aMolecule);
			int tmpIdx = 0;
			do {
				String tmpFilename = this.reportsFolderName + File.separator;
				tmpFilename += aName;
				if (aAddTimestamp) {
					SimpleDateFormat tmpSDF = new SimpleDateFormat("dd.MM.yyyy_HH.mm.ss");
					String tmpDate = tmpSDF.format(new Date());
					tmpFilename += tmpDate;
				}
				if (tmpIdx > 0) {
					tmpFilename += "_" + tmpIdx;
				}
				tmpFilename += ".mol";
				tmpMolFile = new File(tmpFilename);
				tmpIdx++;
			} while (tmpMolFile.exists());
			tmpPrintWriter = new PrintWriter(tmpMolFile);
			tmpPrintWriter.write(tmpMolString);
			tmpPrintWriter.flush();
			tmpPrintWriter.close();
		} catch (Exception e) {
			this.writeError("Error writing molecule!", "ErrorLogger", e);
		}
	}

	/**
	 * This method converts a CDK IAtomContainer into a mol-based string.
	 * 
	 * @param aMolecule
	 *            IAtomConatainer
	 * @return mol-based string
	 * @throws Exception
	 */
	public static String convertIAtomContainerToMolString(final IAtomContainer aMolecule) throws Exception {
		String tmpSDFString = "";
		StringWriter tmpStringWriter = new StringWriter();
		SDFWriter tmpSDFWriter = new SDFWriter(tmpStringWriter);
		tmpSDFWriter.write(aMolecule);
		tmpSDFWriter.close();
		tmpSDFString += tmpStringWriter.toString();
		tmpStringWriter.close();
		return tmpSDFString;
	}

	/**
	 * @return An instance of the ErrorLogger
	 */
	public static ErrorLogger getInstance() {
		return instance;
	}

	/**
	 * @param logMolecules
	 *            Set to true to log molecules on hard disk.
	 */
	public void setLogMolecules(boolean logMolecules) {
		this.logMolecules = logMolecules;
	}

	/**
	 * Close writer. Use setPath() to reset Writer.
	 */
	public void closeErrorHandler() {
		if (this.writer != null) {
			this.writer.close();
		}
	}

	/**
	 * @return True if an error was logged until first invocation of this class or until last reset with setErrorOccured().
	 */
	public boolean isErrorOccured() {
		return errorOccured;
	}

	/**
	 * Sets whether an error was logged or not.
	 * 
	 * @param errorOccured
	 *            False if no error has been logged.
	 */
	public void setErrorOccured(boolean errorOccured) {
		this.errorOccured = errorOccured;
	}

	// End of region
}

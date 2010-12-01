/* $RCSfile$
 * $Author: egonw $
 * $Date: 2008-05-05 12:58:11 +0200 (Mo, 05 Mai 2008) $
 * $Revision: 10819 $
 * 
 * Copyright (C) 2005 by Egon Willighagen <egonw@users.sf.net>
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

import java.io.StringWriter;

import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.io.MDLV2000Writer;

/**
 * @author Egon Willighagen
 * 
 */
public class CDKIOWriter {
	
	/**
	 * Converts the CML string into a MDL mol file.
	 * 
	 * @taverna.consume
	 */
	public static String convertToMDLMolfile(CMLChemFile file) throws Exception {
		StringWriter writer = new StringWriter();
		MDLV2000Writer molWriter = new MDLV2000Writer(writer);
		molWriter.write(file);
		molWriter.close();
		return writer.toString();
	}

	/**
	 * Converts the ChemFile into a CML string.
	 * 
	 * @taverna.consume
	 */
	public static String convertChemFileToCMLString(CMLChemFile chemFile) throws Exception {
		return chemFile.toCML();
	}

	/**
	 * Converts the CML string into a ChemFile.
	 * 
	 * @taverna.consume
	 */
	public static CMLChemFile convertCMLStringToChemFile(String cmlString) throws Exception {
		return new CMLChemFile(cmlString);
	}
}

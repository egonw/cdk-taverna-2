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
package org.openscience.cdk.applications.taverna;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import org.openscience.cdk.ChemFile;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.io.CMLWriter;
import org.openscience.cdk.libio.cml.QSARCustomizer;
import org.openscience.cdk.tools.IDCreator;

/**
 * @author Egon Willighagen
 * 
 */
public class CMLChemFile extends ChemFile {

	private static final long serialVersionUID = -5664142472726700883L;

	/**
	 * Constructs an empty ChemFile.
	 */
	public CMLChemFile() {
		super();
	}

	/**
	 * Constructs a ChemFile from a CML String.
	 * 
	 * @param CMLString
	 *            to deserialize the ChemFile from.
	 * @throws Exception
	 */
	public CMLChemFile(String CMLString) throws Exception {
		CMLReader reader = new CMLReader(new ByteArrayInputStream(CMLString.getBytes()));
		reader.read(this);
		reader.close();
	}

	/**
	 * Serializes this ChemFile into a CML String.
	 * 
	 * @return The CML String serialization.
	 * @throws Exception
	 */
	public String toCML() throws Exception {
		IDCreator.createIDs(this);

		StringWriter stringWriter = new StringWriter();
		CMLWriter writer = new CMLWriter(stringWriter);
		writer.registerCustomizer(new QSARCustomizer());
		writer.write(this);
		stringWriter.close();
		writer.close();
		return stringWriter.toString();
	}

}

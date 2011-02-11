/* $RCSfile$
 * $Author: egonw $
 * $Date: 2008-05-05 12:58:11 +0200 (Mo, 05 Mai 2008) $
 * $Revision: 10819 $
 * 
 * Copyright (C) 2008 by Thomas Kuhn <thomaskuhn@users.sourceforge.net>
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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * Class which stores xml files to disk. The original content compressed to
 * reduce the data size.
 * 
 * @author Thomas Kuhn
 * 
 */
public class XMLFileIO {

	private XMLOutputFactory ouputFactory = null;
	private BufferedReader breader = null;
	private XMLInputFactory inputFactory = null;
	private BufferedOutputStream bufferedOutputStream = null;

	/**
	 * Get the xml stream writer which uses a gzip stream to compress the output
	 * file
	 * 
	 * @param file
	 *            File which gets created with the compressed gzip stream
	 * @return XMLStreamWriter to write the file
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public XMLStreamWriter getXMLStreamWriterWithCompression(File file) throws XMLStreamException, IOException {
		this.bufferedOutputStream = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
		this.ouputFactory = XMLOutputFactory.newInstance();
		return this.ouputFactory.createXMLStreamWriter(this.bufferedOutputStream);
	}

	/**
	 * Get the xml stream reader which reads a gzip file.
	 * 
	 * @param fileName
	 *            File name of file to be read
	 * @return XMLStreamReader to read the file
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public XMLStreamReader getXMLStreamReaderWithCompression(String fileName) throws XMLStreamException, IOException {
		this.inputFactory = XMLInputFactory.newInstance();
		this.breader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName))));
		return this.inputFactory.createXMLStreamReader(this.breader);
	}

	/**
	 * Close the XMLStreamWriter
	 * 
	 * @throws IOException
	 */
	public void closeXMLStreamWriter() throws IOException {
		this.bufferedOutputStream.flush();
		this.bufferedOutputStream.close();
	}

	/**
	 * Close the XMLStreamReader
	 * 
	 * @throws IOException
	 */
	public void closeXMLStreamReader() throws IOException {
		this.breader.close();
	}
}

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

import javax.swing.filechooser.FileFilter;

/**
 * Configurable FileFilter.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class CDKFileFilter extends FileFilter {
	private String filterString = null;
	private String filterDescription = null;

	/**
	 * @param filterDescription
	 *            Description of the depending file extension.
	 * @param filterString
	 *            File extension to be filtered.
	 */
	public CDKFileFilter(String filterDescription, String filterString) {
		this.filterString = filterString;
		this.filterDescription = filterDescription;
	}

	@Override
	public boolean accept(File f) {
		if (f.getName().toLowerCase().endsWith(filterString) || f.isDirectory()) {
			return true;
		}
		return false;
	}

	@Override
	public String getDescription() {
		return this.filterDescription + " (*" + this.filterString + ")";
	}

}

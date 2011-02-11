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
package org.openscience.cdk.applications.taverna.jchempaint;

import java.util.HashMap;
import java.util.List;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;

/**
 * Class which represents the JChemPaint activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class JChemPaintActivity extends AbstractCDKActivity {

	public static final String JCHEMPAINT_ACTIVITY = "JChemPaint";

	/**
	 * Creates a new instance.
	 */
	public JChemPaintActivity() {
		this.OUTPUT_PORTS = new String[] { "Structures" };
	}

	@Override
	protected void addInputPorts() {
		// Nothing to add
	}

	@Override
	protected void addOutputPorts() {
		addOutput(this.OUTPUT_PORTS[0], 1);
	}

	@Override
	public String getActivityName() {
		return JChemPaintActivity.JCHEMPAINT_ACTIVITY;
	}

	@Override
	public HashMap<String, Object> getAdditionalProperties() {
		return new HashMap<String, Object>();
	}

	@Override
	public String getDescription() {
		return "Description: " + JChemPaintActivity.JCHEMPAINT_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.JCHEMPAINT_FOLDER_NAME;
	}

	@Override
	public void work() throws Exception {
		// Get input
		List<CMLChemFile> cmlChemFileList = null;
		byte[] data = (byte[]) this.getConfiguration().getAdditionalProperty(
				CDKTavernaConstants.PROPERTY_CMLCHEMFILE_DATA);
		if (data == null) {
			throw new CDKTavernaException(JChemPaintActivity.JCHEMPAINT_ACTIVITY,
					CDKTavernaException.DATA_CONTAINS_NO_MOLECULE);
		}
		CMLChemFile cmlChemFile = (CMLChemFile) CDKObjectHandler.getObject(data);
		cmlChemFileList = CMLChemFileWrapper.wrapInChemModelList(cmlChemFile);
		if (cmlChemFileList.isEmpty()) {
			throw new CDKTavernaException(JChemPaintActivity.JCHEMPAINT_ACTIVITY,
					CDKTavernaException.DATA_CONTAINS_NO_MOLECULE);
		}
		// Set output
		this.setOutputAsObjectList(cmlChemFileList, this.OUTPUT_PORTS[0]);
	}

}

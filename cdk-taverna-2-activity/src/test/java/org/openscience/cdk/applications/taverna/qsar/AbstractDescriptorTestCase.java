/* $RCSfile$
 * $Author: thomaskuhn $
 * $Date: 2008-08-13 20:55:11 +0200 (Mi, 13 Aug 2008) $
 * $Revision: 12046 $
 * 
 * Copyright (C) 2006 - 2007 by Thomas Kuhn <thomas.kuhn@uni-koeln.de>
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
package org.openscience.cdk.applications.taverna.qsar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaTestCases;
import org.openscience.cdk.applications.taverna.CDKTavernaTestData;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.io.CDKIOFileWriter;

/**
 * Class with contains JUnit-Tests for the CDK-Taverna Project
 * 
 * @author Thomas Kuhn, Andreas Truszkowski
 * 
 */
public abstract class AbstractDescriptorTestCase extends CDKTavernaTestCases {
	// CMLChemFile which contains the original data for the calculaton of the
	// descriptors
	private CMLChemFile[] originalData = null;

	// inputList which contains a list of the input data for the local workers
	protected List<CMLChemFile> inputList = null;

	// map for the local worker input
	protected Map<String, Object> inputMap = null;

	// map for the local worker result
	protected Map<String, Object> resultMap = null;

	// List which contains the calculated results of the local worker
	protected List<CMLChemFile> resultListCalculatedStructures = null;

	// List which contains the NOT calculated results of the local worker
	protected List<CMLChemFile> resultListNOTCalculatedStructures = null;

	protected String[] activityInputNames;

	protected String[] activityOutputNames;

	protected String activityName;
	/**
	 * Instance of the tested processor
	 */
	private AbstractCDKActivity activity;

	protected boolean testDataWith3DCoordinates = false;

	protected boolean testDataWithAtomTypePerception = false;

	public AbstractDescriptorTestCase(Class<? extends AbstractCDKActivity> activityClass) {
		try {
			this.activity = activityClass.newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertEquals(false, true);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertEquals(false, true);
		}
		this.activityInputNames = this.activity.INPUT_PORTS;
		this.activityOutputNames = this.activity.OUTPUT_PORTS;
		this.activityName = this.activity.getActivityName();
	}

	@Override
	protected void executeTest() {
		try {
			getInputList();
			executeActivity();
			checkResults();
		} catch (Exception e) {
			e.printStackTrace();
			// This test causes an error
			assertEquals(false, true);
		}
	}

	protected void getInputList() throws Exception {
		List<byte[]> dataList = new ArrayList<byte[]>();
		inputList = new ArrayList<CMLChemFile>();
		if (testDataWith3DCoordinates) {
			originalData = CDKTavernaTestData.getCMLChemFileWith3DCoordinates();
		} else if (testDataWithAtomTypePerception) {
			originalData = CDKTavernaTestData.getCMLChemFile();
			// AtomTypeTools att =new AtomTypeTools();
		} else {
			originalData = CDKTavernaTestData.getCMLChemFile();
		}
		for (int i = 0; i < originalData.length; i++) {
			inputList.add(originalData[i]);
		}
		for (CMLChemFile c : inputList) {
			dataList.add(CDKObjectHandler.getBytes(c));
		}
		inputMap = new HashMap<String, Object>();
		inputMap.put(activityInputNames[0], dataList);
	}

	@SuppressWarnings("unchecked")
	protected void checkResults() throws Exception {
		resultListCalculatedStructures = new ArrayList<CMLChemFile>();
		List<byte[]> dataListCalculatedStructures = (List<byte[]>) resultMap.get(activityOutputNames[0]);
		for (byte[] data : dataListCalculatedStructures) {
			resultListCalculatedStructures.add((CMLChemFile) CDKObjectHandler.getObject(data));
		}
		resultListNOTCalculatedStructures = new ArrayList<CMLChemFile>();
		List<byte[]> dataListNOTCalculatedStructures = (List<byte[]>) resultMap.get(activityOutputNames[1]);
		for (byte[] data : dataListNOTCalculatedStructures) {
			resultListNOTCalculatedStructures.add((CMLChemFile) CDKObjectHandler.getObject(data));
		}
		boolean notCalculatedResults = false;
		if (resultListNOTCalculatedStructures.size() != 0) {
			notCalculatedResults = true;
			CDKIOFileWriter.writeListOfCMLChemFilesToFile(resultListNOTCalculatedStructures, activityName + "Problem.txt",
					CDKTavernaTestData.getPathForWritingFilesOfUnitTests(false));
		}
		assertEquals(false, notCalculatedResults);
	}

	protected void executeActivity() throws Exception {
		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(activityOutputNames[0], byte[].class);
		expectedOutputTypes.put(activityOutputNames[1], byte[].class);
		resultMap = ActivityInvoker.invokeAsyncActivity(this.activity, inputMap, expectedOutputTypes);
	}
}

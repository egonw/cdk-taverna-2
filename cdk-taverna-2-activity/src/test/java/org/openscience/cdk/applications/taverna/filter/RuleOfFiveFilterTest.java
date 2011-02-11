/* $RCSfile$
 * $Author: thomaskuhn $
 * $Date: 2008-08-13 20:55:38 +0200 (Mi, 13 Aug 2008) $
 * $Revision: 12047 $
 * 
 * Copyright (C) 2006 - 2007 by Thomas Kuhn <thomas.kuhn@uni-koeln.de>
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
package org.openscience.cdk.applications.taverna.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.taverna.t2.activities.testutils.ActivityInvoker;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaTestCases;
import org.openscience.cdk.applications.taverna.CDKTavernaTestData;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.exception.CDKException;

/**
 * Class with contains JUnit-Tests for the CDK-Taverna Project
 * 
 * @author Thomas Kuhn, Andreas Truzskowski
 * 
 */
public class RuleOfFiveFilterTest extends CDKTavernaTestCases {
	// CMLChemFile which contains the original data for the calculaton of the descriptors
	private CMLChemFile[] originalData = null;
	// inputList which contains a list of the input data for the local workers
	private List<CMLChemFile> inputList = null;
	// map for the local worker input
	protected Map<String, Object> inputMap = null;

	// map for the local worker result
	protected Map<String, Object> resultMap = null;
	// Descriptor instance
	private AbstractCDKActivity activity = new RuleOfFiveFilter();

	public RuleOfFiveFilterTest() {
	}

	public static Test suite() {
		return new TestSuite(RuleOfFiveFilterTest.class);
	}

	@SuppressWarnings("unchecked")
	public void executeAsnyc() throws CDKException, Exception {
		List<byte[]> dataList = new ArrayList<byte[]>();
		inputList = new ArrayList<CMLChemFile>();
		originalData = CDKTavernaTestData.getCMLChemFile();
		for (int i = 0; i < originalData.length; i++) {
			inputList.add(originalData[i]);
		}
		for (CMLChemFile c : inputList) {
			dataList.add(CDKObjectHandler.getBytes(c));
		}
		inputMap = new HashMap<String, Object>();
		inputMap.put(activity.INPUT_PORTS[0], dataList);
		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(activity.OUTPUT_PORTS[0], byte[].class);
		expectedOutputTypes.put(activity.OUTPUT_PORTS[1], byte[].class);
		resultMap = ActivityInvoker.invokeAsyncActivity(activity, inputMap, expectedOutputTypes);
		List<byte[]> matchedStructures = (List<byte[]>) resultMap.get(activity.OUTPUT_PORTS[0]);
		List<byte[]> unmatchedStructures = (List<byte[]>) resultMap.get(activity.OUTPUT_PORTS[1]);
		Assert.assertEquals(9, matchedStructures.size());
		Assert.assertEquals(1, unmatchedStructures.size());
	}

	@Override
	protected void executeTest() {
		try {
			this.executeAsnyc();
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

}

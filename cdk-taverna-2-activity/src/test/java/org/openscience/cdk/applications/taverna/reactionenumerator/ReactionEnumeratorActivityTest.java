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
package org.openscience.cdk.applications.taverna.reactionenumerator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;

import org.junit.Assert;
import org.openscience.cdk.Reaction;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaTestCases;
import org.openscience.cdk.applications.taverna.CDKTavernaTestData;
import org.openscience.cdk.applications.taverna.CMLChemFile;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
import org.openscience.cdk.applications.taverna.basicutilities.CMLChemFileWrapper;
import org.openscience.cdk.interfaces.IReaction;

/**
 * Test class for the CML file writer activity.
 * 
 * @author Andreas Truszkowski
 * 
 */
public class ReactionEnumeratorActivityTest extends CDKTavernaTestCases {

	private CDKActivityConfigurationBean configBean;

	private AbstractCDKActivity activity = new ReactionEnumeratorActivity();

	private File dir = null;

	public ReactionEnumeratorActivityTest() {
		super(ReactionEnumeratorActivity.REACTION_ENUMERATOR_ACTIVITY);
	}

	public void makeConfigBean() throws Exception {
		configBean = new CDKActivityConfigurationBean();
		configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_REACTANT_PORTS, new Integer(2));
		configBean.setActivityName(ReactionEnumeratorActivity.REACTION_ENUMERATOR_ACTIVITY);
	}

	public void executeAsynch() throws Exception {
		activity.configure(configBean);
		Map<String, Object> inputs = new HashMap<String, Object>();
		CMLChemFile[] chemFiles = CDKTavernaTestData.getReactionEvaluationEductOne();
		List<byte[]> data = CDKObjectHandler.getBytesList(chemFiles);
		inputs.put(ReactionEnumeratorActivity.REACTANT_PORT + " " + "1", data);
		chemFiles = CDKTavernaTestData.getReactionEvaluationEductTwo();
		data = CDKObjectHandler.getBytesList(chemFiles);
		inputs.put(ReactionEnumeratorActivity.REACTANT_PORT + " " + "2", data);
		byte[] reactionData = CDKObjectHandler.getBytes(CDKTavernaTestData.getReactionEvaluationReaction());
		inputs.put(ReactionEnumeratorActivity.REACTION_PORT, reactionData);
		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(activity.getCOMMENT_PORT(), String.class);
		expectedOutputTypes.put(ReactionEnumeratorActivity.RESULT_PORT, byte[].class);
		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputTypes);
		Assert.assertEquals("Unexpected outputs", 2, outputs.size());
		List<byte[]> objectData = (List<byte[]>) outputs.get(ReactionEnumeratorActivity.RESULT_PORT);
		List<Reaction> resultList = CDKObjectHandler.getReactionList(objectData);
		Assert.assertEquals(9, resultList.size());
		List<String> comment = (List<String>) outputs.get(activity.getCOMMENT_PORT());
		for (String c : comment) {
			Assert.assertTrue(!c.toLowerCase().contains("error"));
		}
	}

	public void cleanUp() {
	}

	public void executeTest() {
		try {
			this.makeConfigBean();
			this.executeAsynch();
			this.cleanUp();
		} catch (Exception e) {
			e.printStackTrace();
			// This test causes an error
			assertEquals(false, true);
		}
	}

}

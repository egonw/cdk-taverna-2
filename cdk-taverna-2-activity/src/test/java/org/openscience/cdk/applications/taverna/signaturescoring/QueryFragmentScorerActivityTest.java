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
package org.openscience.cdk.applications.taverna.signaturescoring;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.taverna.t2.activities.testutils.ActivityInvoker;

import org.junit.Assert;
import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaTestCases;

/**
 * 
 * @author kalai
 */
public class QueryFragmentScorerActivityTest extends CDKTavernaTestCases {

	private CDKActivityConfigurationBean configBean;
	private AbstractCDKActivity activity = new QueryFragmentScorerActivity();

	public QueryFragmentScorerActivityTest() {
		super(QueryFragmentScorerActivity.Query_Fragment_Scorer_ACTIVITY);
	}

	public void makeConfigBean() throws Exception {
		configBean = new CDKActivityConfigurationBean();
		configBean.setActivityName(QueryFragmentScorerActivity.Query_Fragment_Scorer_ACTIVITY);
	}

	@SuppressWarnings("unchecked")
	public void executeAsynch() throws Exception {
		activity.configure(configBean);

		String np_file = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "data"
				+ File.separator + "scoringTestFiles" + File.separator + "np_file";
		String sm_file = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "data"
				+ File.separator + "scoringTestFiles" + File.separator + "sm_file";
		String query_file = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "data"
				+ File.separator + "scoringTestFiles" + File.separator + "query_file";

		List<String> np_files = new ArrayList<String>();
		List<String> sm_files = new ArrayList<String>();
		List<String> query_files = new ArrayList<String>();
		np_files.add(np_file);
		sm_files.add(sm_file);
		query_files.add(query_file);
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put(activity.INPUT_PORTS[0], np_files);
		inputs.put(activity.INPUT_PORTS[1], sm_files);
		inputs.put(activity.INPUT_PORTS[2], query_files);

		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put(activity.OUTPUT_PORTS[0], String.class);
		expectedOutputTypes.put(activity.OUTPUT_PORTS[1], byte[].class);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputTypes);
		Assert.assertEquals("Unexpected outputs", 2, outputs.size());

		List<String> signatures_scores = (ArrayList<String>) outputs.get(activity.OUTPUT_PORTS[0]);
		for (String sign_score : signatures_scores) {

			String[] signs_score = sign_score.split(";");
			for (int i = 2; i < signs_score.length; i++) {
				String score = signs_score[3];
				String expected_score = "0.062469368304149966";
				assertEquals(expected_score, score);
			}
		}
	}

	public void executeTest() {
		try {
			this.makeConfigBean();
			this.executeAsynch();
		} catch (Exception e) {
			e.printStackTrace();
			// This test causes an error
			assertEquals(false, true);
		}

	}

	/**
	 * Method which returns a test suit with the name of this class
	 * 
	 * @return TestSuite
	 */
	public static Test suite() {
		return new TestSuite(QueryFragmentScorerActivityTest.class);
	}
}

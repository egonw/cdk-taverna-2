/*
 * Copyright (C) 2010 - 2011 by Andreas Truszkowski <ATruszkowski@gmx.de>
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
package org.openscience.cdk.applications.taverna.weka.regression;

import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.weka.utilities.AbstractWekaLearningActivity;

/**
 * Class which represents the Weka learning activity.
 * 
 * @author Andreas Truzskowski
 * 
 */
public class WekaRegressionActivity extends AbstractWekaLearningActivity{

	public static final String WEKA_LEARNING_ACTIVITY = "Weka Regression";

	/**
	 * Creates a new instance.
	 */
	public WekaRegressionActivity() {
		this.INPUT_PORTS = new String[] { "Regression Train Datasets", "File" };
		this.OUTPUT_PORTS = new String[] { "Regression Model Files" };
	}

	@Override
	public String getActivityName() {
		return WekaRegressionActivity.WEKA_LEARNING_ACTIVITY;
	}

	@Override
	public String getFolderName() {
		return CDKTavernaConstants.WEKA_REGRESSION_FOLDER_NAME;
	}

}

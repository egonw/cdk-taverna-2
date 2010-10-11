/* $RCSfile$
 * $Author: egonw $
 * $Date: 2008-05-05 12:58:11 +0200 (Mo, 05 Mai 2008) $
 * $Revision: 10819 $
 * 
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
package org.openscience.cdk.applications.taverna.qsar.descriptors.molecular;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.openscience.cdk.applications.taverna.qsar.AbstractDescriptorTestCase;

/**
 * Class with contains JUnit-Tests for the CDK-Taverna Project
 * 
 * @author Andreas Truszkowski
 * 
 */
public class PetitjeanShapeIndexTest extends AbstractDescriptorTestCase {

	/**
	 * Constructor which instantiate the testing processor
	 */
	public PetitjeanShapeIndexTest() {
		super(PetitjeanShapeIndex.class);
		testDataWith3DCoordinates = true;
	}

	/**
	 * Method which returns a test suit with the name of this class
	 * 
	 * @return TestSuite
	 */
	public static Test suite() {
		return new TestSuite(PetitjeanShapeIndexTest.class);
	}

}

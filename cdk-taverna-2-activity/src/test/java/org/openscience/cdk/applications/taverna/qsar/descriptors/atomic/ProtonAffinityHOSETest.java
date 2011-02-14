package org.openscience.cdk.applications.taverna.qsar.descriptors.atomic;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.openscience.cdk.applications.taverna.qsar.AbstractDescriptorTestCase;

/**
 * Class with contains JUnit-Tests for the CDK-Taverna Project
 * 
 * @author Andreas Truszkowski
 * 
 */
public class ProtonAffinityHOSETest extends AbstractDescriptorTestCase {

	/**
	 * Constructor which instantiate the testing processor
	 */
	public ProtonAffinityHOSETest() {
		super(ProtonAffinityHOSE.class);
	}

	/**
	 * Method which returns a test suit with the name of this class
	 * 
	 * @return TestSuite
	 */
	public static Test suite() {
		return new TestSuite(ProtonAffinityHOSETest.class);
	}

}

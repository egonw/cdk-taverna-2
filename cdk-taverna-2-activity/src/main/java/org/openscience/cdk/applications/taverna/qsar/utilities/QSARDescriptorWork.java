package org.openscience.cdk.applications.taverna.qsar.utilities;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.interfaces.IAtomContainer;

public class QSARDescriptorWork {
	public IAtomContainer molecule;
	public Class<? extends AbstractCDKActivity> descriptorClass;
}

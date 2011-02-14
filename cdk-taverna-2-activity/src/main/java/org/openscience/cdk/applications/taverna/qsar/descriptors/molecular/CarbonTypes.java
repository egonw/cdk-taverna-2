package org.openscience.cdk.applications.taverna.qsar.descriptors.molecular;

import org.openscience.cdk.applications.taverna.qsar.AbstractMolecularDescriptor;
import org.openscience.cdk.qsar.IMolecularDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.CarbonTypesDescriptor;

public class CarbonTypes extends AbstractMolecularDescriptor {

	@Override
	public IMolecularDescriptor getDescriptor() {
		return new CarbonTypesDescriptor();
	}

}

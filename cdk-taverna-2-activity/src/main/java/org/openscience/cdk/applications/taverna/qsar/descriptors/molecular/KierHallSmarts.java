package org.openscience.cdk.applications.taverna.qsar.descriptors.molecular;

import org.openscience.cdk.applications.taverna.qsar.AbstractMolecularDescriptor;
import org.openscience.cdk.qsar.IMolecularDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.KierHallSmartsDescriptor;

public class KierHallSmarts extends AbstractMolecularDescriptor {

	@Override
	public IMolecularDescriptor getDescriptor() {
		return new KierHallSmartsDescriptor();
	}

}
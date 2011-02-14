package org.openscience.cdk.applications.taverna.qsar.descriptors.atomic;

import org.openscience.cdk.applications.taverna.qsar.AbstractAtomicDescriptor;
import org.openscience.cdk.qsar.IAtomicDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.ProtonAffinityHOSEDescriptor;

public class ProtonAffinityHOSE extends AbstractAtomicDescriptor {

	@Override
	public IAtomicDescriptor getDescriptor() {
		return new ProtonAffinityHOSEDescriptor();
	}

}

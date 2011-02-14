package org.openscience.cdk.applications.taverna.qsar.descriptors.atomic;

import org.openscience.cdk.applications.taverna.qsar.AbstractAtomicDescriptor;
import org.openscience.cdk.qsar.IAtomicDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.StabilizationPlusChargeDescriptor;

public class StabilizationPlusCharge extends AbstractAtomicDescriptor {

	@Override
	public IAtomicDescriptor getDescriptor() {
		return new StabilizationPlusChargeDescriptor();
	}

}

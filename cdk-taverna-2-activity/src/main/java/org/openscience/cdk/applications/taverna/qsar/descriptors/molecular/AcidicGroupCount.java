package org.openscience.cdk.applications.taverna.qsar.descriptors.molecular;

import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.qsar.AbstractMolecularDescriptor;
import org.openscience.cdk.qsar.IMolecularDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.AcidicGroupCountDescriptor;

public class AcidicGroupCount extends AbstractMolecularDescriptor {

	@Override
	public IMolecularDescriptor getDescriptor() {
		try {
			return new AcidicGroupCountDescriptor();
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.ERROR_CREATING_INSTANCE_OF_DESCRIPTOR,
					this.getClass().getSimpleName(), e);
			return null;
		}
	}

}

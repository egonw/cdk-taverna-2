package org.openscience.cdk.applications.taverna.ui.serviceprovider;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.spi.SPIRegistry;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;

public class CDKServiceProvider implements ServiceDescriptionProvider {

	private SPIRegistry<AbstractCDKActivity> cdkActivityRegistry = new SPIRegistry<AbstractCDKActivity>(AbstractCDKActivity.class);

	/**
	 * Do the actual search for services. Return using the callBack parameter.
	 */
	@SuppressWarnings("unchecked")
	public void findServiceDescriptionsAsync(FindServiceDescriptionsCallBack callBack) {
		CDKServiceDescriptor service;
		// Use callback.status() for long-running searches
		// callBack.status("Resolving example services");
		try {
			List<ServiceDescription> results = new ArrayList<ServiceDescription>();
			// Register activities
			for (AbstractCDKActivity cdkActivity : cdkActivityRegistry.getInstances()) {
				service = new CDKServiceDescriptor(cdkActivity.getClass());
				service.setActivityName(cdkActivity.getActivityName());
				service.setFolderName(cdkActivity.getFolderName());
				// TODO set description
				service.setDescription(cdkActivity.getDescription());
				// service.setConfigurationPanelClass(CDKClassGrabber.getClassByName("org.openscience.cdk.applications.taverna",
				// activity.getConfigurationPanelClass()));
				service.setAdditionalProperties(cdkActivity.getAdditionalProperties());
				results.add(service);
			}
			// partialResults() can also be called several times from inside
			// for-loop if the full search takes a long time
			callBack.partialResults(results);
			// No more results will be coming
			callBack.finished();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * Icon for service provider
	 */
	public Icon getIcon() {
		return null;
	}

	/**
	 * Name of service provider, appears in right click for 'Remove service
	 * provider'
	 */
	public String getName() {
		return "My example service";
	}

	@Override
	public String toString() {
		return getName();
	}

}

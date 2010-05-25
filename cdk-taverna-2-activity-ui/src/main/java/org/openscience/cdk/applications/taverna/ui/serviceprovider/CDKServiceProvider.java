package org.openscience.cdk.applications.taverna.ui.serviceprovider;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKClassGrabber;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;

public class CDKServiceProvider implements ServiceDescriptionProvider {

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
			List<Class> classes = CDKClassGrabber.getClassessOfSuperclass("org.openscience.cdk.applications.taverna",
					AbstractCDKActivity.class);
			// Register activities
			for (Class<? extends AbstractCDKActivity> activityClass : classes) {
				AbstractCDKActivity activity = activityClass.newInstance();
				service = new CDKServiceDescriptor(activityClass);
				service.setActivityName(activity.getActivityName());
				service.setFolderName(activity.getFolderName());
				// TODO set description
				service.setDescription(activity.getDescription());
				service.setConfigurationPanelClass(CDKClassGrabber.getClassByName("org.openscience.cdk.applications.taverna",
						activity.getConfigurationPanelClass()));
				service.setAdditionalProperties(activity.getAdditionalProperties());
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

package org.openscience.cdk.applications.taverna.ui.serviceprovider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.Icon;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.spi.SPIRegistry;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;

public class CDKServiceProvider implements ServiceDescriptionProvider {

	private SPIRegistry<AbstractCDKActivity> cdkActivityRegistry = new SPIRegistry<AbstractCDKActivity>(AbstractCDKActivity.class);

	/**
	 * Do the actual search for services. Return using the callBack parameter.
	 */
	@SuppressWarnings("rawtypes")
	public void findServiceDescriptionsAsync(FindServiceDescriptionsCallBack callBack) {
		CDKServiceDescriptor service;
		// First abuse this method to clean up the previous generated cache.
		this.cleanCache();
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
				service.setDescription(null);// TODO cdkActivity.getDescription());
				service.setAdditionalProperties(cdkActivity.getAdditionalProperties());
				results.add(service);
			}
			// partialResults() can also be called several times from inside
			// for-loop if the full search takes a long time
			callBack.partialResults(results);
			// No more results will be coming
			callBack.finished();
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError(CDKTavernaException.ERROR_PROVIDING_SERVICES, this.getClass().getSimpleName(), e);
		}
	}

	/**
	 * Clears the cache directoy.
	 */
	private void cleanCache() {
		String cacheDir = FileNameGenerator.getCacheDir();
		File cache = new File(cacheDir);
		FileNameGenerator.deleteDir(cache);
		cache.mkdir();
	}

	/**
	 * Icon for service provider
	 */
	public Icon getIcon() {
		return null;
	}

	/**
	 * Name of service provider, appears in right click for 'Remove service provider'
	 */
	public String getName() {
		return "CDK Taverna 2.0";
	}

	@Override
	public String toString() {
		return getName();
	}

	public String getId() {
		return UUID.randomUUID().toString();
	}

}

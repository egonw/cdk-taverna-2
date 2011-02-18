package org.openscience.cdk.applications.taverna.ui.serviceprovider;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.Icon;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workbench.ui.impl.configuration.colour.ColourManager;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKTavernaException;
import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
import org.openscience.cdk.applications.taverna.setup.SetupController;

public class CDKServiceProvider implements ServiceDescriptionProvider {

	private SPIRegistry<AbstractCDKActivity> cdkActivityRegistry = new SPIRegistry<AbstractCDKActivity>(AbstractCDKActivity.class);

	/**
	 * Do the actual search for services. Return using the callBack parameter.
	 */
	@SuppressWarnings("rawtypes")
	public void findServiceDescriptionsAsync(FindServiceDescriptionsCallBack callBack) {
		CDKServiceDescriptor service;
		// Setup CDK-Taverna 2
		SetupController.getInstance().loadConfiguration();
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
				this.setActivityColour(cdkActivity);
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

	private void setActivityColour(AbstractCDKActivity cdkActivity) {
		String name = cdkActivity.getClass().getName();
		String colour;
		if(name.startsWith("org.openscience.cdk.applications.taverna.classification.art2a")) {
			colour = "#00EE76"; // Springgreen2
		} else if(name.startsWith("org.openscience.cdk.applications.taverna.filter")) {
			colour = "#EEAEEE"; // Plum2
		} else if(name.startsWith("org.openscience.cdk.applications.taverna.io")) {
			colour = "#D44942"; // Chili
		} else if(name.startsWith("org.openscience.cdk.applications.taverna.isomorphism")) {
			colour = "#D15FEE"; // Mediumorchid2
		} else if(name.startsWith("org.openscience.cdk.applications.taverna.iterativeio")) {
			colour = "#CD5555"; // Indianred3
		} else if(name.startsWith("org.openscience.cdk.applications.taverna.jchempaint")) {
			colour = "#CCFFCC"; // Offwhitegreen
		} else if(name.startsWith("org.openscience.cdk.applications.taverna.miscellaneous")) {
			colour = "#CCCC00"; // Ralphyellow
		} else if(name.startsWith("org.openscience.cdk.applications.taverna.curation")) {
			colour = "#3CB371"; // Mediumseagreen
		} else if(name.startsWith("org.openscience.cdk.applications.taverna.qsar")) {
			colour = "#49E20E"; // Nerf green
		} else if(name.startsWith("org.openscience.cdk.applications.taverna.reactionenumerator")) {
			colour = "#40E0D0"; // Turquoise
		} else if(name.startsWith("org.openscience.cdk.applications.taverna.renderer")) {
			colour = "#F0E68C"; // Khaki
		} else if(name.startsWith("org.openscience.cdk.applications.taverna.stringconverter")) {
			colour = "#00FFFF"; // Cyan
		} else if(name.startsWith("org.openscience.cdk.applications.taverna.weka")) {
			colour = "#DB2929"; // Brownmadder
		} else if(name.startsWith("org.openscience.cdk.applications.taverna.modelling")) {
			colour = "#00EEEE"; // cyan 2
		} else if(name.startsWith("org.openscience.cdk.applications.taverna.signaturescoring")) {
			colour = "#EEEE00"; // yellow 2
		} else {
			colour = "#FFFFFF"; //White
		}
		ColourManager.getInstance().setPreferredColour(cdkActivity.getClass().getName(), Color.decode(colour));
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

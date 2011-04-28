package org.openscience.cdk.applications.taverna.ui.serviceprovider;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;

public class CDKServiceDescriptor extends ServiceDescription<CDKActivityConfigurationBean> {

	private Class<? extends AbstractCDKActivity> activityClass = null;
	private Class<? extends ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean>> configurationPanelClass = null;
	private String activityName;
	private String folderName;
	private HashMap<String, Object> additionalProperties = new HashMap<String, Object>();

	public CDKServiceDescriptor(Class<? extends AbstractCDKActivity> activityClass) {
		this.activityClass = activityClass;
	}

	/**
	 * The subclass of Activity which should be instantiated when adding a
	 * service for this description
	 */
	@Override
	public Class<? extends Activity<CDKActivityConfigurationBean>> getActivityClass() {
		return this.activityClass;
	}

	/**
	 * The configuration bean which is to be used for configuring the
	 * instantiated activity. Making this bean will typically require some of
	 * the fields set on this service description, like an endpoint URL or
	 * method name.
	 * 
	 */
	@Override
	public CDKActivityConfigurationBean getActivityConfiguration() {
		CDKActivityConfigurationBean bean = new CDKActivityConfigurationBean();
		bean.setActivityName(this.activityName);
		bean.setFolderName(this.folderName);
		bean.setConfigurationPanelClass(this.configurationPanelClass);
		bean.setAdditionalProperties(this.additionalProperties);
		return bean;
	}

	/**
	 * An icon to represent this service description in the service palette.
	 */
	@Override
	public Icon getIcon() {
		ClassLoader cld = getClass().getClassLoader();
		URL url;
		try {
			url = cld.getResources("icons/icon.gif").nextElement();
		} catch (Exception e) {
			// Use standard icon
			return null;
		}
		return new ImageIcon(url);
	}

	/**
	 * The display name that will be shown in service palette and will be used
	 * as a template for processor name when added to workflow.
	 */
	@Override
	public String getName() {
		return this.activityName;
	}

	/**
	 * The path to this service description in the service palette. Folders will
	 * be created for each element of the returned path.
	 */
	@Override
	public List<String> getPath() {
		List<String> folders = new ArrayList<String>();
		folders.add(CDKTavernaConstants.CDK_TAVERNA_FOLDER_NAME);
		String[] subfolders = this.folderName.split("\\\\");
		for (String s : subfolders) {
			folders.add(s);
		}
		return folders;
	}

	/**
	 * Return a list of data values uniquely identifying this service
	 * description (to avoid duplicates). Include only primary key like fields,
	 * ie. ignore descriptions, icons, etc.
	 */
	@Override
	protected List<? extends Object> getIdentifyingData() {
		return Arrays.<Object> asList(this.activityName, this.folderName);
	}

	public String getActivityName() {
		return activityName;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public Class<? extends ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean>> getConfigurationPanelClass() {
		return configurationPanelClass;
	}

	public void setConfigurationPanelClass(
			Class<? extends ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean>> configurationPanelClass) {
		this.configurationPanelClass = configurationPanelClass;
	}

	public void setAdditionalProperties(HashMap<String, Object> additionalProperties) {
		this.additionalProperties = additionalProperties;
	}
}

package org.openscience.cdk.applications.taverna;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Example activity configuration bean.
 * 
 */
public class CDKActivityConfigurationBean implements Serializable {

	private static final long serialVersionUID = -4212664764377739507L;

	private String activityName;
	private String folderName;
	private HashMap<String, Object> additionalProperties = new HashMap<String, Object>();

	@SuppressWarnings("unchecked")
	private Class configurationPanelClass;

	@SuppressWarnings("unchecked")
	public Class getConfigurationPanelClass() {
		return configurationPanelClass;
	}

	@SuppressWarnings("unchecked")
	public void setConfigurationPanelClass(Class confiurationPanelClass) {
		this.configurationPanelClass = confiurationPanelClass;
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

	public HashMap<String, Object> getAdditionalProperties() {
		return additionalProperties;
	}

	public Object getAdditionalProperty(String key) {
		return this.additionalProperties.get(key);
	}

	public void setAdditionalProperties(HashMap<String, Object> additionalProperties) {
		this.additionalProperties = additionalProperties;
	}

	public void addAdditionalProperty(String key, Object object) {
		this.additionalProperties.put(key, object);
	}
}

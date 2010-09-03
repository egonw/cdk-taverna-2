/*
 * Copyright (C) 2010 by Andreas Truszkowski <ATruszkowski@gmx.de>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.openscience.cdk.applications.taverna;

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

	/**
	 * @return the name of the activity.
	 */
	public String getActivityName() {
		return activityName;
	}

	/**
	 * Sets the name of the activity.
	 * 
	 * @param activityName
	 */
	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	/**
	 * @return the folder name of the activity.
	 */
	public String getFolderName() {
		return folderName;
	}

	/**
	 * Sets the foldername of the activity.
	 * 
	 * @param folderName
	 */
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	/**
	 * @return hashmap containing additionally properties needed for the configuration of the activity.
	 */
	public HashMap<String, Object> getAdditionalProperties() {
		return additionalProperties;
	}

	/**
	 * Gets an additionally property needed for the configuration of the activity.
	 * 
	 * @param key
	 * @return property
	 */
	public Object getAdditionalProperty(String key) {
		return this.additionalProperties.get(key);
	}

	/**
	 * Sets the additionally properties needed for the configuration of the activity.
	 * 
	 * @param additionalProperties
	 */
	public void setAdditionalProperties(HashMap<String, Object> additionalProperties) {
		this.additionalProperties = additionalProperties;
	}

	/**
	 * Adds an additionally property needed for the configuration of the activity.
	 * 
	 * @param key
	 * @param object
	 */
	public void addAdditionalProperty(String key, Object object) {
		this.additionalProperties.put(key, object);
	}
}

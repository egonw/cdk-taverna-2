package org.openscience.cdk.applications.taverna;

public class CDKTavernaException extends Exception {

	private static final long serialVersionUID = 4460231083206706250L;
	
	public static final String WRONG_INPUT_PORT_TYPE = "Incorrect input port type";
	
	private String activityName;
	private String type;
	
	public CDKTavernaException(String activityName, String type) {
		super(type + " in " + activityName + " activity.");
		this.activityName = activityName;
		this.type = type;
	}
	
}

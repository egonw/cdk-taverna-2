package org.openscience.cdk.applications.taverna.basicutilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ProgressLogger {

	private static ProgressLogger instance = null;
	private HashMap<String, File> activityFileMap = null;

	private ProgressLogger() {
		this.activityFileMap = new HashMap<String, File>();
	}

	public static synchronized ProgressLogger getInstance() {
		if (instance == null) {
			instance = new ProgressLogger();
		}
		return instance;
	}

	public void writeProgress(String activityName, String progress) {
		Date tmpDate = Calendar.getInstance().getTime();
		File file = this.activityFileMap.get(activityName);
		PrintWriter writer;
		try {
			if (file == null) {
				file = new File(FileNameGenerator.getLogDir() + File.separator + activityName + ".log");
				this.activityFileMap.put(activityName, file);
				writer = new PrintWriter(new FileOutputStream(file, false));
			} else {
				writer = new PrintWriter(new FileOutputStream(file, true));
			}
			writer = new PrintWriter(new FileOutputStream(file, true));
			writer.append(tmpDate.toString() + ": " + progress);
			writer.close();
		} catch (Exception e) {
			ErrorLogger.getInstance().writeError("Could not write progress!", "ProgressLogger", e);
		}
	}
}

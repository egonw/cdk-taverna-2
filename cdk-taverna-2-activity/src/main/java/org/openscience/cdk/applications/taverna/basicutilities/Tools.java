package org.openscience.cdk.applications.taverna.basicutilities;

import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;

public class Tools {

	public static void centerWindowOnScreen(Component window) {
		Point center = new Point((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2, (int) Toolkit
				.getDefaultToolkit().getScreenSize().getHeight() / 2);
		window.setLocation((center.x - window.getWidth() / 2), (center.y - window.getHeight() / 2));
	}

	public static String getDirectory(File file) {
		if (file.isDirectory()) {
			return file.getPath();
		} else {
			return file.getParent();
		}
	}

	public static String getFileName(File file) {
		String result = file.getPath();
		if (file.isDirectory()) {
			return "";
		}
		result = file.getName();
		int idx = result.lastIndexOf(".");
		if (idx > 0) {
			return result.substring(0, idx - 1);
		} else {
			return result;
		}
	}

}

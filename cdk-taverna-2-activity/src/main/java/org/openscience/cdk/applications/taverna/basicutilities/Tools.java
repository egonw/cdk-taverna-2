/*
 * Copyright (C) 2011 by Andreas Truszkowski <ATruszkowski@gmx.de>
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
package org.openscience.cdk.applications.taverna.basicutilities;

import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;

public class Tools {

	/**
	 * Centers target window in the middle of the screen.
	 * 
	 * @param window
	 */
	public static void centerWindowOnScreen(Component window) {
		Point center = new Point((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2, (int) Toolkit
				.getDefaultToolkit().getScreenSize().getHeight() / 2);
		window.setLocation((center.x - window.getWidth() / 2), (center.y - window.getHeight() / 2));
	}

	/**
	 * Extracts the directory of target file.
	 * 
	 * @param file
	 * @return The filepath.
	 */
	public static String getDirectory(File file) {
		if (file.isDirectory()) {
			return file.getPath();
		} else {
			return file.getParent();
		}
	}

	/**
	 * Extracts the filename from target file.
	 * 
	 * @param file
	 * @return The extracted name
	 */
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

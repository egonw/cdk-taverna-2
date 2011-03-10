package org.openscience.cdk.applications.taverna.ui;

import java.awt.Component;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class UITools {
	/**
	 * Checks whether the JTextFiels is an legal argument.
	 * 
	 * @param name
	 *            Name of the field
	 * @param textField
	 *            The checked text field
	 * @param minValue
	 * @param maxValue
	 * @return True whether the input is correct.
	 */
	public static boolean checkTextFieldValueInt(Component comp, String name, JTextField textField, int minValue, int maxValue) {
		try {
			int value = Integer.parseInt(textField.getText());
			if (value < minValue || value > maxValue) {
				JOptionPane.showMessageDialog(comp, "Please enter a valid number! Field: " + name, "Illegal Argument",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(comp, "Please enter a valid number! Field: " + name, "Illegal Argument",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	/**
	 * Checks whether the JTextFiels is an legal argument.
	 * 
	 * @param name
	 *            Name of the field
	 * @param textField
	 *            The checked text field
	 * @param minValue
	 * @param maxValue
	 * @return True whether the input is correct.
	 */
	public static boolean checkTextFieldValueDouble(Component comp, String name, JTextField textField, double minValue, double maxValue) {
		try {
			double value = Double.parseDouble(textField.getText());
			if (value < minValue || value > maxValue) {
				JOptionPane.showMessageDialog(comp, "Please enter a valid number! Field: " + name, "Illegal Argument",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(comp, "Please enter a valid number! Field: " + name, "Illegal Argument",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
}

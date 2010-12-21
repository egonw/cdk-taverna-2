package org.openscience.cdk.applications.taverna.ui.weka.panels;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import weka.clusterers.Clusterer;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JButton;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.border.BevelBorder;

public abstract class AbstractConfigurationFrame extends JDialog {

	private ActionListener okAction = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			if (checkValues()) {
				setVisible(false);
			}
		}
	};

	public AbstractConfigurationFrame() {
		setAlwaysOnTop(true);
		setModal(true);
		this.setTitle(this.getConfiguratedClass().getSimpleName());
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		FlowLayout fl_buttonPanel = (FlowLayout) buttonPanel.getLayout();
		fl_buttonPanel.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(this.okAction);
		buttonPanel.add(btnOk);
	}

	private static final long serialVersionUID = 8133794885345768027L;

	public abstract boolean checkValues();

	public abstract Class<?> getConfiguratedClass();

	public abstract String getName();

	public abstract String[] getOptions();

	public boolean checkTextFieldValue(String name, JTextField textField, int minValue, int maxValue) {
		try {
			int value = Integer.parseInt(textField.getText());
			if (value < minValue || value > maxValue) {
				JOptionPane.showMessageDialog(this, "Please enter a valid number! Field: " + name, "Illegal Argument",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Please enter a valid number! Field: " + name, "Illegal Argument",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
}

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
package org.openscience.cdk.applications.taverna.setup;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import java.awt.Color;

/**
 * Setup dialog
 * 
 * @author Andreas Truszkowski
 *
 */
public class SetupDialog extends JDialog {

	private static final long serialVersionUID = 8323781543039721448L;

	private final JPanel contentPanel = new JPanel();
	private JTextField workingDirectoryTextField;
	private JButton button;
	private JButton okButton;
	private JButton cancelButton;
	private JCheckBox chckbxCacheDatarecommended;
	private JCheckBox chckbxCompessData;

	/**
	 * Create the dialog.
	 */
	public SetupDialog() {
		setModal(true);
		setTitle("CDK-Taverna 2.0 Setup");
		setBounds(100, 100, 450, 225);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		setResizable(false);
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		SpringLayout sl_contentPanel = new SpringLayout();
		contentPanel.setLayout(sl_contentPanel);

		JLabel lblWorkingDirectrory = new JLabel("Working Directrory:");
		sl_contentPanel.putConstraint(SpringLayout.NORTH, lblWorkingDirectrory, 10, SpringLayout.NORTH, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.WEST, lblWorkingDirectrory, 10, SpringLayout.WEST, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.EAST, lblWorkingDirectrory, -10, SpringLayout.EAST, contentPanel);
		contentPanel.add(lblWorkingDirectrory);

		workingDirectoryTextField = new JTextField();
		sl_contentPanel.putConstraint(SpringLayout.NORTH, workingDirectoryTextField, 9, SpringLayout.SOUTH,
				lblWorkingDirectrory);
		workingDirectoryTextField.setEditable(false);
		sl_contentPanel
				.putConstraint(SpringLayout.WEST, workingDirectoryTextField, 10, SpringLayout.WEST, contentPanel);
		contentPanel.add(workingDirectoryTextField);
		workingDirectoryTextField.setColumns(10);

		button = new JButton("");
		sl_contentPanel.putConstraint(SpringLayout.SOUTH, button, 31, SpringLayout.SOUTH, lblWorkingDirectrory);
		sl_contentPanel.putConstraint(SpringLayout.EAST, workingDirectoryTextField, -10, SpringLayout.WEST, button);
		sl_contentPanel.putConstraint(SpringLayout.NORTH, button, 6, SpringLayout.SOUTH, lblWorkingDirectrory);
		sl_contentPanel.putConstraint(SpringLayout.WEST, button, -35, SpringLayout.EAST, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.EAST, button, -10, SpringLayout.EAST, contentPanel);
		contentPanel.add(button);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Data Management", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		sl_contentPanel.putConstraint(SpringLayout.NORTH, panel, 10, SpringLayout.SOUTH, workingDirectoryTextField);
		sl_contentPanel.putConstraint(SpringLayout.WEST, panel, 10, SpringLayout.WEST, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.SOUTH, panel, -10, SpringLayout.SOUTH, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.EAST, panel, -10, SpringLayout.EAST, contentPanel);
		contentPanel.add(panel);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		chckbxCacheDatarecommended = new JCheckBox("Use Data Caching (recommended)");
		sl_contentPanel.putConstraint(SpringLayout.NORTH, chckbxCacheDatarecommended, 25, SpringLayout.NORTH, panel);
		sl_contentPanel.putConstraint(SpringLayout.WEST, chckbxCacheDatarecommended, 127, SpringLayout.WEST, panel);
		panel.add(chckbxCacheDatarecommended);
		chckbxCacheDatarecommended.setToolTipText("Cache CDK-Taverna 2.0 data on hard disk instead of using the memory or the provenance database.");
		chckbxCacheDatarecommended.setSelected(true);
		sl_contentPanel.putConstraint(SpringLayout.EAST, chckbxCacheDatarecommended, 316, SpringLayout.WEST, contentPanel);
		
		chckbxCompessData = new JCheckBox("Compess Data");
		chckbxCompessData.setSelected(true);
		sl_panel.putConstraint(SpringLayout.NORTH, chckbxCompessData, 6, SpringLayout.SOUTH, chckbxCacheDatarecommended);
		sl_panel.putConstraint(SpringLayout.WEST, chckbxCompessData, 0, SpringLayout.WEST, panel);
		panel.add(chckbxCompessData);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	public JButton getChooseDirectoryButton() {
		return button;
	}

	public JTextField getWorkingDirectoryTextField() {
		return workingDirectoryTextField;
	}

	public JButton getOkButton() {
		return okButton;
	}

	public JButton getCancelButton() {
		return cancelButton;
	}
	public JCheckBox getChckbxCacheDatarecommended() {
		return chckbxCacheDatarecommended;
	}
	public JCheckBox getChckbxCompessData() {
		return chckbxCompessData;
	}
}

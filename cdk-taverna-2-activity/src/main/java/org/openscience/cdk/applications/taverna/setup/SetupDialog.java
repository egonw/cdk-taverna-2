package org.openscience.cdk.applications.taverna.setup;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.JCheckBox;

public class SetupDialog extends JDialog {

	private static final long serialVersionUID = 8323781543039721448L;

	private final JPanel contentPanel = new JPanel();
	private JTextField workingDirectoryTextField;
	private JButton button;
	private JButton okButton;
	private JButton cancelButton;
	private JCheckBox chckbxCacheDatarecommended;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			SetupDialog dialog = new SetupDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public SetupDialog() {
		setModal(true);
		setTitle("CDK-Taverna 2.0 Setup");
		setBounds(100, 100, 450, 165);
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
		
		chckbxCacheDatarecommended = new JCheckBox("Cache data (recommended)");
		chckbxCacheDatarecommended.setToolTipText("Cache CDK-Taverna 2.0 data on hard disk instead of using the memory or the provenance database.");
		chckbxCacheDatarecommended.setSelected(true);
		sl_contentPanel.putConstraint(SpringLayout.NORTH, chckbxCacheDatarecommended, 10, SpringLayout.SOUTH, workingDirectoryTextField);
		sl_contentPanel.putConstraint(SpringLayout.WEST, chckbxCacheDatarecommended, 0, SpringLayout.WEST, lblWorkingDirectrory);
		sl_contentPanel.putConstraint(SpringLayout.EAST, chckbxCacheDatarecommended, 316, SpringLayout.WEST, contentPanel);
		contentPanel.add(chckbxCacheDatarecommended);
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
}

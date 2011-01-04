package org.openscience.cdk.applications.taverna.ui.art2a;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;

public class ART2aView extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTextField numberOfClassificationsTextField;
	private JTextField upperVigilanceLimitTextField;
	private JTextField lowerVigilanceLimitTextField;
	private JTextField maximumClassificationTimeTextField;
	private JCheckBox scaleFingerprintItemsCheckBox;
	private JTextField pathTextField;
	private JButton pathButton;

	public ART2aView() {
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setPreferredSize(new Dimension(420, 147));
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);

		JLabel lblNumberOfClassifications = new JLabel("Number of classifications:");
		springLayout.putConstraint(SpringLayout.NORTH, lblNumberOfClassifications, 13, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, lblNumberOfClassifications, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, lblNumberOfClassifications, 150, SpringLayout.WEST, this);
		add(lblNumberOfClassifications);

		JLabel lblUpperVigilanceParameter = new JLabel("Upper vigilance limit:");
		springLayout.putConstraint(SpringLayout.NORTH, lblUpperVigilanceParameter, 17, SpringLayout.SOUTH,
				lblNumberOfClassifications);
		springLayout.putConstraint(SpringLayout.WEST, lblUpperVigilanceParameter, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, lblUpperVigilanceParameter, 150, SpringLayout.WEST, this);
		add(lblUpperVigilanceParameter);

		JLabel lblLowerVigilanceParameter = new JLabel("Lower vigilance limit:");
		springLayout.putConstraint(SpringLayout.NORTH, lblLowerVigilanceParameter, 17, SpringLayout.SOUTH,
				lblUpperVigilanceParameter);
		springLayout.putConstraint(SpringLayout.WEST, lblLowerVigilanceParameter, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, lblLowerVigilanceParameter, 150, SpringLayout.WEST, this);
		add(lblLowerVigilanceParameter);

		numberOfClassificationsTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, numberOfClassificationsTextField, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, numberOfClassificationsTextField, 0, SpringLayout.EAST,
				lblNumberOfClassifications);
		springLayout.putConstraint(SpringLayout.EAST, numberOfClassificationsTextField, 40, SpringLayout.EAST,
				lblNumberOfClassifications);
		add(numberOfClassificationsTextField);
		numberOfClassificationsTextField.setColumns(10);

		upperVigilanceLimitTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, upperVigilanceLimitTextField, 10, SpringLayout.SOUTH,
				numberOfClassificationsTextField);
		springLayout.putConstraint(SpringLayout.WEST, upperVigilanceLimitTextField, 0, SpringLayout.EAST,
				lblUpperVigilanceParameter);
		springLayout.putConstraint(SpringLayout.EAST, upperVigilanceLimitTextField, 40, SpringLayout.EAST,
				lblUpperVigilanceParameter);
		add(upperVigilanceLimitTextField);
		upperVigilanceLimitTextField.setColumns(10);

		lowerVigilanceLimitTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, lowerVigilanceLimitTextField, 10, SpringLayout.SOUTH,
				upperVigilanceLimitTextField);
		springLayout.putConstraint(SpringLayout.WEST, lowerVigilanceLimitTextField, 0, SpringLayout.EAST,
				lblLowerVigilanceParameter);
		springLayout.putConstraint(SpringLayout.EAST, lowerVigilanceLimitTextField, 40, SpringLayout.EAST,
				lblLowerVigilanceParameter);
		add(lowerVigilanceLimitTextField);
		lowerVigilanceLimitTextField.setColumns(10);

		JLabel lblMaximumClassificationTimew = new JLabel("Maximum classification time:");
		springLayout.putConstraint(SpringLayout.NORTH, lblMaximumClassificationTimew, 13, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, lblMaximumClassificationTimew, 10, SpringLayout.EAST,
				numberOfClassificationsTextField);
		springLayout.putConstraint(SpringLayout.EAST, lblMaximumClassificationTimew, 175, SpringLayout.EAST,
				numberOfClassificationsTextField);
		add(lblMaximumClassificationTimew);

		scaleFingerprintItemsCheckBox = new JCheckBox("Scale fingerprint items");
		springLayout.putConstraint(SpringLayout.NORTH, scaleFingerprintItemsCheckBox, 42, SpringLayout.SOUTH,
				lblMaximumClassificationTimew);
		springLayout.putConstraint(SpringLayout.WEST, scaleFingerprintItemsCheckBox, 40, SpringLayout.EAST,
				upperVigilanceLimitTextField);
		springLayout.putConstraint(SpringLayout.EAST, scaleFingerprintItemsCheckBox, 200, SpringLayout.EAST,
				upperVigilanceLimitTextField);
		add(scaleFingerprintItemsCheckBox);

		maximumClassificationTimeTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, maximumClassificationTimeTextField, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, maximumClassificationTimeTextField, 0, SpringLayout.EAST,
				lblMaximumClassificationTimew);
		springLayout.putConstraint(SpringLayout.EAST, maximumClassificationTimeTextField, 40, SpringLayout.EAST,
				lblMaximumClassificationTimew);
		add(maximumClassificationTimeTextField);
		maximumClassificationTimeTextField.setColumns(10);

		pathTextField = new JTextField();
		pathTextField.setEditable(false);
		springLayout.putConstraint(SpringLayout.NORTH, pathTextField, -35, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.WEST, pathTextField, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, pathTextField, -10, SpringLayout.SOUTH, this);
		add(pathTextField);
		pathTextField.setColumns(10);

		pathButton = new JButton("");
		pathButton.setBorderPainted(false);
		pathButton.setContentAreaFilled(false);
		springLayout.putConstraint(SpringLayout.NORTH, pathButton, -35, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.SOUTH, pathButton, -10, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, pathTextField, -10, SpringLayout.WEST, pathButton);
		springLayout.putConstraint(SpringLayout.WEST, pathButton, -35, SpringLayout.EAST, this);
		springLayout.putConstraint(SpringLayout.EAST, pathButton, -10, SpringLayout.EAST, this);
		add(pathButton);
	}

	public JTextField getNumberOfClassificationsTextField() {
		return numberOfClassificationsTextField;
	}

	public JTextField getUpperVigilanceLimitTextField() {
		return upperVigilanceLimitTextField;
	}

	public JTextField getLowerVigilanceLimitTextField() {
		return lowerVigilanceLimitTextField;
	}

	public JCheckBox getScaleFingerprintItemsCheckBox() {
		return scaleFingerprintItemsCheckBox;
	}

	public JTextField getMaximumClassificationTimeTextField() {
		return maximumClassificationTimeTextField;
	}

	public JTextField getPathTextField() {
		return pathTextField;
	}

	public JButton getPathButton() {
		return pathButton;
	}
}

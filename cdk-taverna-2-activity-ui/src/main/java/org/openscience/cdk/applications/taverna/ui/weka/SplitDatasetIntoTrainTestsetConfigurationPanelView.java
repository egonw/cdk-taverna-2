package org.openscience.cdk.applications.taverna.ui.weka;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;
import javax.swing.JButton;

public class SplitDatasetIntoTrainTestsetConfigurationPanelView extends JPanel {

	private static final long serialVersionUID = -6560279042248681080L;
	private JTextField iterationsTextField;
	private JComboBox classifierComboBox;
	private final Action action = new RandomAction();
	private JCheckBox useBlacklistingCheckBox;
	private final Action action_1 = new ClusterRepresentativesAction();
	private final Action action_2 = new SingleGlobalMaxAction();
	private JRadioButton rdbtnNewRadioButton;
	private JRadioButton rdbtnNewRadioButton_1;
	private JRadioButton rdbtnNewRadioButton_2;
	private JTextField lowerRatioTextField;
	private JTextField higherRatioTextField;
	private JTextField stepsTextField;
	private JCheckBox chooseBestCheckBox;
	private JButton btnConfigure;

	/**
	 * Create the panel.
	 */
	public SplitDatasetIntoTrainTestsetConfigurationPanelView() {
		setPreferredSize(new Dimension(425, 245));
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);

		ButtonGroup group = new ButtonGroup();
		rdbtnNewRadioButton = new JRadioButton("Random");
		rdbtnNewRadioButton.setAction(action);
		rdbtnNewRadioButton.setSelected(true);
		add(rdbtnNewRadioButton);
		group.add(rdbtnNewRadioButton);

		rdbtnNewRadioButton_1 = new JRadioButton("Cluster Representatives");
		springLayout.putConstraint(SpringLayout.WEST, rdbtnNewRadioButton, 0, SpringLayout.WEST, rdbtnNewRadioButton_1);
		springLayout.putConstraint(SpringLayout.SOUTH, rdbtnNewRadioButton, -6, SpringLayout.NORTH, rdbtnNewRadioButton_1);
		springLayout.putConstraint(SpringLayout.NORTH, rdbtnNewRadioButton_1, 39, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, rdbtnNewRadioButton_1, 10, SpringLayout.WEST, this);
		rdbtnNewRadioButton_1.setAction(action_1);
		add(rdbtnNewRadioButton_1);
		group.add(rdbtnNewRadioButton_1);

		rdbtnNewRadioButton_2 = new JRadioButton("SingleGlobalMax");
		springLayout.putConstraint(SpringLayout.NORTH, rdbtnNewRadioButton_2, 6, SpringLayout.SOUTH, rdbtnNewRadioButton_1);
		springLayout.putConstraint(SpringLayout.WEST, rdbtnNewRadioButton_2, 0, SpringLayout.WEST, rdbtnNewRadioButton);
		springLayout.putConstraint(SpringLayout.EAST, rdbtnNewRadioButton_2, 215, SpringLayout.WEST, this);
		rdbtnNewRadioButton_2.setAction(action_2);
		add(rdbtnNewRadioButton_2);
		group.add(rdbtnNewRadioButton_2);

		JLabel lblNewLabel = new JLabel("Classifier:");
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel, 11, SpringLayout.SOUTH, rdbtnNewRadioButton_2);
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel, 34, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, lblNewLabel, -288, SpringLayout.EAST, this);
		add(lblNewLabel);

		classifierComboBox = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, classifierComboBox, -3, SpringLayout.NORTH, lblNewLabel);
		springLayout.putConstraint(SpringLayout.EAST, classifierComboBox, -107, SpringLayout.EAST, this);
		classifierComboBox.setEnabled(false);
		add(classifierComboBox);

		iterationsTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.WEST, classifierComboBox, 0, SpringLayout.WEST, iterationsTextField);
		springLayout.putConstraint(SpringLayout.NORTH, iterationsTextField, 124, SpringLayout.NORTH, this);
		iterationsTextField.setEnabled(false);
		iterationsTextField.setText("10");
		add(iterationsTextField);
		iterationsTextField.setColumns(10);

		JLabel lblIterations = new JLabel("Iterations:");
		springLayout.putConstraint(SpringLayout.NORTH, lblIterations, 127, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, lblIterations, 34, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.WEST, iterationsTextField, 13, SpringLayout.EAST, lblIterations);
		add(lblIterations);

		useBlacklistingCheckBox = new JCheckBox("Use Blacklisting");
		springLayout.putConstraint(SpringLayout.NORTH, useBlacklistingCheckBox, 4, SpringLayout.SOUTH,
				iterationsTextField);
		springLayout.putConstraint(SpringLayout.WEST, useBlacklistingCheckBox, 34, SpringLayout.WEST, this);
		useBlacklistingCheckBox.setEnabled(false);
		useBlacklistingCheckBox.setSelected(true);
		add(useBlacklistingCheckBox);

		JLabel lblNewLabel_1 = new JLabel("Trainingset Size (in %):");
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel_1, 10, SpringLayout.WEST, this);
		add(lblNewLabel_1);

		lowerRatioTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.WEST, lowerRatioTextField, 6, SpringLayout.EAST, lblNewLabel_1);
		springLayout.putConstraint(SpringLayout.EAST, lowerRatioTextField, 36, SpringLayout.EAST, lblNewLabel_1);
		lowerRatioTextField.setText("5");
		add(lowerRatioTextField);
		lowerRatioTextField.setColumns(10);

		JLabel lblNewLabel_2 = new JLabel("-");
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel_2, 6, SpringLayout.EAST, lowerRatioTextField);
		add(lblNewLabel_2);

		higherRatioTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.EAST, higherRatioTextField, 36, SpringLayout.EAST, lblNewLabel_2);
		higherRatioTextField.setText("50");
		springLayout.putConstraint(SpringLayout.WEST, higherRatioTextField, 6, SpringLayout.EAST, lblNewLabel_2);
		add(higherRatioTextField);
		higherRatioTextField.setColumns(10);

		JLabel lblOfsteps = new JLabel("# of Steps:");
		springLayout.putConstraint(SpringLayout.EAST, rdbtnNewRadioButton, -190, SpringLayout.EAST, lblOfsteps);
		springLayout.putConstraint(SpringLayout.WEST, lblOfsteps, 6, SpringLayout.EAST, higherRatioTextField);
		add(lblOfsteps);

		stepsTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.EAST, stepsTextField, 36, SpringLayout.EAST, lblOfsteps);
		stepsTextField.setText("10");
		springLayout.putConstraint(SpringLayout.WEST, stepsTextField, 6, SpringLayout.EAST, lblOfsteps);
		add(stepsTextField);
		stepsTextField.setColumns(10);

		chooseBestCheckBox = new JCheckBox("Choose best");
		springLayout.putConstraint(SpringLayout.WEST, chooseBestCheckBox, 34, SpringLayout.WEST, this);
		chooseBestCheckBox.setEnabled(false);
		springLayout.putConstraint(SpringLayout.NORTH, chooseBestCheckBox, 4, SpringLayout.SOUTH,
				useBlacklistingCheckBox);
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel_2, 9, SpringLayout.SOUTH, chooseBestCheckBox);
		springLayout.putConstraint(SpringLayout.NORTH, lblOfsteps, 9, SpringLayout.SOUTH, chooseBestCheckBox);
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel_1, 9, SpringLayout.SOUTH, chooseBestCheckBox);
		springLayout.putConstraint(SpringLayout.NORTH, stepsTextField, 6, SpringLayout.SOUTH, chooseBestCheckBox);
		springLayout.putConstraint(SpringLayout.NORTH, higherRatioTextField, 6, SpringLayout.SOUTH, chooseBestCheckBox);
		springLayout.putConstraint(SpringLayout.NORTH, lowerRatioTextField, 6, SpringLayout.SOUTH, chooseBestCheckBox);
		chooseBestCheckBox.setSelected(true);
		add(chooseBestCheckBox);
		
		btnConfigure = new JButton("Configure");
		btnConfigure.setEnabled(false);
		springLayout.putConstraint(SpringLayout.NORTH, btnConfigure, -4, SpringLayout.NORTH, lblNewLabel);
		springLayout.putConstraint(SpringLayout.WEST, btnConfigure, 6, SpringLayout.EAST, classifierComboBox);
		springLayout.putConstraint(SpringLayout.SOUTH, btnConfigure, -83, SpringLayout.NORTH, stepsTextField);
		springLayout.putConstraint(SpringLayout.EAST, btnConfigure, -12, SpringLayout.EAST, this);
		add(btnConfigure);

	}

	public JComboBox getClassifierComboBox() {
		return classifierComboBox;
	}

	private class RandomAction extends AbstractAction {

		private static final long serialVersionUID = -2635227445213863566L;

		public RandomAction() {
			putValue(NAME, "Random");
		}

		public void actionPerformed(ActionEvent e) {
			classifierComboBox.setEnabled(false);
			useBlacklistingCheckBox.setEnabled(false);
			iterationsTextField.setEnabled(false);
			chooseBestCheckBox.setEnabled(false);
			btnConfigure.setEnabled(false);
		}
	}

	public JTextField getIterationsTextField() {
		return iterationsTextField;
	}

	public JCheckBox getUseBlacklistingCheckBox() {
		return useBlacklistingCheckBox;
	}

	private class ClusterRepresentativesAction extends AbstractAction {

		private static final long serialVersionUID = -3045669431561871860L;

		public ClusterRepresentativesAction() {
			putValue(NAME, "Cluster Representatives");
		}

		public void actionPerformed(ActionEvent e) {
			classifierComboBox.setEnabled(false);
			useBlacklistingCheckBox.setEnabled(false);
			iterationsTextField.setEnabled(false);
			chooseBestCheckBox.setEnabled(false);
			btnConfigure.setEnabled(false);
		}
	}

	private class SingleGlobalMaxAction extends AbstractAction {

		private static final long serialVersionUID = 113797985498836349L;

		public SingleGlobalMaxAction() {
			putValue(NAME, "SingleGlobalMax");
		}

		public void actionPerformed(ActionEvent e) {
			classifierComboBox.setEnabled(true);
			useBlacklistingCheckBox.setEnabled(true);
			iterationsTextField.setEnabled(true);
			chooseBestCheckBox.setEnabled(true);
			btnConfigure.setEnabled(true);
		}
	}

	public JRadioButton getRandomRadioButton() {
		return rdbtnNewRadioButton;
	}

	public JRadioButton getClusterRadioButton() {
		return rdbtnNewRadioButton_1;
	}

	public JRadioButton getSingleGlobalMaxRadioButton() {
		return rdbtnNewRadioButton_2;
	}

	public JTextField getLowerRatioTextField() {
		return lowerRatioTextField;
	}

	public JTextField getHigherRatioTextField() {
		return higherRatioTextField;
	}

	public JTextField getStepsTextField() {
		return stepsTextField;
	}

	public JCheckBox getChooseBestCheckBox() {
		return chooseBestCheckBox;
	}
	public JButton getBtnConfigure() {
		return btnConfigure;
	}
}

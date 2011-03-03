package org.openscience.cdk.applications.taverna.ui.weka;

import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import java.awt.Dimension;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.Action;

public class CreateLearningDatasetConfigurationPanelView extends JPanel {
	private JTextField iterationsTextField;
	private JComboBox classifierComboBox;
	private final Action action = new RandomAction();
	private JCheckBox useBlacklistingCheckBox;
	private final Action action_1 = new ClusterRepresentativesAction();
	private final Action action_2 = new SingleGlobalMaxAction();
	private JRadioButton rdbtnNewRadioButton;
	private JRadioButton rdbtnNewRadioButton_1;
	private JRadioButton rdbtnNewRadioButton_2;

	/**
	 * Create the panel.
	 */
	public CreateLearningDatasetConfigurationPanelView() {
		setPreferredSize(new Dimension(325, 185));
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		ButtonGroup group = new ButtonGroup();
		rdbtnNewRadioButton = new JRadioButton("Random");
		rdbtnNewRadioButton.setAction(action);
		rdbtnNewRadioButton.setSelected(true);
		springLayout.putConstraint(SpringLayout.NORTH, rdbtnNewRadioButton, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, rdbtnNewRadioButton, 10, SpringLayout.WEST, this);
		add(rdbtnNewRadioButton);
		group.add(rdbtnNewRadioButton);
		
		rdbtnNewRadioButton_1 = new JRadioButton("Cluster Representatives");
		rdbtnNewRadioButton_1.setAction(action_1);
		springLayout.putConstraint(SpringLayout.NORTH, rdbtnNewRadioButton_1, 6, SpringLayout.SOUTH, rdbtnNewRadioButton);
		springLayout.putConstraint(SpringLayout.WEST, rdbtnNewRadioButton_1, 0, SpringLayout.WEST, rdbtnNewRadioButton);
		add(rdbtnNewRadioButton_1);
		group.add(rdbtnNewRadioButton_1);
		
		rdbtnNewRadioButton_2 = new JRadioButton("SingleGlobalMax");
		rdbtnNewRadioButton_2.setAction(action_2);
		springLayout.putConstraint(SpringLayout.NORTH, rdbtnNewRadioButton_2, 6, SpringLayout.SOUTH, rdbtnNewRadioButton_1);
		springLayout.putConstraint(SpringLayout.WEST, rdbtnNewRadioButton_2, 0, SpringLayout.WEST, rdbtnNewRadioButton);
		add(rdbtnNewRadioButton_2);
		group.add(rdbtnNewRadioButton_2);
		
		JLabel lblNewLabel = new JLabel("Classifier:");
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel, 11, SpringLayout.SOUTH, rdbtnNewRadioButton_2);
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel, 34, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, lblNewLabel, -63, SpringLayout.EAST, rdbtnNewRadioButton_1);
		add(lblNewLabel);
		
		classifierComboBox = new JComboBox();
		classifierComboBox.setEnabled(false);
		springLayout.putConstraint(SpringLayout.NORTH, classifierComboBox, 7, SpringLayout.SOUTH, rdbtnNewRadioButton_2);
		springLayout.putConstraint(SpringLayout.WEST, classifierComboBox, 8, SpringLayout.EAST, lblNewLabel);
		springLayout.putConstraint(SpringLayout.EAST, classifierComboBox, 221, SpringLayout.EAST, lblNewLabel);
		add(classifierComboBox);
		
		iterationsTextField = new JTextField();
		iterationsTextField.setEnabled(false);
		springLayout.putConstraint(SpringLayout.NORTH, iterationsTextField, 6, SpringLayout.SOUTH, classifierComboBox);
		springLayout.putConstraint(SpringLayout.WEST, iterationsTextField, 0, SpringLayout.WEST, classifierComboBox);
		iterationsTextField.setText("10");
		add(iterationsTextField);
		iterationsTextField.setColumns(10);
		
		JLabel lblIterations = new JLabel("Iterations:");
		springLayout.putConstraint(SpringLayout.NORTH, lblIterations, 3, SpringLayout.NORTH, iterationsTextField);
		springLayout.putConstraint(SpringLayout.WEST, lblIterations, 0, SpringLayout.WEST, lblNewLabel);
		add(lblIterations);
		
		useBlacklistingCheckBox = new JCheckBox("Use Blacklisting");
		useBlacklistingCheckBox.setEnabled(false);
		springLayout.putConstraint(SpringLayout.NORTH, useBlacklistingCheckBox, 4, SpringLayout.SOUTH, iterationsTextField);
		useBlacklistingCheckBox.setSelected(true);
		springLayout.putConstraint(SpringLayout.WEST, useBlacklistingCheckBox, 0, SpringLayout.WEST, lblNewLabel);
		add(useBlacklistingCheckBox);

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
}

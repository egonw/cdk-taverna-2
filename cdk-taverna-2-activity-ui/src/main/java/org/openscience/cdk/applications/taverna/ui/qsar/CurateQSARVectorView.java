package org.openscience.cdk.applications.taverna.ui.qsar;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.BevelBorder;

public class CurateQSARVectorView extends JPanel {

	private static final long serialVersionUID = 1L;
	private JRadioButton rdbtnDynamicCuration;
	private JRadioButton rdbtnCurateOnlyColumns;
	private JRadioButton rdbtnCurateOnlyRows;
	private JCheckBox chckbxRemoveMinmaxValue;

	public CurateQSARVectorView() {
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setPreferredSize(new Dimension(300, 150));
		setLayout(new GridLayout(0, 1, 0, 0));

		rdbtnDynamicCuration = new JRadioButton("Dynamic curation between rows and columns");
		rdbtnDynamicCuration.setSelected(true);
		add(rdbtnDynamicCuration);

		rdbtnCurateOnlyColumns = new JRadioButton("Curate only columns (Reject descriptors)");
		add(rdbtnCurateOnlyColumns);

		rdbtnCurateOnlyRows = new JRadioButton("Curate only rows (Reject molecules)");
		add(rdbtnCurateOnlyRows);
		ButtonGroup group = new ButtonGroup();
		group.add(rdbtnCurateOnlyColumns);
		group.add(rdbtnCurateOnlyRows);
		group.add(rdbtnDynamicCuration);
		chckbxRemoveMinmaxValue = new JCheckBox("Remove values where the min/max  not differ");
		chckbxRemoveMinmaxValue.setSelected(true);
		add(chckbxRemoveMinmaxValue);
	}

	public JRadioButton getRdbtnDynamicCuration() {
		return rdbtnDynamicCuration;
	}

	public JRadioButton getRdbtnCurateOnlyColumns() {
		return rdbtnCurateOnlyColumns;
	}

	public JRadioButton getRdbtnCurateOnlyRows() {
		return rdbtnCurateOnlyRows;
	}

	public JCheckBox getChckbxRemoveMinmaxValue() {
		return chckbxRemoveMinmaxValue;
	}
}

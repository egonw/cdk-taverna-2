package org.openscience.cdk.applications.taverna.ui.qsar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;

public class QSARDescriptorConfigurationPanel extends
		ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> {

	private static final long serialVersionUID = 3360691883759809971L;

	private SPIRegistry<AbstractCDKActivity> cdkActivityRegistry = new SPIRegistry<AbstractCDKActivity>(AbstractCDKActivity.class);

	private AbstractCDKActivity activity;
	private CDKActivityConfigurationBean configBean;
	private JButton selectAllButton = null;
	private JButton clearButton = null;

	private HashMap<Class<? extends AbstractCDKActivity>, JCheckBox> selectionMap = null;
	private ArrayList<Class<? extends AbstractCDKActivity>> selectedClasses = new ArrayList<Class<? extends AbstractCDKActivity>>();

	public QSARDescriptorConfigurationPanel(AbstractCDKActivity activity) {
		this.activity = activity;
		this.configBean = this.activity.getConfiguration();
		this.initGUI();
	}

	@SuppressWarnings("unchecked")
	protected void initGUI() {
		try {
			JPanel selectionPanel = new JPanel(new GridLayout(1, 4));
			this.setLayout(new BorderLayout());
			this.setPreferredSize(new Dimension(1000, 600));
			String[] packages = new String[] { "org.openscience.cdk.applications.taverna.qsar.descriptors.atomic",
					"org.openscience.cdk.applications.taverna.qsar.descriptors.bond",
					"org.openscience.cdk.applications.taverna.qsar.descriptors.molecular",
					"org.openscience.cdk.applications.taverna.qsar.descriptors.atompair",
					"org.openscience.cdk.applications.taverna.qsar.descriptors.protein" };
			String[] titles = new String[] { "atomic", "bond", "molecular", "atompair", "protein" };
			JPanel[] panels = new JPanel[packages.length];
			JScrollPane[] scrollPanes = new JScrollPane[packages.length];
			int[] length = new int[packages.length];
			for (int i = 0; i < packages.length; i++) {
				panels[i] = new JPanel();
				scrollPanes[i] = new JScrollPane(panels[i]);
				TitledBorder border = new TitledBorder(new LineBorder(Color.BLACK), titles[i] + " - Package");
				scrollPanes[i].setBorder(border);
				scrollPanes[i].getViewport().setLayout(new FlowLayout());
				scrollPanes[i].setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				for (AbstractCDKActivity cdkActivity : cdkActivityRegistry.getInstances()) {
					if (cdkActivity.getClass().getName().startsWith(packages[i])) {
						length[i]++;
					}
				}
				selectionPanel.add(scrollPanes[i]);
			}
			this.selectionMap = new HashMap<Class<? extends AbstractCDKActivity>, JCheckBox>();
			;
			for (int i = 0; i < packages.length; i++) {
				panels[i].setLayout(new GridLayout(length[i], 1));
				for (AbstractCDKActivity cdkActivity : cdkActivityRegistry.getInstances()) {
					if (cdkActivity.getClass().getName().startsWith(packages[i])) {
						JCheckBox checkBox = new JCheckBox(cdkActivity.getClass().getSimpleName());
						panels[i].add(checkBox);
						this.selectionMap.put(cdkActivity.getClass(), checkBox);
					}
				}
			}
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
			buttonPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
			this.selectAllButton = new JButton("Select all");
			this.selectAllButton.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					QSARDescriptorConfigurationPanel.this.selectAll();
				}
			});
			this.clearButton = new JButton("Clear");
			this.clearButton.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					QSARDescriptorConfigurationPanel.this.unselectAll();
				}
			});
			buttonPanel.add(this.selectAllButton);
			buttonPanel.add(this.clearButton);
			this.add(selectionPanel, BorderLayout.CENTER);
			this.add(buttonPanel, BorderLayout.SOUTH);
			for (Entry<Class<? extends AbstractCDKActivity>, JCheckBox> entry : this.selectionMap.entrySet()) {
				JCheckBox checkBox = entry.getValue();
				checkBox.setSelected(false);
			}
			ArrayList<Class<? extends AbstractCDKActivity>> classes = (ArrayList<Class<? extends AbstractCDKActivity>>) this.configBean
					.getAdditionalProperty(CDKTavernaConstants.PROPERTY_CHOSEN_QSARDESCRIPTORS);
			if (classes != null) {
				for (Class<? extends AbstractCDKActivity> clazz : classes) {
					JCheckBox checkBox = this.selectionMap.get(clazz);
					checkBox.setSelected(true);
				}
			}
			this.repaint();
		} catch (Exception e) {
			e.printStackTrace();
			// TODO Exception handling
		}

	}
	
	private void selectAll() {
		for (Entry<Class<? extends AbstractCDKActivity>, JCheckBox> entry : this.selectionMap.entrySet()) {
			JCheckBox checkBox = entry.getValue();
			checkBox.setSelected(true);
		}
	}

	private void unselectAll() {
		for (Entry<Class<? extends AbstractCDKActivity>, JCheckBox> entry : this.selectionMap.entrySet()) {
			JCheckBox checkBox = entry.getValue();
			checkBox.setSelected(false);
		}
	}
	
	private ArrayList<Class<? extends AbstractCDKActivity>> getSelectedClasses() {
		ArrayList<Class<? extends AbstractCDKActivity>> classes = new ArrayList<Class<? extends AbstractCDKActivity>>();
		for (Entry<Class<? extends AbstractCDKActivity>, JCheckBox> entry : this.selectionMap.entrySet()) {
			Class<? extends AbstractCDKActivity> clazz = entry.getKey();
			JCheckBox checkBox = entry.getValue();
			if (checkBox.isSelected()) {
				classes.add(clazz);
			}
		}
		return classes;
	}

	@Override
	public boolean checkValues() {
		return !this.getSelectedClasses().isEmpty();
	}

	@Override
	public CDKActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}

	@Override
	public boolean isConfigurationChanged() {
		for (Entry<Class<? extends AbstractCDKActivity>, JCheckBox> entry : this.selectionMap.entrySet()) {
			Class<? extends AbstractCDKActivity> clazz = entry.getKey();
			JCheckBox checkBox = entry.getValue();
			if (checkBox.isSelected()) {
				if (!this.selectedClasses.contains(clazz)) {
					return true;
				}
			} else {
				if (this.selectedClasses.contains(clazz)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void noteConfiguration() {
		this.configBean = (CDKActivityConfigurationBean) this.cloneBean(this.configBean);
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_CHOSEN_QSARDESCRIPTORS, this.getSelectedClasses());
	}

	@Override
	public void refreshConfiguration() {
		this.selectedClasses = this.getSelectedClasses();
	}

}

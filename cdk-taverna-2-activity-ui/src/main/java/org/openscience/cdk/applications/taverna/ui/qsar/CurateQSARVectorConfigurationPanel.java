package org.openscience.cdk.applications.taverna.ui.qsar;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
import org.openscience.cdk.applications.taverna.qsar.utilities.QSARVectorUtility;

public class CurateQSARVectorConfigurationPanel extends
		ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> {

	private static final long serialVersionUID = -1892213335945799747L;
	private CurateQSARVectorView view = null;
	private AbstractCDKActivity activity = null;
	private CDKActivityConfigurationBean configBean = null;

	public CurateQSARVectorConfigurationPanel(AbstractCDKActivity activity) {
		this.activity = activity;
		this.configBean = this.activity.getConfiguration();
		this.initGUI();
	}

	private void initGUI() {
		this.view = new CurateQSARVectorView();
		this.add(view);
		this.refreshConfiguration();
	}

	@Override
	public boolean isConfigurationChanged() {
		int currentType = (Integer) this.configBean.getAdditionalProperty(CDKTavernaConstants.PROPERTY_QSAR_VECTOR_CURATION_TYPE);
		int type;
		if (this.view.getRdbtnCurateOnlyRows().isSelected()) {
			type = QSARVectorUtility.CURATE_ONLY_ROWS;
		} else if (this.view.getRdbtnDynamicCuration().isSelected()) {
			type = QSARVectorUtility.DYNAMIC_CURATION;
		} else {
			type = QSARVectorUtility.CURATE_ONLY_COLUMNS;
		}
		if (type != currentType) {
			return true;
		}
		return false;
	}

	@Override
	public CDKActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}

	@Override
	public void noteConfiguration() {
		this.configBean = (CDKActivityConfigurationBean) this.cloneBean(this.configBean);
		if (this.view.getRdbtnCurateOnlyRows().isSelected()) {
			this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_QSAR_VECTOR_CURATION_TYPE,
					QSARVectorUtility.CURATE_ONLY_ROWS);
		} else if (this.view.getRdbtnDynamicCuration().isSelected()) {
			this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_QSAR_VECTOR_CURATION_TYPE,
					QSARVectorUtility.DYNAMIC_CURATION);
		} else {
			this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_QSAR_VECTOR_CURATION_TYPE,
					QSARVectorUtility.CURATE_ONLY_COLUMNS);
		}
		boolean curateMinMax = this.view.getChckbxRemoveMinmaxValue().isSelected();
		this.configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_QSAR_VECTOR_MIN_MAX_CURATION, curateMinMax);
	}

	@Override
	public void refreshConfiguration() {
		int type = (Integer) this.configBean.getAdditionalProperty(CDKTavernaConstants.PROPERTY_QSAR_VECTOR_CURATION_TYPE);
		switch (type) {
		case QSARVectorUtility.CURATE_ONLY_ROWS:
			this.view.getRdbtnCurateOnlyRows().setSelected(true);
			break;
		case QSARVectorUtility.DYNAMIC_CURATION:
			this.view.getRdbtnDynamicCuration().setSelected(true);
			break;
		default:
			this.view.getRdbtnCurateOnlyColumns().setSelected(true);
			break;
		}
		boolean curateMinMax = (Boolean) this.configBean
				.getAdditionalProperty(CDKTavernaConstants.PROPERTY_QSAR_VECTOR_MIN_MAX_CURATION);
		this.view.getChckbxRemoveMinmaxValue().setSelected(curateMinMax);
	}

	@Override
	public boolean checkValues() {
		return true;
	}

}

package org.openscience.cdk.applications.taverna.ui.view;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
import org.openscience.cdk.applications.taverna.clustering.art2a.ART2aClustererActivity;
import org.openscience.cdk.applications.taverna.curation.MoleculeConnectivityCheckerActivity;
import org.openscience.cdk.applications.taverna.interfaces.IIterativeFileWriter;
import org.openscience.cdk.applications.taverna.interfaces.IPortNumber;
import org.openscience.cdk.applications.taverna.jchempaint.JChemPaintActivity;
import org.openscience.cdk.applications.taverna.qsar.CurateQSARVectorActivity;
import org.openscience.cdk.applications.taverna.qsar.QSARDescriptorActivity;
import org.openscience.cdk.applications.taverna.qsar.QSARDescriptorThreadedActivity;
import org.openscience.cdk.applications.taverna.reactionenumerator.ReactionEnumeratorActivity;
import org.openscience.cdk.applications.taverna.signaturescoring.AtomSignatureActivity;
import org.openscience.cdk.applications.taverna.ui.EmptyConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.art2a.ART2aClassificationConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.curation.AtomSignatureConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.curation.CuratorConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.io.IterativeFileWriterConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.jchempaint.JChemPaintConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.miscellaneous.PortNumberConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.qsar.CurateQSARVectorConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.qsar.QSARDescriptorConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.reactionenumerator.ReactionEnumeratorConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.weka.AttributeEvaluationConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.weka.GACAttributeEvaluationConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.weka.GARAttributeEvaluationConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.weka.ScatternPlotConfigurationPanel;
import org.openscience.cdk.applications.taverna.ui.weka.SplitClassificationTrainTestsetConfigurationPanelController;
import org.openscience.cdk.applications.taverna.ui.weka.SplitRegressionTrainTestsetConfigurationPanelController;
import org.openscience.cdk.applications.taverna.ui.weka.WekaClassificationConfigurationPanelController;
import org.openscience.cdk.applications.taverna.ui.weka.WekaClusteringConfigurationPanelController;
import org.openscience.cdk.applications.taverna.ui.weka.WekaRegressionConfigurationPanelController;
import org.openscience.cdk.applications.taverna.weka.classification.EvaluateClassificationResultsAsPDFActivity;
import org.openscience.cdk.applications.taverna.weka.classification.GACAttributeSelectionActivity;
import org.openscience.cdk.applications.taverna.weka.classification.SplitClassificationTrainTestsetActivity;
import org.openscience.cdk.applications.taverna.weka.classification.WekaClassificationActivity;
import org.openscience.cdk.applications.taverna.weka.clustering.WekaClusteringActivity;
import org.openscience.cdk.applications.taverna.weka.regression.LeaveOneOutRAttributeSelectionActivity;
import org.openscience.cdk.applications.taverna.weka.regression.GARAttributeSelectionActivity;
import org.openscience.cdk.applications.taverna.weka.regression.EvaluateRegressionResultsAsPDFActivity;
import org.openscience.cdk.applications.taverna.weka.regression.SplitRegressionTrainTestsetActivity;
import org.openscience.cdk.applications.taverna.weka.regression.WekaRegressionActivity;

public class CDKConfigurationPanelFactory {

	public static ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> getConfigurationPanel(
			AbstractCDKActivity activity) {
		if (activity instanceof IIterativeFileWriter) {
			return new IterativeFileWriterConfigurationPanel(activity);
		} else if (activity instanceof IPortNumber) {
			return new PortNumberConfigurationPanel(activity);
		} else if (activity instanceof JChemPaintActivity) {
			return new JChemPaintConfigurationPanel(activity);
		} else if (activity instanceof AtomSignatureActivity) {
			return new AtomSignatureConfigurationPanel(activity);
		} else if (activity instanceof QSARDescriptorActivity || activity instanceof QSARDescriptorThreadedActivity) {
			return new QSARDescriptorConfigurationPanel(activity);
		} else if (activity instanceof MoleculeConnectivityCheckerActivity) {
			return new CuratorConfigurationPanel(activity);
		} else if (activity instanceof ReactionEnumeratorActivity) {
			return new ReactionEnumeratorConfigurationPanel(activity);
		} else if (activity instanceof ART2aClustererActivity) {
			return new ART2aClassificationConfigurationPanel(activity);
		} else if (activity instanceof CurateQSARVectorActivity) {
			return new CurateQSARVectorConfigurationPanel(activity);
		} else if (activity instanceof WekaClusteringActivity) {
			return new WekaClusteringConfigurationPanelController(activity);
		} else if (activity instanceof WekaRegressionActivity) {
			return new WekaRegressionConfigurationPanelController(activity);
		} else if (activity instanceof SplitRegressionTrainTestsetActivity) {
			return new SplitRegressionTrainTestsetConfigurationPanelController(activity);
		} else if (activity instanceof EvaluateRegressionResultsAsPDFActivity
				|| activity instanceof EvaluateClassificationResultsAsPDFActivity) {
			return new ScatternPlotConfigurationPanel(activity);
		} else if (activity instanceof GARAttributeSelectionActivity) {
			return new GARAttributeEvaluationConfigurationPanel(activity);
		} else if (activity instanceof LeaveOneOutRAttributeSelectionActivity) {
			return new AttributeEvaluationConfigurationPanel(activity);
		} else if (activity instanceof SplitClassificationTrainTestsetActivity) {
			return new SplitClassificationTrainTestsetConfigurationPanelController(activity);
		} else if (activity instanceof GACAttributeSelectionActivity) {
			return new GACAttributeEvaluationConfigurationPanel(activity);
		}else if (activity instanceof WekaClassificationActivity) {
			return new WekaClassificationConfigurationPanelController(activity);
		}
		return new EmptyConfigurationPanel();
	}

}

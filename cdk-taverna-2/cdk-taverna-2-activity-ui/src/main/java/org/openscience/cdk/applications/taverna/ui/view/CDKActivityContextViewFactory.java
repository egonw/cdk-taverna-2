package org.openscience.cdk.applications.taverna.ui.view;

import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;

import org.openscience.cdk.applications.taverna.AbstractCDKActivity;

public class CDKActivityContextViewFactory implements ContextualViewFactory<AbstractCDKActivity> {

	public boolean canHandle(Object selection) {
		return selection instanceof AbstractCDKActivity;
	}

	public List<ContextualView> getViews(AbstractCDKActivity selection) {
		return Arrays.<ContextualView> asList(new CDKContextualView(selection));
	}

}

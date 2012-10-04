package com.ceteva.diagram.action;

import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.actions.ActionFactory;

public class WorkbenchContribution extends org.eclipse.gef.ui.actions.ActionBarContributor {

  protected void buildActions() {
	addRetargetAction(new PrintRetargetAction());
  }

  protected void declareGlobalActionKeys() {
	addGlobalActionKey(ActionFactory.PRINT.getId());
  }

  public void contributeToToolBar(IToolBarManager tbm) {
	tbm.add(new ZoomComboContributionItem(getPage()));
  }
  
}

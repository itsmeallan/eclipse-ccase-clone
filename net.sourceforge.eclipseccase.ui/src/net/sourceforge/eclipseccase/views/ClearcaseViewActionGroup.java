/*
 * Copyright (c) 2004 Intershop (www.intershop.de) Created on Apr 8, 2004
 */

package net.sourceforge.eclipseccase.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.views.navigator.MainActionGroup;

/**
 * TODO Provide description for ClearcaseViewActionGroup.
 * 
 * @author Gunnar Wagenknecht (g.wagenknecht@intershop.de)
 */
public class ClearcaseViewActionGroup extends MainActionGroup
{

    /**
     * Creates a new instance.
     * 
     * @param navigator
     */
    public ClearcaseViewActionGroup(ClearcaseViewPart navigator)
    {
        super(navigator);
    }
    
    protected Action refreshAction;
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.views.navigator.MainActionGroup#makeActions()
     */
    protected void makeActions()
    {
        super.makeActions();

        refreshAction = new Action("&Refresh View", TeamImages.getImageDescriptor(ISharedImages.IMG_REFRESH))
        {
            public void run()
            {
                getClearcaseView().refresh();
            }

        };
        refreshAction.setToolTipText("Refreshes the view");
		refreshAction.setDisabledImageDescriptor(TeamImages.getImageDescriptor(ISharedImages.IMG_REFRESH_DISABLED));
		refreshAction.setHoverImageDescriptor(TeamImages.getImageDescriptor(ISharedImages.IMG_REFRESH_ENABLED));
    	}
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.views.navigator.MainActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
     */
    public void fillActionBars(IActionBars actionBars)
    {
        super.fillActionBars(actionBars);

		actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);

		IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(new Separator());
		toolBar.add(refreshAction);		
    }

    /**
     * @return
     */
    protected ClearcaseViewPart getClearcaseView()
    {
        return ((ClearcaseViewPart) getNavigator());
    }
}
package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.ui.console.ConsoleOperationListener;

import org.eclipse.ui.PlatformUI;

import net.sourceforge.eclipseccase.views.ConfigSpecView;

import org.eclipse.core.runtime.IProgressMonitor;

import net.sourceforge.clearcase.events.OperationListener;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseInterface;

import net.sourceforge.eclipseccase.ClearcaseProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionDelegate;

/**
 * Pulls up the clearcase version tree for the element
 */
public class ExternalUpdateAction extends ClearcaseWorkspaceAction{

	private IResource [] resources =null;
	private ConfigSpecView view = null;
	
	public boolean isEnabled() 
	{
		boolean bRes = true;

		IResource[] resources = getSelectedResources();
		if (resources.length != 0)
		{
			for (int i = 0; (i < resources.length) && (bRes); i++)
			{
				IResource resource = resources[i];
				ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);
				if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource) || !provider.hasRemote(resource))
					bRes = false;
			}
		}
		else
		{
			bRes = false;
		}

		return bRes;
	}

    /**
     * @see IActionDelegate#run(IAction)
     */
    public void execute(IAction action) {
        resources = getSelectedResources();
        
        try {
			view = (ConfigSpecView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("net.sourceforge.eclipseccase.views.ConfigSpecView");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                try {

                    if(resources != null && resources.length > 0)
                    {
                        beginTask(monitor, "Updating...", resources.length);
                        
                    	ClearCaseInterface cci = ClearCase.createInterface(ClearCase.INTERFACE_CLI);
                    	String viewName = cci.getViewName(resources[0].getLocation().toOSString());
                    	cci.setViewConfigSpec(viewName, "-current", new ConsoleOperationListener(monitor));
                    	
        				resources[0].getProject().refreshLocal(IResource.DEPTH_INFINITE, subMonitor(monitor));
                    }
                } finally {
                    monitor.done();
                }
            }
        };
        executeInBackground(runnable, "Updating resources from ClearCase");
    }


}
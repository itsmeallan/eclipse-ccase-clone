package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.ClearcaseProvider;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;

public class RefreshStateAction extends ClearcaseWorkspaceAction
{
	/*
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action)
	{
		IWorkspaceRunnable runnable = new IWorkspaceRunnable()
        {
            public void run(IProgressMonitor monitor) throws CoreException
            {
				try
				{
					IResource[] resources = getSelectedResources();
                    beginTask(monitor, "Refreshing state...", resources.length);
					for (int i = 0; i < resources.length; i++)
					{
						IResource resource = resources[i];
                        checkCanceled(monitor);
						ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);
						provider.refresh(new IResource[] {resource},
											IResource.DEPTH_INFINITE, subMonitor(monitor));
					}
				}
				finally
				{
					monitor.done();
				}
			}
        };
        
        executeInBackground(runnable, "Refreshing state");
	}

	protected boolean isEnabled()
	{
		IResource[] resources = getSelectedResources();
		if (resources.length == 0)
			return false;
		for (int i = 0; i < resources.length; i++)
		{
			IResource resource = resources[i];
			ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);
			if (provider == null)
				return false;
		}
		return true;
	}

}
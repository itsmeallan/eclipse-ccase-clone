package net.sourceforge.eclipseccase.ui.actions;

import java.io.IOException;

import net.sourceforge.clearcase.simple.ClearcaseUtil;
import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.ClearcaseProvider;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.IActionDelegate;

/**
 * Pulls up the clearcase version tree for the element
 */
public class ExternalUpdateAction extends ClearcaseWorkspaceAction {

    /**
     * @see TeamAction#isEnabled()
     */
    protected boolean isEnabled() throws TeamException {
        IResource[] resources = getSelectedResources();
        if (resources.length == 0) return false;
        for (int i = 0; i < resources.length; i++) {
            IResource resource = resources[i];
            ClearcaseProvider provider = ClearcaseProvider
                    .getClearcaseProvider(resource);
            if (provider == null || provider.isUnknownState(resource)
                    || provider.isIgnored(resource)
                    || !provider.hasRemote(resource)) return false;
            if (!provider.isSnapShot(resource)) return false;
        }
        return true;
    }

    /**
     * @see IActionDelegate#run(IAction)
     */
    public void run(IAction action) {
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

            public void run(IProgressMonitor monitor) throws CoreException {
                try {
                    IResource[] resources = getSelectedResources();
                    beginTask(monitor, "Updating...", resources.length);
                    for (int i = 0; i < resources.length; i++) {
                        IResource resource = resources[i];

                        if(ClearcasePlugin.isUseCleartool())
                        {
                            // update using cleartool
                            ClearcasePlugin.getEngine().cleartool(
                                    "update -graphical "
                                            + ClearcaseUtil.quote(resource.getLocation().toOSString()));
                        }
                        else
                        {
                            try {
                                Process process = Runtime.getRuntime().exec(new String[] {"clearviewupdate", "-pname", resource.getLocation().toOSString()});
                                process.waitFor();
                            } catch (IOException e) {
                                throw new TeamException("Could not execute: clearviewupdate: " + e.getMessage(), e);
                            } catch (InterruptedException e) {
                                throw new OperationCanceledException();
                            }
                        }
                        
                        // refresh resources: will refresh state if necessary
                        resource.refreshLocal(IResource.DEPTH_INFINITE, subMonitor(monitor));
                    }
                } finally {
                    monitor.done();
                }
            }
        };
        executeInBackground(runnable, "Updating resources from ClearCase");
    }

}
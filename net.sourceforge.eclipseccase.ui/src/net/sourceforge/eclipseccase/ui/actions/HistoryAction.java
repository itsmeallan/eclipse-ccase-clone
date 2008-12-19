package net.sourceforge.eclipseccase.ui.actions;

import java.io.IOException;
import net.sourceforge.clearcase.commandline.CleartoolCommandLine;
import net.sourceforge.clearcase.commandline.CommandLauncher;
import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.ClearcaseProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionDelegate;

/**
 *  Pulls up the clearcase history
 */
public class HistoryAction extends ClearcaseWorkspaceAction
{

    /**
     * {@inheritDoc
     */
    public boolean isEnabled() 
    {
        IResource[] resources = getSelectedResources();
        if (resources.length == 0)
            return false;
        for (int i = 0; i < resources.length; i++)
        {
            IResource resource = resources[i];
            ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);
            if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource) || !provider.hasRemote(resource))
                return false;
        }
        return true;
    }

    /**
     * @see IActionDelegate#run(IAction)
     */
    public void execute(IAction action)
    {IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
		public void run(IProgressMonitor monitor) throws CoreException {
			try {
				 IResource[] resources = getSelectedResources();
                 for (int i = 0; i < resources.length; i++)
                 {
                     IResource resource = resources[i];
                     String path = resource.getLocation().toOSString();
                     if (ClearcasePlugin.isUseCleartool())
                     {
                         new CommandLauncher().execute(new CleartoolCommandLine("lshistory").addOption("-graphical").addElement(path).create(),null,null,null);
                     }
                     else
                     {
                         Runtime.getRuntime().exec(new String[] {"clearhistory", resource.getLocation().toOSString()});
                     }
                 }
			} catch (IOException ex) {

			} finally {
				monitor.done();
			}
		}
	};

	executeInBackground(runnable, "History");
	
        
    }

}
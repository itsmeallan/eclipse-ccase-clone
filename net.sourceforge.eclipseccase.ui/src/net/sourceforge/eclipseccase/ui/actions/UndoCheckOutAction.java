package net.sourceforge.eclipseccase.ui.actions;

import java.util.*;
import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.ClearcaseProvider;
import net.sourceforge.eclipseccase.ui.DirectoryLastComparator;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;

/**
 * @author conwaym To change this generated comment edit the template variable
 *         "typecomment": Workbench>Preferences>Java>Templates.
 */
public class UndoCheckOutAction extends ClearcaseWorkspaceAction {

    public void execute(IAction action) {
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

            public void run(IProgressMonitor monitor) throws CoreException {
                try {
                    IResource[] resources = getSelectedResources();
                    beginTask(monitor, "Undoing checkout...", resources.length);

                    if (ClearcasePlugin.isUseClearDlg()) {
                        monitor
                                .subTask("Executing ClearCase user interface...");
                        ClearDlgHelper.uncheckout(resources);
                    } else {
                        // Sort resources with directories last so that the
                        // modification of a
                        // directory doesn't abort the modification of files
                        // within
                        // it.
                        List resList = Arrays.asList(resources);
                        Collections
                                .sort(resList, new DirectoryLastComparator());

                        for (int i = 0; i < resources.length; i++) {
                            IResource resource = resources[i];
                            ClearcaseProvider provider = ClearcaseProvider
                                    .getClearcaseProvider(resource);
                            provider.uncheckout(new IResource[] { resource },
                                    IResource.DEPTH_ZERO, subMonitor(monitor));
                        }
                    }
                } finally {
                    monitor.done();
                }
            }
        };

        executeInBackground(runnable, "Uncheckout resources from ClearCase");
    }

    public boolean isEnabled() {
        IResource[] resources = getSelectedResources();
        if (resources.length == 0) return false;
        for (int i = 0; i < resources.length; i++) {
            IResource resource = resources[i];
            ClearcaseProvider provider = ClearcaseProvider
                    .getClearcaseProvider(resource);
            if (provider == null || provider.isUnknownState(resource)
                    || provider.isIgnored(resource)
                    || !provider.hasRemote(resource)) return false;
            if (!provider.isCheckedOut(resource)) return false;
        }
        return true;
    }

}
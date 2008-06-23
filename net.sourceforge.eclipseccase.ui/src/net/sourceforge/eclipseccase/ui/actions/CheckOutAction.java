package net.sourceforge.eclipseccase.ui.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.ClearcaseProvider;
import net.sourceforge.eclipseccase.ui.CommentDialog;
import net.sourceforge.eclipseccase.ui.DirectoryLastComparator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.team.core.TeamException;

public class CheckOutAction extends ClearcaseWorkspaceAction {

    public void execute(IAction action) {
        String maybeComment = "";
        int maybeDepth = IResource.DEPTH_ZERO;

        if (!ClearcasePlugin.isUseClearDlg()
                && ClearcasePlugin.isCommentCheckout()) {
            CommentDialog dlg = new CommentDialog(getShell(),
                    "Checkout comment");
            if (dlg.open() == Window.CANCEL) return;
            maybeComment = dlg.getComment();
            maybeDepth = dlg.isRecursive() ? IResource.DEPTH_INFINITE
                    : IResource.DEPTH_ZERO;
        }
        if(!ClearcasePlugin.isCheckoutAutoAlways()){
        	
        IResource[] resources = getSelectedResources();
        MessageDialog checkoutQuestion = new MessageDialog(getShell(), "Checkout",
				null, "Do you really want to checkout?", MessageDialog.QUESTION, new
				String[] {"Yes", "No", "Cancel" }, 0);
				int returncode = checkoutQuestion.open();
				if (returncode == 1) {
				//No option
				StringBuffer message = new StringBuffer("No auto checkout performed for the following resources:\n");
				for (int i = 0; i < resources.length; i++) {
					IResource resource = resources[i];
					message.append("\n\t" + resource.getFullPath());
				}
				//TODO: No status here??? Ref to ClearcaseModificationHandler.java.
				//return new Status(IStatus.ERROR, ClearcaseProvider.ID,
				//TeamException.NOT_CHECKED_OUT, message.toString(), null);
				return;
				} else if(returncode == 2) {
				//Cancel option.
				return;
				}
        }
    

        final String comment = maybeComment;
        final int depth = maybeDepth;

        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

            public void run(IProgressMonitor monitor) throws CoreException {

                try {
                    IResource[] resources = getSelectedResources();
                    beginTask(monitor, "Checking out...", resources.length);

                    if (ClearcasePlugin.isUseClearDlg()) {
                        monitor
                                .subTask("Executing ClearCase user interface...");
                        ClearDlgHelper.checkout(resources);
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
                            // fix for 1046462 
                            // TODO: investigate null here 
                            if (null != provider) {
                                provider.setComment(comment);
                                provider.checkout(new IResource[] { resource },
                                        depth, subMonitor(monitor));
                            }
                        }
                    }
                } finally {
                    monitor.done();
                }
            }
        };
        executeInBackground(runnable, "Checking out resources from ClearCase");
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
            if (provider.isCheckedOut(resource)) return false;
        }
        return true;
    }

}
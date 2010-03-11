package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.ClearDlgHelper;

import net.sourceforge.eclipseccase.ClearCasePlugin;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.ui.CommentDialog;
import net.sourceforge.eclipseccase.ui.console.ConsoleOperationListener;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;

public class AddToClearCaseAction extends ClearCaseWorkspaceAction {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void execute(IAction action) {
		String maybeComment = "";
		int maybeDepth = IResource.DEPTH_ZERO;

		if (!ClearCasePlugin.isUseClearDlg() && ClearCasePlugin.isCommentAdd()) {
			CommentDialog dlg = new CommentDialog(getShell(), "Add to ClearCase comment");
			if (dlg.open() == Window.CANCEL)
				return;
			maybeComment = dlg.getComment();
			maybeDepth = dlg.isRecursive() ? IResource.DEPTH_INFINITE : IResource.DEPTH_ZERO;
		}

		final String comment = maybeComment;
		final int depth = maybeDepth;

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					IResource[] resources = getSelectedResources();
					beginTask(monitor, "Adding...", resources.length);
					ConsoleOperationListener opListener = new ConsoleOperationListener(monitor);
					if (ClearCasePlugin.isUseClearDlg()) {
						monitor.subTask("Executing ClearCase user interface...");
						ClearDlgHelper.add(resources);
					} else {
						ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resources[0]);
						provider.setComment(comment);
						provider.setOperationListener(opListener);
						provider.add(resources, depth, subMonitor(monitor));
					}
				} finally {
					monitor.done();
				}
			}

		};

		executeInBackground(runnable, "Adding resources to ClearCase");
	}

	private static final String DEBUG_ID = "AddToClearCaseAction";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		IResource[] resources = getSelectedResources();
		if (resources.length == 0)
			return false;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
			if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource))
				return false;

			// Projects may be the view directory containing the VOBS, if so,
			// don't want to be able to add em, or any resource diretcly under
			// them
			if (resource.getType() == IResource.PROJECT && !provider.isClearCaseElement(resource)) {
				ClearCasePlugin.debug(DEBUG_ID, "disabled for project outside CC: " + resource);
				return false;
			}
			if (resource.getParent().getType() == IResource.PROJECT && !provider.isClearCaseElement(resource.getParent())) {
				ClearCasePlugin.debug(DEBUG_ID, "disabled for " + resource + " because parent is project outside CC: " + resource.getParent());
				return false;
			}
			if (provider.isClearCaseElement(resource)) {
				ClearCasePlugin.debug(DEBUG_ID, "disabled for " + resource + " because it already is CC element");
				return false;
			}
		}
		return true;
	}

}
package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.ClearCasePlugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import net.sourceforge.clearcase.commandline.CleartoolCommandLine;
import net.sourceforge.clearcase.commandline.CommandLauncher;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.ui.CommentDialog;
import net.sourceforge.eclipseccase.ui.console.ConsoleOperationListener;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

public class MkBrTypeAction extends ClearCaseWorkspaceAction {
	private IResource[] resources = null;

	private String branchName = "";

	private String branchComment = "";

	/**
	 * {@inheritDoc
	 */
	@Override
	public boolean isEnabled() {
		boolean bRes = true;

		IResource[] resources = getSelectedResources();
		if (resources.length != 0) {
			for (int i = 0; (i < resources.length) && (bRes); i++) {
				IResource resource = resources[i];
				ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
				if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource) || !provider.isClearCaseElement(resource)) {
					bRes = false;
				}
			}
		} else {
			bRes = false;
		}

		return bRes;

	}

	@Override
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		resources = getSelectedResources();

		InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), "Create a branch", "Branch name:", ClearCasePlugin.getBranchPrefix(), null);
		if (dlg.open() == Window.OK) {
			// User clicked OK; update the label with the input
			// System.out.println(dlg.getValue());
			branchName = dlg.getValue();
		} else
			return;

		
		branchComment = branchName.replaceAll(ClearCasePlugin.getBranchPrefix(), "") +  ": ";
		CommentDialog commentDlg = new CommentDialog(getShell(), "Make Branch comment", branchComment);
		if (commentDlg.open() == Window.CANCEL)
			return;

		branchComment = commentDlg.getComment();

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {

				try {
					if (resources != null && resources.length != 0) {
						IResource resource = resources[0];
						File workingDir = null;
						if (resource.getType() == IResource.FOLDER) {
							workingDir = new File(resource.getLocation().toOSString());
						} else {
							workingDir = new File(resource.getLocation().toOSString()).getParentFile();
						}

						new CommandLauncher().execute(new CleartoolCommandLine("mkbrtype").addOption("-c").addElement(branchComment).addElement(branchName).create(), workingDir, null, new ConsoleOperationListener(monitor));
					}
				} catch (Exception e) {
				} finally {
					monitor.done();
				}
			}
		};

		executeInBackground(runnable, "Make Branch Type");
	}
}

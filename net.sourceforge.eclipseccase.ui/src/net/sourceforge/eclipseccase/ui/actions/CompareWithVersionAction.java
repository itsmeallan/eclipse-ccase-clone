package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.ui.console.ConsoleOperationListener;

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
 * Pulls up the compare with predecessor dialog.
 */
public class CompareWithVersionAction extends ClearcaseWorkspaceAction {

	private IResource element = null;
	private String versionA = null;
	private String versionB = null;



	public void setElement(IResource element) {
		this.element = element;
	}

	public void setVersionA(String versionA) {
		this.versionA = versionA;
	}

	public void setVersionB(String versionB) {
		this.versionB = versionB;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEnabled()  
	{
		if (element != null)
		{
			ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(element);
			if (provider != null && !provider.isUnknownState(element) && 
					!provider.isIgnored(element) && provider.hasRemote(element))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void execute(IAction action) {

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					String path = element.getLocation().toOSString();
					
					String v1 = path;
					if(versionA != null)
					{
						v1 = v1 + "@@" + versionA;
					}

					String v2 = path;
					if(versionB != null)
					{
						v2 = v2 + "@@" + versionB;
					}

                	ConsoleOperationListener opListener = new ConsoleOperationListener(monitor);
					if (ClearcasePlugin.isUseCleartool()) {
						new CommandLauncher().execute(new CleartoolCommandLine("diff").addOption("-graphical").addElement(v1).addElement(v2).create(), null, null, opListener);
					} 
				} catch (Exception ex) {

				} finally {
					monitor.done();
				}
			}
		};
		executeInBackground(runnable, "Compare With History");

	}

}
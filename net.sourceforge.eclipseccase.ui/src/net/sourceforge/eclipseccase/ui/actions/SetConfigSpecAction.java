package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.ui.console.ClearCaseConsole;
import net.sourceforge.eclipseccase.ui.console.ClearCaseConsoleFactory;

import net.sourceforge.eclipseccase.ui.console.ConsoleOperationListener;

import net.sourceforge.eclipseccase.views.ConfigSpecView;

import java.io.File;
import java.io.FileWriter;
import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseInterface;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import net.sourceforge.eclipseccase.ClearcaseProvider;
import org.eclipse.core.resources.IResource;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.jface.action.IAction;

/**
 * @author fbelouin
 *
 */
public class SetConfigSpecAction extends ClearcaseWorkspaceAction {
	private IResource resource = null;
	private String configSpecTxt = null;
	
	/**
	 * {@inheritDoc
	 */
	public boolean isEnabled() 
	{
		boolean bRes = true;

		if (resource != null)
		{
			ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);
			if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource) || !provider.hasRemote(resource))
				bRes = false;
		}
		else
		{
			bRes = false;
		}

		return bRes;

	}
	
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {

				monitor.done();
				if(resource != null)
				{
					try {
						File f = new File("/tmp/configSpec" + Integer.toString(this.hashCode()) + ".tmp");
						if (f.exists()) {
							f.delete();
						}
						FileWriter writer = new FileWriter(f);
						writer.write(configSpecTxt, 0, configSpecTxt.length());
						writer.close();

						ClearCaseInterface cci = ClearCase.createInterface(ClearCase.INTERFACE_CLI);
						String viewName = cci.getViewName(resource.getLocation().toOSString());
						cci.setViewConfigSpec(viewName, f.getPath(), new ConsoleOperationListener(monitor));
						f.delete();

						if (resource != null) {
							resource.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
						}
					} catch (Exception e) {
						ClearCaseConsole console = ClearCaseConsoleFactory.getClearCaseConsole();
						console.err.println("A Problem occurs while updating Config Spec.\n" + e.getMessage());
						console.show();
					}
				}
			}
		};

		executeInForeground(runnable, PROGRESS_DIALOG, "Set Config Spec...");
	}

	public void setResource(IResource resource) {
		this.resource = resource;
	}
	
	public void setConfigSpecTxt(String configSpecTxt) {
		this.configSpecTxt = configSpecTxt;
	}
}
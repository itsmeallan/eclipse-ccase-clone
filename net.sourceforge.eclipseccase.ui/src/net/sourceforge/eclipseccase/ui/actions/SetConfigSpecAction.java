package net.sourceforge.eclipseccase.ui.actions;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseInterface;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.ui.console.ClearCaseConsole;
import net.sourceforge.eclipseccase.ui.console.ClearCaseConsoleFactory;
import net.sourceforge.eclipseccase.views.ConfigSpecView;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.IAction;

/**
 * @author fbelouin
 * 
 */
public class SetConfigSpecAction extends ClearCaseWorkspaceAction {
	private IResource resource = null;

	private String configSpecTxt = null;

	private ConfigSpecView view = null;

	/**
	 * {@inheritDoc

	 */
	@Override
	public boolean isEnabled() {
		boolean bRes = true;

		if (resource != null) {
			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
			if (provider == null) {
				bRes = false;
			}
		} else {
			bRes = false;
		}

		return bRes;

	}

	@Override
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				ClearCaseConsole console = ClearCaseConsoleFactory.getClearCaseConsole();
				if (resource != null) {
					int scale = 10;
					ClearCaseInterface cci = ClearCase.createInterface(ClearCase.INTERFACE_CLI_SP);
					String viewName = cci.getViewName(resource.getLocation().toOSString());

					try {
						String userDir = System.getProperty("user.home");
						File f = new File(userDir + File.separator + "configSpec" + Integer.toString(this.hashCode()) + ".tmp");
						if (f.exists()) {
							if(!f.delete()){
								console.err.println("A Problem occured when trying to delete tmp cs file.\n");
								console.show();
							}
						}
						FileWriter writer = new FileWriter(f);
						writer.write(configSpecTxt, 0, configSpecTxt.length());
						writer.close();

						if (viewName.length() > 0) {
							cci.setViewConfigSpec(viewName, f.getPath(), resource.getProject().getLocation().toOSString(), null);
							
						}
						monitor.beginTask("Refreshing workspace ...", 1 * scale);
						resource.getProject().refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 1 * scale));

					} catch (Exception e) {
						console.err.println("A Problem occurs while updating Config Spec.\n" + e.getMessage());
						console.show();

					} finally {
						monitor.done();
						if (view != null) {
							view.focusOnConfigSpec();
						}

					}
				}
			}
		};

		executeInBackground(runnable, "Set Config Spec");

	}

	public void setResource(IResource resource) {
		this.resource = resource;
	}

	public void setConfigSpecTxt(String configSpecTxt) {
		this.configSpecTxt = configSpecTxt;
	}

	public void setConfigSpecView(ConfigSpecView view) {
		this.view = view;
	}
}

package net.sourceforge.eclipseccase.ui.actions;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Display;

import net.sourceforge.eclipseccase.ui.preferences.DiffMergePreferencePage;

import net.sourceforge.eclipseccase.ClearCasePreferences;

import net.sourceforge.eclipseccase.ui.compare.PredecessorCompareInput;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IFile;

import net.sourceforge.eclipseccase.ClearCasePlugin;

import net.sourceforge.eclipseccase.ClearCaseProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionDelegate;

/**
 * Pulls up the compare with predecessor dialog.
 */
public class CompareWithPredecessorAction extends ClearCaseWorkspaceAction {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnabled() {
		IResource[] resources = getSelectedResources();
		if (resources.length == 0)
			return false;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
			if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource) || !provider.isClearCaseElement(resource))
				return false;
		}
		return true;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	@Override
	public void execute(IAction action) {
		
		if(ClearCasePreferences.isCompareExternal()){
			String tool = ClearCasePreferences.getExtDiffTool();
			String path = ClearCasePreferences.getExtDiffExecPath();
			String [] prefs = new String [] {tool,path};
			if(!validatePreferences(prefs)) return;
			
			
			if(tool.equals(DiffMergePreferencePage.TOOL_KDIFF)){
				
			}else if(tool.equals(DiffMergePreferencePage.TOOL_IBM)){
				
			}
			
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					try {
						IResource[] resources = getSelectedResources();
						for (int i = 0; i < resources.length; i++) {
							IResource resource = resources[i];
							String cur = resource.getLocation().toOSString();
							ClearCaseProvider p = ClearCaseProvider.getClearCaseProvider(resource);
							if (p != null) {
								String prev = p.getPredecessorVersion(resource);
								//FIXME: handle operation.
//								DiffOperation oper = new DiffOperation();
//								oper.diff(cur,prev);
								
							}
							
							
						}
					} finally {
						monitor.done();
					}
				}
			};
			executeInBackground(runnable, "External Compare With Predecessor");
		}

		if (ClearCasePreferences.isCompareExternal()) {
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					try {
						IResource[] resources = getSelectedResources();
						for (int i = 0; i < resources.length; i++) {
							IResource resource = resources[i];
							String path = resource.getLocation().toOSString();
							ClearCaseProvider p = ClearCaseProvider.getClearCaseProvider(resource);
							if (p != null) {
								p.compareWithPredecessor(path);
							}
						}
					} finally {
						monitor.done();
					}

				}
			};
			executeInBackground(runnable, "Compare With Predecessor");

		} else {
			// 20101124 mike use new internal compare.
			IResource[] resources = getSelectedResources();

			CompareConfiguration config = new CompareConfiguration();
			config.setLeftEditable(false);
			config.setRightEditable(false); // Could be made editable in the
			// future.

			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				ClearCaseProvider p = ClearCaseProvider.getClearCaseProvider(resource);

				if (p != null && resource instanceof IFile) {
					PredecessorCompareInput input = new PredecessorCompareInput(config, (IFile) resource, p);
					CompareUI.openCompareEditor(input);
				}
			}
		}
	}
	
	private boolean validatePreferences(String [] prefs){
		boolean result = true;
			for (int i = 0; i < prefs.length; i++) {
			String pref = prefs[i];
			if(pref.isEmpty()) result = false;MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", "Preference: "+pref+" is not set!");
		}
			return result;
	}
	
	class Tool {
		
	}
	
}
package net.sourceforge.eclipseccase.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.ClearcaseProvider;
import net.sourceforge.eclipseccase.ui.ClearcaseUI;
import net.sourceforge.eclipseccase.ui.ConfirmSaveModifiedResourcesDialog;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

abstract public class ClearcaseAction extends TeamAction implements
		IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	private static Map actions = new HashMap();

	public ClearcaseAction() {
		super();
	}

	public void init(IWorkbenchWindow workbenchWindow) {
		this.window = workbenchWindow;
	}

	public void dispose() {
		deregisterAction();
		super.dispose();
	}

	// Lets us use the same actions for context menus/actionSet+keybindings -
	// i.e. if we
	// are in an active editor, that is the active selection instead of whats
	// selected in
	// the tree view
	protected IResource[] getSelectedResources() {
		IResource[] result = null;
		IWorkbenchPart part = null;
		if (getWindow() != null)
			part = getWindow().getActivePage().getActivePart();
		if (part != null && part instanceof IEditorPart) {
			IEditorPart editor = (IEditorPart) part;
			IEditorInput input = editor.getEditorInput();
			IResource edited = (IFile) input.getAdapter(IFile.class);
			if (edited != null) {
				result = new IResource[] { edited };
			}
		}

		if (null != result)
			return result;

		result = super.getSelectedResources();

		if (result == null)
			result = new IResource[0];

		return result;
	}

	public IWorkbenchWindow getWindow() {
		return window;
	}

	private void registerAction(IAction action) {
		if (action != null) {
			synchronized (actions) {
				IAction old = (IAction) actions.get(this);
				if (old != null && old != action) {
					ClearcasePlugin.log(IStatus.WARNING,
							"Mismatched actions in ClearcaseAction", null);
				}
				actions.put(this, action);
			}
		}
	}

	private void deregisterAction() {
		synchronized (actions) {
			actions.remove(this);
		}
	}

	// We may want to register a cache IResourceStateListener so that if the
	// state changes,
	// and we have not changed our selection, the action enablement gets updated
	// to reflect
	// the new state. May be too much overhead for too little benefit
	protected void updateActionEnablement() {
		synchronized (actions) {
			for (Iterator iter = actions.entrySet().iterator(); iter.hasNext();) {
				Map.Entry element = (Map.Entry) iter.next();
				ClearcaseAction ccAction = (ClearcaseAction) element.getKey();
				IAction action = (IAction) element.getValue();
				ccAction.setActionEnablement(action);
			}
		}
	}

	public void selectionChanged(IAction action, ISelection newSlection) {
		super.selectionChanged(action, newSlection);
		registerAction(action);
	}

	/**
	 * Returns the files which are not saved and which are part of the resources
	 * being exported.
	 * 
	 * @return an array of unsaved files
	 */
	protected IFile[] getUnsavedFiles() {
		IEditorPart[] dirtyEditors = getDirtyEditors(getShell());
		Set unsavedFiles = new HashSet(dirtyEditors.length);
		if (dirtyEditors.length > 0) {
			List selectedResources = Arrays.asList(getSelectedResources());
			for (int i = 0; i < dirtyEditors.length; i++) {
				if (dirtyEditors[i].getEditorInput() instanceof IFileEditorInput) {
					IFile dirtyFile = ((IFileEditorInput) dirtyEditors[i]
							.getEditorInput()).getFile();
					if (selectedResources.contains(dirtyFile)) {
						unsavedFiles.add(dirtyFile);
					}
				}
			}
		}
		return (IFile[]) unsavedFiles.toArray(new IFile[unsavedFiles.size()]);
	}

	/**
	 * Returns a list of selected resources that are different from their
	 * predecessors.
	 * 
	 * @return a list of selected resources that are different from their
	 *         predecessors
	 */
	protected IResource[] getChangedResources() {
		IResource[] resources = getSelectedResources();
		Set changedResources = new HashSet(resources.length);
		if (resources.length > 0) {
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				ClearcaseProvider provider = ClearcaseProvider
						.getClearcaseProvider(resource);
				if (null != provider) {
					if (provider.isDifferent(resource))
						changedResources.add(resource);
				}
			}
		}
		return (IResource[]) changedResources
				.toArray(new IResource[changedResources.size()]);

	}

	/**
	 * Returns the dirty editors.
	 * 
	 * @param parent
	 * @return the dirty editors
	 */
	IEditorPart[] getDirtyEditors(Shell parent) {
		Display display = parent.getDisplay();
		final Object[] result = new Object[1];
		display.syncExec(new Runnable() {
			public void run() {
				result[0] = ClearcaseUI.getDirtyEditors();
			}
		});
		return (IEditorPart[]) result[0];
	}

	/**
	 * Asks to confirm to save the modified resources and save them if OK is
	 * pressed.
	 * 
	 * @return true if user pressed OK and save was successful.
	 */
	protected boolean saveModifiedResourcesIfUserConfirms(IFile[] dirtyFiles) {
		if (confirmSaveModifiedResources(dirtyFiles))
			return saveModifiedResources(dirtyFiles);

		return false;
	}

	/**
	 * Asks the user to confirm to save the modified resources.
	 * 
	 * @return true if user pressed OK.
	 */
	private boolean confirmSaveModifiedResources(IFile[] dirtyFiles) {
		if (dirtyFiles == null || dirtyFiles.length == 0)
			return true;

		// Get display for further UI operations
		Display display = getShell().getDisplay();
		if (display == null || display.isDisposed())
			return false;

		// Ask user to confirm saving of all files
		final ConfirmSaveModifiedResourcesDialog dlg = new ConfirmSaveModifiedResourcesDialog(
				getShell(), dirtyFiles);
		final int[] intResult = new int[1];
		Runnable runnable = new Runnable() {
			public void run() {
				intResult[0] = dlg.open();
			}
		};
		display.syncExec(runnable);

		return intResult[0] == IDialogConstants.OK_ID;
	}

	/**
	 * Save all of the editors in the workbench.
	 * 
	 * @return true if successful.
	 */
	private boolean saveModifiedResources(final IFile[] dirtyFiles) {
		// Get display for further UI operations
		Display display = getShell().getDisplay();
		if (display == null || display.isDisposed())
			return false;

		final boolean[] retVal = new boolean[1];
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					new ProgressMonitorDialog(getShell()).run(false, false,
							createSaveModifiedResourcesRunnable(dirtyFiles));
					retVal[0] = true;
				} catch (InvocationTargetException ex) {
					handle(ex, "Error Saving",
							"An error occured while saving modified resource.");
					retVal[0] = false;
				} catch (InterruptedException ex) {
					// Can't happen. Operation isn't cancelable.
					retVal[0] = false;
				}
			}
		};
		display.syncExec(runnable);
		return retVal[0];
	}

	IRunnableWithProgress createSaveModifiedResourcesRunnable(
			final IFile[] dirtyFiles) {
		return new IRunnableWithProgress() {
			public void run(final IProgressMonitor pm) {
				IEditorPart[] editorsToSave = getDirtyEditors(getShell());
				pm.beginTask("saving modified resources", editorsToSave.length); //$NON-NLS-1$
				try {
					List dirtyFilesList = Arrays.asList(dirtyFiles);
					for (int i = 0; i < editorsToSave.length; i++) {
						if (editorsToSave[i].getEditorInput() instanceof IFileEditorInput) {
							IFile dirtyFile = ((IFileEditorInput) editorsToSave[i]
									.getEditorInput()).getFile();
							if (dirtyFilesList.contains((dirtyFile)))
								editorsToSave[i].doSave(new SubProgressMonitor(
										pm, 1));
						}
						pm.worked(1);
					}
				} finally {
					pm.done();
				}
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#getShell()
	 */
	protected Shell getShell() {
		return super.getShell();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#handle(java.lang.Exception,
	 *      java.lang.String, java.lang.String)
	 */
	protected void handle(Exception exception, String title, String message) {
		super.handle(exception, title, message);
	}

	/**
	 * @param monitor
	 * @param resources
	 */
	protected static void beginTask(IProgressMonitor monitor, String taskName,
			int length) {
		monitor.beginTask(taskName, length * 10000);
	}

	/**
	 * @param monitor
	 * @return new submonitor
	 */
	protected static IProgressMonitor subMonitor(IProgressMonitor monitor) {
		if (monitor == null)
			return new NullProgressMonitor();

		if (monitor.isCanceled())
			throw new OperationCanceledException();

		return new SubProgressMonitor(monitor, 10000);
	}

	/**
	 * Checks if the monitor has been canceled.
	 * 
	 * @param monitor
	 */
	protected static void checkCanceled(IProgressMonitor monitor) {
		if (null != monitor && monitor.isCanceled())
			throw new OperationCanceledException();
	}
}

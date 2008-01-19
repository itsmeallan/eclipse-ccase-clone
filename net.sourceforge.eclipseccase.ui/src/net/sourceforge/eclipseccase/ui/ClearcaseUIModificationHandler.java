/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/

package net.sourceforge.eclipseccase.ui;

import java.lang.reflect.InvocationTargetException;
import net.sourceforge.eclipseccase.*;
import net.sourceforge.eclipseccase.ui.actions.ClearDlgHelper;
import net.sourceforge.eclipseccase.ui.preferences.ClearcasePreferenceStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * A Clearcase modification handler that uses the Eclipse UI to show feedback.
 * <p>
 * This class is not intended to be subclassed, instanciated or called outside
 * the Eclipse ClearCase integration.
 * </p>
 */
class ClearcaseUIModificationHandler extends ClearcaseModificationHandler {

	/** the preference store */
	static final IPreferenceStore store = new ClearcasePreferenceStore();

	/** the lock to handle concurrent validate edit requests */
	private final ILock validateEditLock = Platform.getJobManager().newLock();

	/**
	 * Creates a new instance.
	 * 
	 */
	protected ClearcaseUIModificationHandler() {
		super();
	}

	/**
	 * Returns the shell context
	 * 
	 * @param context
	 * @return the shell context
	 */
	private Shell getShell(Object context) {
		if (context instanceof Shell)
			return (Shell) context;
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.eclipseccase.ClearcaseModificationHandler#validateEdit(org.eclipse.core.resources.IFile[],
	 *      java.lang.Object)
	 */
	public IStatus validateEdit(IFile[] files, Object context) {
		final Shell shell = getShell(context);
		if (null == shell)
			return super.validateEdit(files, context);

		try {
			validateEditLock.acquire();
			final IFile[] readOnlyFiles = getFilesToCheckout(files);
			if (readOnlyFiles.length == 0)
				return OK;
			final IStatus[] status = new IStatus[1];
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

				public void run() {
					status[0] = checkout(readOnlyFiles, shell);
				}
			});
			if (null == status[0])
				return CANCEL;
			return status[0];
		} finally {
			validateEditLock.release();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.eclipseccase.ClearcaseModificationHandler#validateSave(org.eclipse.core.resources.IFile)
	 */
	public IStatus validateSave(final IFile file) {
		try {
			validateEditLock.acquire();
			if (!needsCheckout(file))
				return OK;
			final IStatus[] status = new IStatus[1];
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

				public void run() {
					status[0] = checkout(new IFile[] { file }, PlatformUI.getWorkbench().getDisplay().getActiveShell());
				}
			});
			if (null == status[0])
				return CANCEL;
			return status[0];
		} finally {
			validateEditLock.release();
		}
	}

	/**
	 * Checks out the specified files.
	 * 
	 * @param files
	 * @param shell
	 * @return a status describing the result
	 */
	private IStatus checkout(final IFile[] files, final Shell shell) {

		// cancel if auto-checkout is disabled to give underlying
		// logic a chance to handle that case
		if (ClearcasePlugin.isCheckoutAutoNever())
			return CANCEL;

		// check if we are allowed to prompt
		if (!ClearcasePlugin.isCheckoutAutoAlways()) {
			MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoCancelQuestion(shell, Messages.getString("ClearcaseUIModificationHandler.checkoutDialog.title"), //$NON-NLS-1$
					Messages.getString("ClearcaseUIModificationHandler.checkoutDialog.message"), //$NON-NLS-1$
					null, false, store, IClearcasePreferenceConstants.CHECKOUT_AUTO);

			switch (dialog.getReturnCode()) {
			case IDialogConstants.OK_ID:
			case IDialogConstants.YES_ID:
			case IDialogConstants.YES_TO_ALL_ID:
				break;

			default:
				return CANCEL;
			}

		}

		final ClearcaseProvider provider = getProvider(files);

		// check for provider
		if (null == provider) {
			ClearcasePlugin.log(Messages.getString("ClearcaseUIModificationHandler.error.noProvider"), //$NON-NLS-1$
					new IllegalStateException(Messages.getString("ClearcaseUIModificationHandler.error.noProviderAvailable"))); //$NON-NLS-1$
			MessageDialog.openError(shell, Messages.getString("ClearcaseUIModificationHandler.errorDialog.title"), //$NON-NLS-1$
					Messages.getString("ClearcaseUIModificationHandler.errorDialog.message")); //$NON-NLS-1$
			return CANCEL;
		}

		final boolean useClearDlg = ClearcasePlugin.isUseClearDlg();
		final boolean askForComment = ClearcasePlugin.isCommentCheckout() && !ClearcasePlugin.isCommentCheckoutNeverOnAuto();

		try {
			// use workbench window as preferred runnable context
			IRunnableContext context;
			if (PlatformUI.isWorkbenchRunning() && null != PlatformUI.getWorkbench().getActiveWorkbenchWindow())
				context = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			else
				context = new ProgressMonitorDialog(shell);

			// checkout
			PlatformUI.getWorkbench().getProgressService().runInUI(context, new IRunnableWithProgress() {

				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						String comment = null;

						if (!useClearDlg && askForComment) {
							CommentDialog dlg = new CommentDialog(shell, "Checkout comment");
							dlg.setRecursive(false);
							dlg.setRecursiveEnabled(false);
							if (dlg.open() == Window.CANCEL)
								throw new InterruptedException("Operation canceled by user.");
							else
								comment = dlg.getComment();
						}

						synchronized (provider) {
							boolean refreshing = setResourceRefreshing(provider, false);
							try {
								monitor.beginTask(Messages.getString("ClearcaseUIModificationHandler.task.checkout"), files.length); //$NON-NLS-1$
								if (ClearcasePlugin.isUseClearDlg()) {
									monitor.subTask("Executing ClearCase user interface...");
									ClearDlgHelper.checkout(files);
								} else {
									if (null != comment)
										provider.setComment(comment);

									for (int i = 0; i < files.length; i++) {
										IFile file = files[i];
										monitor.subTask(file.getName());
										provider.checkout(new IFile[] { file }, IResource.DEPTH_ZERO, null);
										file.refreshLocal(IResource.DEPTH_ZERO, null);
										monitor.worked(i);
									}
								}
							} finally {
								setResourceRefreshing(provider, refreshing);
								monitor.done();
							}
						}
					} catch (CoreException ex) {
						throw new InvocationTargetException(ex);
					}
				}
			}, new MultiRule(files));
		} catch (InvocationTargetException e) {
			ClearcasePlugin.log(Messages.getString("ClearcaseUIModificationHandler.error.checkout") //$NON-NLS-1$
					+ (null != e.getCause() ? e.getCause().getMessage() : e.getMessage()), null != e.getCause() ? e.getCause(): e);
			MessageDialog.openError(shell,Messages.getString("ClearcaseUIModificationHandler.errorDialog.title"), //$NON-NLS-1$
			Messages.getString("ClearcaseUIModificationHandler.errorDialog.message")); //$NON-NLS-1$
			return CANCEL;
		} catch (InterruptedException e) {
			return CANCEL;
		}

		return OK;
	}
}
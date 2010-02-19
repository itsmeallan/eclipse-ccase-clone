/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matthew Conway - initial API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *     Gunnar Wagenknecht - new features, enhancements and bug fixes
 *******************************************************************************/
package net.sourceforge.eclipseccase;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.team.FileModificationValidationContext;
import org.eclipse.core.resources.team.FileModificationValidator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.PlatformUI;

/**
 * A simple file modification handler for the ClearCase integration.
 * <p>
 * Although this class is public it is not intended to be subclassed,
 * instanciated or called outside the Eclipse ClearCase integration.
 * </p>
 */
public class ClearcaseModificationHandler extends FileModificationValidator {

	/** constant for OK status */
	protected static final IStatus OK = ClearcaseProvider.OK_STATUS;

	/** constant for CANCEL status */
	protected static final IStatus CANCEL = ClearcaseProvider.CANCEL_STATUS;

	/**
	 * Constructor for ClearcaseModificationHandler.
	 * 
	 * @param provider
	 */
	protected ClearcaseModificationHandler() {
		// protected
	}

	/**
	 * Indicates if a file needs to be checked out.
	 * 
	 * @param file
	 * @return <code>true</code> if a file needs to be checked out
	 */
	protected boolean needsCheckout(IFile file) {

		// writable files don't need to be checked out
		if (file.isReadOnly()) {
			ClearcaseProvider provider = ClearcaseProvider
					.getClearcaseProvider(file);

			// if there is no provider, it's not a ClearCase file
			if (null != provider) {

				// ensure resource state is initialized
				provider.ensureInitialized(file);

				// needs checkout if file is managed
				return provider.hasRemote(file);
			}
		}
		return false;
	}

	/**
	 * Returns a list of files that need to be checked out.
	 * 
	 * @param files
	 * @return a list of files that need to be checked out
	 */
	protected IFile[] getFilesToCheckout(IFile[] files) {

		// collect files that need to be checked out
		List<IFile> readOnlys = new ArrayList<IFile>();
		for (int i = 0; i < files.length; i++) {
			IFile iFile = files[i];
			if (needsCheckout(iFile)) {
				readOnlys.add(iFile);
			}
		}
		return (IFile[]) readOnlys.toArray(new IFile[readOnlys.size()]);
	}

	/**
	 * Returns the Clearcase Team provider for all files.
	 * <p>
	 * This implementation requires all files to be in the same project.
	 * </p>
	 * 
	 * @param files
	 * @return the Clearcase Team provider for all files
	 */
	protected ClearcaseProvider getProvider(IFile[] files) {
		if (files.length > 0)
			return ClearcaseProvider.getClearcaseProvider(files[0]);
		return null;
	}

	/**
	 * Enables or disables refreshing in the ClearCase provider.
	 * 
	 * @param provider
	 * @param refreshResource
	 * 
	 * @return the old value
	 */
	protected boolean setResourceRefreshing(ClearcaseProvider provider,
			boolean refreshResource) {
		boolean old = provider.refreshResources;
		provider.refreshResources = refreshResource;
		return old;
	}

	/**
	 * Checks out the specified files.
	 * 
	 * @param files
	 * @return a status describing the result
	 */
	private IStatus checkout(final IFile[] files) {

		if (ClearcasePlugin.isCheckoutAutoNever())
			return CANCEL;

		if (!ClearcasePlugin.isCheckoutAutoAlways()) {
			CheckoutQuestionRunnable checkoutQuestion = new CheckoutQuestionRunnable();
			getDisplay().syncExec(checkoutQuestion);
			int returncode = checkoutQuestion.getResult();
			/* Yes=0 No=1 Cancel=2 */
			if (returncode == 1)
				return new Status(IStatus.CANCEL, ClearcasePlugin.PLUGIN_ID,
						"Checkout operation failed, operation was cancelled by user.");
			else if (returncode == 2)
				// Cancel option.
				return new Status(IStatus.CANCEL, ClearcasePlugin.PLUGIN_ID,
						"Checkout operation failed, operation was cancelled by user");
		}

		ClearcaseProvider provider = getProvider(files);

		// check for provider
		if (null == provider)
			return new Status(IStatus.ERROR, ClearcaseProvider.ID,
					TeamException.NOT_CHECKED_OUT, "No ClearCase resources!",
					new IllegalStateException("Provider is null!"));

		// checkout
		try {
			synchronized (provider) {
				boolean refreshing = setResourceRefreshing(provider, false);
				try {
					for (int i = 0; i < files.length; i++) {
						IFile file = files[i];
						provider.checkout(new IFile[] { file },
								IResource.DEPTH_ZERO, null);
						file.refreshLocal(IResource.DEPTH_ZERO, null);
					}
				} finally {
					setResourceRefreshing(provider, refreshing);
				}
			}
		} catch (CoreException ex) {
			return ex.getStatus();
		}
		return OK;
	}

	private Display getDisplay() {
		Display display = PlatformUI.getWorkbench().getDisplay();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	private class CheckoutQuestionRunnable implements Runnable {

		private int dialogResult;

		public void run() {
			MessageDialog checkoutQuestion = new MessageDialog(getDisplay()
					.getActiveShell(), "Checkout", null,
					"Do you really want to checkout?", MessageDialog.QUESTION,
					new String[] { "Yes", "No", "Cancel" }, 0);
			dialogResult = checkoutQuestion.open();
		}

		public int getResult() {
			return dialogResult;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.resources.team.FileModificationValidator#validateEdit
	 * (org.eclipse.core.resources.IFile[],
	 * org.eclipse.core.resources.team.FileModificationValidationContext)
	 */
	@Override
	public IStatus validateEdit(IFile[] files,
			FileModificationValidationContext context) {
		IFile[] readOnlyFiles = getFilesToCheckout(files);
		if (readOnlyFiles.length == 0)
			return OK;
		return checkout(readOnlyFiles);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.resources.IFileModificationValidator#validateSave(org
	 * .eclipse.core.resources.IFile)
	 */
	@Override
	public IStatus validateSave(IFile file) {
		if (!needsCheckout(file))
			return OK;
		return checkout(new IFile[] { file });
	}

}
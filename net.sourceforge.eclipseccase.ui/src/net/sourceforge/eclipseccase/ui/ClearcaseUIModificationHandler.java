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

import net.sourceforge.eclipseccase.ClearcaseModificationHandler;
import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.ClearcaseProvider;
import net.sourceforge.eclipseccase.IClearcasePreferenceConstants;
import net.sourceforge.eclipseccase.ui.preferences.ClearcasePreferenceStore;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
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
        if (context instanceof Shell) return (Shell) context;
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
        if (null == shell) return super.validateEdit(files, context);

        try {
            validateEditLock.acquire();
            final IFile[] readOnlyFiles = getFilesToCheckout(files);
            if (readOnlyFiles.length == 0) return OK;
            final IStatus[] status = new IStatus[1];
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

                public void run() {
                    status[0] = checkout(readOnlyFiles, shell);
                }
            });
            if(null == status[0])
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

        // don't fail and don't do anything if auto checkout is NEVER
        if (ClearcasePlugin.isCheckoutAutoNever()) return OK;

        // check if we are allowed to prompt
        if (!ClearcasePlugin.isCheckoutAutoAlways()) {
            MessageDialogWithToggle dialog = MessageDialogWithToggle
                    .openYesNoCancelQuestion(shell, "ClearCase Checkout",
                            "Do you want to checkout the selected resources?",
                            null, false, store,
                            IClearcasePreferenceConstants.CHECKOUT_AUTO);

            switch (dialog.getReturnCode()) {
            case IDialogConstants.CANCEL_ID:
                return CANCEL;
            case IDialogConstants.NO_ID:
                return OK;
            }
        }

        final ClearcaseProvider provider = getProvider(files);

        // check for provider
        if (null == provider) {
            ClearcasePlugin.log("No provider found!",
                    new IllegalStateException("No provider available!"));
            MessageDialog
                    .openError(shell, "ClearCase Checkout",
                            "Could not checkout the selected resources. Please see log for details.");
            return CANCEL;
        }

        try {
            // use workbench window as preferred runnable context
            IRunnableContext context;
            if (PlatformUI.isWorkbenchRunning()
                    && null != PlatformUI.getWorkbench()
                            .getActiveWorkbenchWindow()) {
                context = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            } else {
                context = new ProgressMonitorDialog(shell);
            }

            // checkout
            PlatformUI.getWorkbench().getProgressService().runInUI(context,
                    new IRunnableWithProgress() {

                        public void run(IProgressMonitor monitor)
                                throws InvocationTargetException,
                                InterruptedException {
                            try {
                                provider.checkout(files, IResource.DEPTH_ZERO,
                                        monitor);

                            } catch (TeamException ex) {
                                throw new InvocationTargetException(ex);
                            }
                        }
                    }, new MultiRule(files));
        } catch (InvocationTargetException e) {
            ClearcasePlugin.log("Errors during auto checkout: "
                    + (null != e.getCause() ? e.getCause().getMessage() : e
                            .getMessage()), null != e.getCause() ? e.getCause()
                    : e);
            MessageDialog
                    .openError(shell, "ClearCase Checkout",
                            "Could not checkout the selected resources. Please see log for details.");
            return CANCEL;
        } catch (InterruptedException e) {
            return CANCEL;
        }

        return OK;
    }
}
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
import org.eclipse.core.resources.IFileModificationValidator;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.TeamException;

/**
 * A simple file modification handler for the ClearCase integration.
 * <p>
 * Although this class is public it is not intended to be subclassed,
 * instanciated or called outside the Eclipse ClearCase integration.
 * </p>
 */
public class ClearcaseModificationHandler implements IFileModificationValidator {

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
        List readOnlys = new ArrayList();
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
     * Checks out the specified files.
     * 
     * @param files
     * @return a status describing the result
     */
    private IStatus checkout(final IFile[] files) {
        
        // don't fail and don't do anything if auto checkout is NEVER
        if(ClearcasePlugin.isCheckoutAutoNever()) return OK;
        
        // fail if not set to always
        if (!ClearcasePlugin.isCheckoutAutoAlways()) {
            StringBuffer message = new StringBuffer(
                    "No auto checkout performed for the following resources:\n");
            for (int i = 0; i < files.length; i++) {
                IFile file = files[i];
                message.append("\n\t" + file.getFullPath());
            }
            return new Status(IStatus.ERROR, ClearcaseProvider.ID,
                    TeamException.NOT_CHECKED_OUT, message.toString(), null);
        }

        ClearcaseProvider provider = getProvider(files);

        // check for provider
        if (null == provider) { return new Status(IStatus.ERROR,
                ClearcaseProvider.ID, TeamException.NOT_CHECKED_OUT,
                "No ClearCase resources!", new IllegalStateException(
                        "Provider is null!")); }

        // checkout
        try {
            provider.checkout(files, IResource.DEPTH_ZERO, null);
        } catch (TeamException ex) {
            return ex.getStatus();
        }
        return OK;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IFileModificationValidator#validateEdit(org.eclipse.core.resources.IFile[],
     *      java.lang.Object)
     */
    public IStatus validateEdit(IFile[] files, Object context) {
        IFile[] readOnlyFiles = getFilesToCheckout(files);
        if (readOnlyFiles.length == 0) return OK;
        return checkout(readOnlyFiles);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IFileModificationValidator#validateSave(org.eclipse.core.resources.IFile)
     */
    public IStatus validateSave(IFile file) {
        if(!needsCheckout(file)) return OK;
        return checkout(new IFile[] {file});
    }
}
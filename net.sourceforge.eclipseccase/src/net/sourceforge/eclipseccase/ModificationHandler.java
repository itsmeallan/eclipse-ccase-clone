
package net.sourceforge.eclipseccase;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileModificationValidator;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.TeamException;

/**
 * The file modification handler for auto-checkout.
 */
public class ModificationHandler implements IFileModificationValidator
{
	
	ClearcaseProvider provider;

	/**
	 * Constructor for ModificationHandler.
     * @param provider
	 */
	public ModificationHandler(ClearcaseProvider provider)
	{
		this.provider = provider;
	}

	/**
	 * @see IFileModificationValidator#validateEdit(IFile[], Object)
	 */
	public IStatus validateEdit(IFile[] files, Object context)
	{
		IStatus result = new Status(IStatus.OK, ClearcaseProvider.ID, TeamException.OK, "OK", null);
		Set needCheckout = new HashSet();
		for (int i = 0; i < files.length; ++i)
		{
            if(!provider.isIgnored(files[i]))
            {
                StateCache cache = StateCacheFactory.getInstance().get(files[i]);
                if (cache.isUninitialized())
                    cache.doUpdate();
                if (cache.hasRemote() && ! cache.isCheckedOut() && files[i].isReadOnly())
                    needCheckout.add(files[i]);
            }
		}
        
        if(!needCheckout.isEmpty())
        {
            if (!ClearcasePlugin.isCheckoutOnEdit())
            {
                StringBuffer message = new StringBuffer("No auto checkout performed for the following resources:\n");
                for (Iterator iter = needCheckout.iterator(); iter.hasNext();)
                {
                    IResource element = (IResource) iter.next();
                    message.append("\n\t" + element.getFullPath());
                }
                result = new Status(IStatus.ERROR, ClearcaseProvider.ID, TeamException.NOT_CHECKED_OUT,
                message.toString(), null); 
//                if(null != context && context instanceof Shell)
//                {
//                    MessageDialog.openInformation((Shell)context, "Check Out", "The ClearCase auto checkout feature is disabled. Resources need to be checked out manually.");
//                }
                return result;
            }
            
            try
            {
                provider.checkout((IResource[]) needCheckout.toArray(new IResource[needCheckout.size()]),
                                 IResource.DEPTH_INFINITE, null);
				//// Refresh resource state so that editor context menus/completion/etc know that file is now writable
				//for (Iterator iter = needCheckout.iterator(); iter.hasNext();)
				//{
				//    IResource element = (IResource) iter.next();
				//    element.refreshLocal(IResource.DEPTH_ZERO, null);
				//}
            }
            catch(TeamException ex)
            {
                result = ex.getStatus();
            }
//            catch (CoreException ex)
//            {
//                result = new Status(IStatus.WARNING, ClearcaseProvider.PLUGIN_ID, TeamException.IO_FAILED, "Failed to refresh resource state: " + ex.getLocalizedMessage(), ex);
//            }
        }

        return result;          
	}

	/**
	 * @see IFileModificationValidator#validateSave(IFile)
	 */
	public IStatus validateSave(IFile file)
	{
		return validateEdit(new IFile[] {file}, null);
	}

}

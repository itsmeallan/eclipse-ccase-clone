package net.sourceforge.eclipseccase.actions;

import net.sourceforge.eclipseccase.ClearcaseProvider;
import net.sourceforge.eclipseccase.compare.ResourceCompareInput;
import net.sourceforge.eclipseccase.compare.VersionExtendedFile;
import net.sourceforge.eclipseccase.compare.VersionExtendedFolder;
import net.sourceforge.eclipseccase.compare.VersionExtendedProject;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.team.core.TeamException;

/**
 *  Pulls up the clearcase version tree for the element
 */
public class CompareWithPredecessorInternalAction extends ClearcaseAction
{

    private ResourceCompareInput fInput;

    /**
     * @see TeamAction#isEnabled()
     */
    protected boolean isEnabled() throws TeamException
    {
        IResource[] resources = getSelectedResources();
        if (resources.length != 1)
        {
            return false;
        }
        IResource resource = resources[0];
        ClearcaseProvider provider = ClearcaseProvider.getProvider(resource);
        if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource))
            return false;

        // Only allow comparing of folders for dynamic views for now
        // (even on dynamic views it is somewhat broken)
        if (resource.getType() == IResource.FOLDER && provider.isSnapShot(resource))
        {
            return false;
        }

        return setCompareResources(resource, false);
    }

    public void run(IAction action)
    {
        if (fInput != null)
        {
            IResource[] resources = getSelectedResources();
            IResource resource = resources[0];
            setCompareResources(resource, true);
            fInput.initializeCompareConfiguration();
            CompareUI.openCompareEditorOnPage(fInput, getTargetPage());
            fInput = null; // don't reuse this input!
        }
    }
    /**
     * Gets the current, and previous version of given resource and checks if they are comparable
     * Also fInput is filled with both versions.
     * @param resource the resource from whihc to get the current and previous version
     * @param update Do we need to update the snapshot resource? 
     * @return true when both versions can be compared
     */
    private boolean setCompareResources(IResource resource, boolean update)
    {
        ClearcaseProvider provider = ClearcaseProvider.getProvider(resource);
        IResource current = null;
        IResource predecessor = null;

        String currentversion = provider.getVersion(resource);
        if (currentversion.equals(""))
        {
            if (provider.isSnapShot(resource) && provider.isHijacked(resource))
            {
                currentversion = "/HIJACKED";
            }
        }
        String version = provider.getPredecessorVersion(resource);

        switch (resource.getType())
        {
            case IResource.FILE :
                current = new VersionExtendedFile((IFile) resource, currentversion);
                predecessor = new VersionExtendedFile((IFile) resource, version);

                break;
            case IResource.FOLDER :
                predecessor = new VersionExtendedFolder((IFolder) resource, version);
                current = new VersionExtendedFolder((IFolder) resource, currentversion);
                break;
            case IResource.PROJECT :
                predecessor = new VersionExtendedProject((IProject) resource, version);
                current = new VersionExtendedProject((IProject) resource, currentversion);
                break;
            default :
                return false;
        }

        IResource[] comparables = new IResource[] { current, predecessor };
        return fInput.setResources(comparables);
    }

    public void selectionChanged(IAction action, ISelection selection)
    {
        if (fInput == null)
        {
            CompareConfiguration cc = new CompareConfiguration();
            // buffered merge mode: don't ask for confirmation
            // when switching between modified resources
            cc.setProperty(CompareEditor.CONFIRM_SAVE_PROPERTY, new Boolean(false));

            fInput = new ResourceCompareInput(cc);
        }
        super.selectionChanged(action, selection);
    }

}
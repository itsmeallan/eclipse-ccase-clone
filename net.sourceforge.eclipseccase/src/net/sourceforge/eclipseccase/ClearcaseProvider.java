package net.sourceforge.eclipseccase;

import java.io.File;
import java.text.MessageFormat;

import net.sourceforge.clearcase.simple.ClearcaseUtil;
import net.sourceforge.clearcase.simple.IClearcase;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFileModificationValidator;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.simpleAccess.SimpleAccessOperations;

public class ClearcaseProvider extends RepositoryProvider implements SimpleAccessOperations
{

    private IMoveDeleteHook moveHandler = new MoveHandler(this);
    private IFileModificationValidator modificationValidator = new ModificationHandler(this);

    private String comment = "";

    public static final String ID = "net.sourceforge.eclipseccase.ClearcaseProvider";

    static final Status OK_STATUS = new Status(IStatus.OK, ID, TeamException.OK, "OK", null);

    public ClearcaseProvider()
    {
        super();
    }

    /**
     * @see RepositoryProvider#configureProject()
     */
    public void configureProject() throws CoreException
    {}

    /**
     * @see RepositoryProvider#getID()
     */
    public String getID()
    {
        return ID;
    }

    /**
     * @see IProjectNature#deconfigure()
     */
    public void deconfigure() throws CoreException
    {}

    public static ClearcaseProvider getProvider(IResource resource)
    {
        RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject());
        if (provider instanceof ClearcaseProvider)
            return (ClearcaseProvider) provider;
        else
            return null;
    }

    /**
     * @see SimpleAccessOperations#get(IResource[], int, IProgressMonitor)
     */
    public void get(IResource[] resources, int depth, IProgressMonitor progress)
        throws TeamException
    {
        execute(new IIterativeOperation()
        {
            public IStatus visit(IResource resource, int depth, IProgressMonitor progress)
            {
                // Sanity check - can't delete something that is not part of clearcase
                if (!hasRemote(resource))
                {
                    return new Status(
                        IStatus.ERROR,
                        ID,
                        TeamException.NO_REMOTE_RESOURCE,
                        MessageFormat.format(
                            "Resource \"{0}\" is not a ClearCase element!",
                            new Object[] { resource.getFullPath().toString()}),
                        null);
                }

                IStatus result = OK_STATUS;
                String filename = resource.getLocation().toOSString();
                IClearcase.Status status =
                    ClearcasePlugin.getEngine().cleartool(
                        "update -log NUL -force -ptime " + ClearcaseUtil.quote(filename));
                changeState(resource, IResource.DEPTH_INFINITE, progress);
                if (!status.status)
                {
                    result =
                        new Status(
                            IStatus.ERROR,
                            ID,
                            TeamException.UNABLE,
                            "Update failed: " + status.message,
                            null);
                }
                return result;
            }
        }, resources, IResource.DEPTH_INFINITE, progress);
    }

    /**
     * @see SimpleAccessOperations#checkout(IResource[], int, IProgressMonitor)
     */
    public void checkout(IResource[] resources, int depth, IProgressMonitor progress)
        throws TeamException
    {
        try
        {
            execute(new IRecursiveOperation()
            {
                public IStatus visit(IResource resource, IProgressMonitor progress)
                {
                    // Sanity check - can't delete something that is not part of clearcase
                    if (!hasRemote(resource))
                    {
                        return new Status(
                            IStatus.WARNING,
                            ID,
                            TeamException.NO_REMOTE_RESOURCE,
                            MessageFormat.format(
                                "Resource \"{0}\" is not a ClearCase element!",
                                new Object[] { resource.getFullPath().toString()}),
                            null);
                    }

                    // Sanity check - can't checkout something that is already checked out
                    if (isCheckedOut(resource))
                    {
                        return new Status(
                            IStatus.WARNING,
                            ID,
                            TeamException.NOT_CHECKED_IN,
                            MessageFormat.format(
                                "Resource \"{0}\" is already checked out!",
                                new Object[] { resource.getFullPath().toString()}),
                            null);
                    }

                    IStatus result = OK_STATUS;
                    boolean reserved = ClearcasePlugin.isReservedCheckouts();
                    IClearcase.Status status =
                        ClearcasePlugin.getEngine().checkout(
                            resource.getLocation().toOSString(),
                            getComment(),
                            reserved,
                            true);
                    changeState(resource, IResource.DEPTH_ZERO, progress);
                    if (!status.status)
                    {
                        result =
                            new Status(
                                IStatus.ERROR,
                                ID,
                                TeamException.UNABLE,
                                "Checkout failed: " + status.message,
                                null);
                    }
                    return result;
                }
            }, resources, depth, progress);
        }
        finally
        {
            setComment("");
        }
    }

    public void refresh(IResource[] resources, int depth, IProgressMonitor progress)
        throws TeamException
    {
        execute(new IRecursiveOperation()
        {
            public IStatus visit(IResource resource, IProgressMonitor progress)
            {
                Status result = OK_STATUS;
                StateCache cache = StateCacheFactory.getInstance().get(resource);
                cache.updateAsync(false, true);
                return result;
            }
        }, resources, depth, progress);
    }

    public void refreshQuick(IResource[] resources, int depth, IProgressMonitor progress)
        throws TeamException
    {
        execute(new IRecursiveOperation()
        {
            public IStatus visit(IResource resource, IProgressMonitor progress)
            {
                Status result = OK_STATUS;
                StateCache cache = StateCacheFactory.getInstance().get(resource);
                cache.updateAsync(true, true);
                return result;
            }
        }, resources, depth, progress);
    }

    /**
     * @see SimpleAccessOperations#checkin(IResource[], int, IProgressMonitor)
     */
    public void checkin(IResource[] resources, int depth, IProgressMonitor progress)
        throws TeamException
    {
        try
        {
            execute(new IRecursiveOperation()
            {
                public IStatus visit(IResource resource, IProgressMonitor progress)
                {
                    // Sanity check - can't delete something that is not part of clearcase
                    if (!hasRemote(resource))
                    {
                        return new Status(
                            IStatus.WARNING,
                            ID,
                            TeamException.NO_REMOTE_RESOURCE,
                            MessageFormat.format(
                                "Resource \"{0}\" is not a ClearCase element!",
                                new Object[] { resource.getFullPath().toString()}),
                            null);
                    }

                    // Sanity check - can't checkin something that is not checked out
                    if (!isCheckedOut(resource))
                    {
                        return new Status(
                            IStatus.WARNING,
                            ID,
                            TeamException.NOT_CHECKED_OUT,
                            MessageFormat.format(
                                "Resource \"{0}\" is not checked out!",
                                new Object[] { resource.getFullPath().toString()}),
                            null);
                    }

                    IStatus result = OK_STATUS;
                    IClearcase.Status status =
                        ClearcasePlugin.getEngine().checkin(
                            resource.getLocation().toOSString(),
                            getComment(),
                            ClearcasePlugin.isCheckinPreserveTime());
                    changeState(resource, IResource.DEPTH_ZERO, progress);
                    if (!status.status)
                    {
                        result =
                            new Status(
                                IStatus.ERROR,
                                ID,
                                TeamException.UNABLE,
                                "Checkin failed: " + status.message,
                                null);
                    }
                    return result;
                }
            }, resources, depth, progress);
        }
        finally
        {
            setComment("");
        }
    }

    /**
     * @see SimpleAccessOperations#uncheckout(IResource[], int, IProgressMonitor)
     */
    public void uncheckout(IResource[] resources, int depth, IProgressMonitor progress)
        throws TeamException
    {
        execute(new IRecursiveOperation()
        {
            public IStatus visit(IResource resource, IProgressMonitor progress)
            {
                // Sanity check - can't uncheckout something that is not checked out
                if (!isCheckedOut(resource))
                {
                    return new Status(
                        IStatus.WARNING,
                        ID,
                        TeamException.NOT_CHECKED_OUT,
                        MessageFormat.format(
                            "Resource \"{0}\" is not checked out!",
                            new Object[] { resource.getFullPath().toString()}),
                        null);
                }

                IStatus result = OK_STATUS;
                IClearcase.Status status =
                    ClearcasePlugin.getEngine().uncheckout(
                        resource.getLocation().toOSString(),
                        false);
                changeState(resource, IResource.DEPTH_ONE, progress);
                if (!status.status)
                {
                    result =
                        new Status(
                            IStatus.ERROR,
                            ID,
                            TeamException.UNABLE,
                            "Uncheckout failed: " + status.message,
                            null);
                }
                return result;
            }
        }, resources, depth, progress);
    }

    /**
     * @see SimpleAccessOperations#delete(IResource[], IProgressMonitor)
     */
    public void delete(IResource[] resources, IProgressMonitor progress) throws TeamException
    {
        try
        {
            execute(new IIterativeOperation()
            {
                public IStatus visit(IResource resource, int depth, IProgressMonitor progress)
                {
                    // Sanity check - can't delete something that is not part of clearcase
                    if (!hasRemote(resource))
                    {
                        return new Status(
                            IStatus.ERROR,
                            ID,
                            TeamException.NO_REMOTE_RESOURCE,
                            MessageFormat.format(
                                "Resource \"{0}\" is not a ClearCase element!",
                                new Object[] { resource.getFullPath().toString()}),
                            null);
                    }

                    IStatus result = checkoutParent(resource);
                    if (result.isOK())
                    {
                        IClearcase.Status status =
                            ClearcasePlugin.getEngine().delete(
                                resource.getLocation().toOSString(),
                                getComment());
                        StateCacheFactory.getInstance().remove(resource);
                        changeState(resource.getParent(), IResource.DEPTH_ONE, progress);
                        if (!status.status)
                        {
                            result =
                                new Status(
                                    IStatus.ERROR,
                                    ID,
                                    TeamException.UNABLE,
                                    "Delete failed: " + status.message,
                                    null);
                        }
                    }
                    return result;
                }
            }, resources, IResource.DEPTH_INFINITE, progress);
        }
        finally
        {
            setComment("");
        }
    }

    public void add(IResource[] resources, int depth, IProgressMonitor progress)
        throws TeamException
    {
        try
        {
            execute(new IRecursiveOperation()
            {
                public IStatus visit(IResource resource, IProgressMonitor progress)
                {
                    IStatus result;

                    // Sanity check - can't add something that already is under VC
                    if (hasRemote(resource))
                    {
                        return new Status(
                            IStatus.WARNING,
                            ID,
                            TeamException.UNABLE,
                            MessageFormat.format(
                                "Resource \"{0}\" is already under source control!",
                                new Object[] { resource.getFullPath().toString()}),
                            null);
                    }

                    // Walk up parent heirarchy, find first ccase
                    // element that is a parent, and walk back down, adding each to ccase
                    IResource parent = resource.getParent();

                    // When resource is a project, try checkout its parent, and if that fails,
                    // then neither project nor workspace is in clearcase.
                    if (resource instanceof IProject || hasRemote(parent))
                    {
                        result = checkoutParent(resource);
                    }
                    else
                    {
                        result = visit(parent, progress);
                    }

                    if (result.isOK())
                    {
                        if (resource instanceof IFolder)
                        {
                            try
                            {
                                String path = resource.getLocation().toOSString();
                                File origfolder = new File(path);
                                File mkelemfolder = new File(path + ".mkelem");
                                origfolder.renameTo(mkelemfolder);
                                IClearcase.Status status =
                                    ClearcasePlugin.getEngine().add(path, getComment(), true);
                                if (status.status)
                                {
                                    File[] members = mkelemfolder.listFiles();
                                    for (int i = 0; i < members.length; i++)
                                    {
                                        File member = members[i];
                                        File newMember =
                                            new File(origfolder.getPath(), member.getName());
                                        member.renameTo(newMember);
                                    }
                                    mkelemfolder.delete();
                                    changeState(
                                        resource.getParent(),
                                        IResource.DEPTH_ONE,
                                        progress);
                                }
                                else
                                {
                                    result =
                                        new Status(
                                            IStatus.ERROR,
                                            ID,
                                            TeamException.UNABLE,
                                            "Add failed: " + status.message,
                                            null);
                                }

                            }
                            catch (Exception ex)
                            {
                                result =
                                    new Status(
                                        IStatus.ERROR,
                                        ID,
                                        TeamException.UNABLE,
                                        "Add failed: " + ex.getMessage(),
                                        ex);

                            }
                        }
                        else
                        {
                            IClearcase.Status status =
                                ClearcasePlugin.getEngine().add(
                                    resource.getLocation().toOSString(),
                                    getComment(),
                                    false);
                            changeState(resource, IResource.DEPTH_ZERO, progress);
                            if (!status.status)
                            {
                                result =
                                    new Status(
                                        IStatus.ERROR,
                                        ID,
                                        TeamException.UNABLE,
                                        "Add failed: " + status.message,
                                        null);
                            }
                        }

                    }

                    return result;
                }
            }, resources, depth, progress);
        }
        finally
        {
            setComment("");
        }
    }

    /**
     * @see SimpleAccessOperations#moved(IPath, IResource, IProgressMonitor)
     */
    public void moved(IPath source, IResource target, IProgressMonitor progress)
        throws TeamException
    {}

    /**
     * @see SimpleAccessOperations#isCheckedOut(IResource)
     */
    public boolean isCheckedOut(IResource resource)
    {
        return StateCacheFactory.getInstance().get(resource).isCheckedOut();
    }

    public boolean isSnapShot(IResource resource)
    {
        return StateCacheFactory.getInstance().get(resource).isSnapShot();
    }

    public boolean isHijacked(IResource resource)
    {
        return StateCacheFactory.getInstance().get(resource).isHijacked();
    }

    public boolean isUnknownState(IResource resource)
    {
        return StateCacheFactory.getInstance().isUnitialized(resource);
    }

    /**
     * @see SimpleAccessOperations#hasRemote(IResource)
     */
    public boolean hasRemote(IResource resource)
    {
        return StateCacheFactory.getInstance().get(resource).hasRemote();
    }

    /**
     * @see SimpleAccessOperations#isDirty(IResource)
     */
    public boolean isDirty(IResource resource)
    {
        return StateCacheFactory.getInstance().get(resource).isDirty();
    }

    public String getVersion(IResource resource)
    {
        return StateCacheFactory.getInstance().get(resource).getVersion();
    }

    public String getPredecessorVersion(IResource resource)
    {
        return StateCacheFactory.getInstance().get(resource).getPredecessorVersion();
    }

    public String getViewName(IResource resource)
    {
        IClearcase.Status status =
            ClearcasePlugin.getEngine().getViewName(resource.getLocation().toOSString());
        if (status.status)
            return status.message.trim();
        else
            return "none";
    }

    /**
     * Returns the root of the view. An empty view root indicates a dynamic view.
     * @param resource
     * @return
     */
    public String getViewRoot(IResource resource) throws TeamException
    {
        IClearcase.Status status =
            ClearcasePlugin.getEngine().getViewRoot(resource.getLocation().toOSString());
        if (status.status)
            return status.message.trim();
        else
            throw new TeamException(
                new Status(
                    IStatus.ERROR,
                    ClearcasePlugin.ID,
                    TeamException.UNABLE,
                    "Could not determine view root: " + status.message,
                    null));
    }

    /**
     * Returns the name of the vob that contains the specified element
     * @param resource
     * @return
     */
    public String getVobName(IResource resource) throws TeamException
    {
        String viewRoot = getViewRoot(resource);
        IPath viewLocation = new Path(viewRoot);
        IPath resourceLocation = resource.getLocation();

        // ignore device when dealing with dynamic views
        if (viewRoot.length() == 0)
            viewLocation = viewLocation.setDevice(resourceLocation.getDevice());

        if (viewLocation.isPrefixOf(resourceLocation))
        {
            IPath vobLocation = resourceLocation.removeFirstSegments(viewLocation.segmentCount());
            if (!ClearcasePlugin.isWindows && vobLocation.segmentCount() > 0)
            {
                // on unix vobs are prefixed with directory named "/vobs"
                vobLocation = vobLocation.removeFirstSegments(1);
            }
            if (vobLocation.segmentCount() > 0)
                return vobLocation.segment(0);
        }
        return "none";
    }

    /**
     * Returns the vob relative path of the specified element
     * @param resource
     * @return
     */
    public String getVobRelativPath(IResource resource) throws TeamException
    {
        String viewRoot = getViewRoot(resource);
        IPath viewLocation = new Path(viewRoot).setDevice(null); // ignore device
        IPath resourceLocation = resource.getLocation().setDevice(null); // ignore devices

        if (viewLocation.isPrefixOf(resourceLocation))
        {
            IPath vobLocation = resourceLocation.removeFirstSegments(viewLocation.segmentCount());
            if (!ClearcasePlugin.isWindows && vobLocation.segmentCount() > 0)
            {
                // on unix vobs are prefixed with directory named "/vobs"
                vobLocation = vobLocation.removeFirstSegments(1);
            }
            if (vobLocation.segmentCount() > 0)
                return vobLocation.removeFirstSegments(1).makeRelative().toString();
        }
        return "none";
    }

    public IStatus move(IResource source, IResource destination)
    {
        try
        {
            // Sanity check - can't move something that is not part of clearcase
            if (!hasRemote(source))
            {
                return new Status(
                    IStatus.ERROR,
                    ID,
                    TeamException.NO_REMOTE_RESOURCE,
                    MessageFormat.format(
                        "Resource \"{0}\" is not under source control!",
                        new Object[] { source.getFullPath().toString()}),
                    null);
            }

            IStatus result = checkoutParent(source);

            if (result.isOK())
                result = checkoutParent(destination);

            if (result.isOK())
            {
                IClearcase.Status ccStatus =
                    ClearcasePlugin.getEngine().move(
                        source.getLocation().toOSString(),
                        destination.getLocation().toOSString(),
                        getComment());
                if (!ccStatus.status)
                {
                    return new Status(
                        IStatus.ERROR,
                        ID,
                        TeamException.UNABLE,
                        "Could not move element: " + ccStatus.message,
                        null);
                }
                StateCacheFactory.getInstance().remove(source);
                changeState(source.getParent(), IResource.DEPTH_ZERO, null);
                changeState(destination.getParent(), IResource.DEPTH_ZERO, null);
                changeClearcaseState(destination, IResource.DEPTH_ZERO, null);
            }
            return result;
        }
        finally
        {
            setComment("");
        }
    }

    public IStatus checkoutParent(IResource resource)
    {
        IStatus result = OK_STATUS;
        String parent = null;
        // IProject's parent is the workspace directory, we want the filesystem
        // parent if the workspace is not itself in clearcase
        boolean flag = resource instanceof IProject && !hasRemote(resource.getParent());
        if (flag)
        {
            parent = resource.getLocation().toFile().getParent().toString();
        }
        else
        {
            parent = resource.getParent().getLocation().toOSString();
        }

        if (!ClearcasePlugin.getEngine().isElement(parent))
        {
            result =
                new Status(
                    IStatus.ERROR,
                    ID,
                    TeamException.UNABLE,
                    "Could not find a parent that is a clearcase element",
                    null);
            return result;
        }

        if (!ClearcasePlugin.getEngine().isCheckedOut(parent))
        {
            IClearcase.Status ccStatus =
                ClearcasePlugin.getEngine().checkout(
                    parent,
                    getComment(),
                    ClearcasePlugin.isReservedCheckouts(),
                    true);
            if (!flag)
                changeState(resource.getParent(), IResource.DEPTH_ZERO, null);
            if (!ccStatus.status)
            {
                result =
                    new Status(
                        IStatus.ERROR,
                        ID,
                        TeamException.UNABLE,
                        "Could not check out parent: " + ccStatus.message,
                        null);
            }
        }
        return result;
    }

    // Notifies decorator that state has changed for an element
    void changeState(IResource resource, int depth, IProgressMonitor monitor)
    {
        try
        {
            resource.refreshLocal(depth, monitor);
            changeClearcaseState(resource, depth, monitor);
        }
        catch (CoreException ex)
        {
            ClearcasePlugin.log(IStatus.ERROR, "Error refreshing clearcase/resource state", ex);
        }
    }

    private IStatus changeClearcaseState(IResource resource, int depth, IProgressMonitor monitor)
    {
        IStatus result = execute(new IRecursiveOperation()
        {
            public IStatus visit(IResource resource, IProgressMonitor progress)
            {
                Status result = OK_STATUS;
                // probably overkill/expensive to do it here - should do it on a
                // case by case basis for eac method that actually changes state
                StateCache cache = StateCacheFactory.getInstance().get(resource);
                cache.update(false);
                return result;
            }
        }, resource, depth, monitor);
        return result;
    }

    /**
     * @see RepositoryProvider#getSimpleAccess()
     */
    public SimpleAccessOperations getSimpleAccess()
    {
        return this;
    }

    /**
     * @see RepositoryProvider#getMoveDeleteHook()
     */
    public IMoveDeleteHook getMoveDeleteHook()
    {
        return moveHandler;
    }

    /**
     * @see RepositoryProvider#getFileModificationValidator()
     */
    public IFileModificationValidator getFileModificationValidator()
    {
        return modificationValidator;
    }

    /**
     * Gets the comment.
     * @return Returns a String
     */
    public String getComment()
    {
        return comment;
    }

    /**
     * Sets the comment.
     * @param comment The comment to set
     */
    public void setComment(String comment)
    {
        // escape comment if enabled
        if (comment.trim().length() > 0 && ClearcasePlugin.isEscapeComments())
            comment = ClearcaseUtil.getEscaped(comment);

        this.comment = comment;
    }

    // Out of sheer laziness, I appropriated the following code from the team provider example =)

    /**
     * These interfaces are to operations that can be performed on the array of resources,
     * and on all resources identified by the depth parameter.
     * @see execute(IOperation, IResource[], int, IProgressMonitor)
     */
    public static interface IOperation
    {}
    public static interface IIterativeOperation extends IOperation
    {
        public IStatus visit(IResource resource, int depth, IProgressMonitor progress);
    }
    public static interface IRecursiveOperation extends IOperation
    {
        public IStatus visit(IResource resource, IProgressMonitor progress);
    }

    /**
     * Perform the given operation on the array of resources, each to the
     * specified depth.  Throw an exception if a problem ocurs, otherwise
     * remain silent.
     */
    protected void execute(
        IOperation operation,
        IResource[] resources,
        int depth,
        IProgressMonitor progress)
        throws TeamException
    {

        // Create an array to hold the status for each resource.
        MultiStatus multiStatus = new MultiStatus(getID(), TeamException.OK, "OK", null);

        // For each resource in the local resources array until we have errors.
        for (int i = 0; i < resources.length && !multiStatus.matches(IStatus.ERROR); i++)
        {
            if (!isIgnored(resources[i]))
            {
                if (operation instanceof IRecursiveOperation)
                    multiStatus.merge(
                        execute((IRecursiveOperation) operation, resources[i], depth, progress));
                else
                    multiStatus.merge(
                        ((IIterativeOperation) operation).visit(resources[i], depth, progress));
            }
        }

        // Finally, if any problems occurred, throw the exeption with all the statuses,
        // but if there were no problems exit silently.
        if (!multiStatus.isOK())
        {
            String message =
                multiStatus.matches(IStatus.ERROR)
                    ? "There were errors that prevent the requested operation from finishing successfully."
                    : "The requested operation finished with warnings.";
            throw new TeamException(
                new MultiStatus(
                    multiStatus.getPlugin(),
                    multiStatus.getCode(),
                    multiStatus.getChildren(),
                    message,
                    multiStatus.getException()));
        }

        // Cause all the resource changes to be broadcast to listeners.
        //		TeamPlugin.getManager().broadcastResourceStateChanges(resources);
    }

    /**
     * Perform the given operation on a resource to the given depth.
     */
    protected IStatus execute(
        IRecursiveOperation operation,
        IResource resource,
        int depth,
        IProgressMonitor progress)
    {
        // Visit the given resource first.
        IStatus status = operation.visit(resource, progress);

        // If the resource is a file then the depth parameter is irrelevant.
        if (resource.getType() == IResource.FILE)
            return status;

        // If we are not considering any members of the container then we are done.
        if (depth == IResource.DEPTH_ZERO)
            return status;

        // If the operation was unsuccessful, do not attempt to go deep.
        if (status.matches(IStatus.ERROR)) //if (!status.isOK())
            return status;

        // If the container has no children then we are done.
        IResource[] members = getMembers(resource);
        if (members.length == 0)
            return status;

        // There are children and we are going deep, the response will be a multi-status.
        MultiStatus multiStatus =
            new MultiStatus(
                status.getPlugin(),
                status.getCode(),
                status.getMessage(),
                status.getException());

        // The next level will be one less than the current level...
        int childDepth =
            (depth == IResource.DEPTH_ONE) ? IResource.DEPTH_ZERO : IResource.DEPTH_INFINITE;

        // Collect the responses in the multistatus (use merge to flatten the tree).
        for (int i = 0; i < members.length && !multiStatus.matches(IStatus.ERROR); i++)
        {
            if (!isIgnored(members[i]))
                multiStatus.merge(execute(operation, members[i], childDepth, progress));
        }

        // correct the MultiStatus message
        if (!multiStatus.isOK())
        {
            /* Remember: the multi status was created with "OK" as message!
             * This is not meaningful anymore. We have to correct it.
             */
            String message =
                multiStatus.matches(IStatus.ERROR)
                    ? "There were errors that prevent the requested operation from finishing successfully."
                    : "The requested operation finished with warnings.";
            multiStatus =
                new MultiStatus(
                    multiStatus.getPlugin(),
                    multiStatus.getCode(),
                    multiStatus.getChildren(),
                    message,
                    multiStatus.getException());
        }
        return multiStatus;
    }

    protected IResource[] getMembers(IResource resource)
    {
        if (resource.getType() != IResource.FILE)
        {
            try
            {
                return ((IContainer) resource).members();
            }
            catch (CoreException exception)
            {
                exception.printStackTrace();
                throw new RuntimeException();
            }
        } //end-if
        else
            return new IResource[0];
    }

    /**
     * @see org.eclipse.team.core.RepositoryProvider#canHandleLinkedResources()
     */
    public boolean canHandleLinkedResources()
    {
        return true;
    }

    /**
     * Indicates if a resource is ignored and not handled.
     * <p>Resources are never ignored, if they have a remote resource.</p>
     * @param resource
     * @return
     */
    public boolean isIgnored(IResource resource)
    {
        // always ignore eclipse linked resource
        String linkedParentName = resource.getProjectRelativePath().segment(0);
        if(null != linkedParentName)
        {
            IFolder linkedParent = resource.getProject().getFolder(linkedParentName);
            if (linkedParent.isLinked())
                return true;
        }

        // never ignore handled resources
        if (hasRemote(resource))
            return false;

        // never ignore workspace root
        if (null == resource.getParent())
            return false;

        // check the global ignores from Team (includes derived resources)
        if (Team.isIgnoredHint(resource))
            return true;

        // check the parent, if the parent is ignored 
        // then this resource is ignored also
        return isIgnored(resource.getParent());
    }

}
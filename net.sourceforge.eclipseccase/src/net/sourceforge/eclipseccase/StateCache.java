
package net.sourceforge.eclipseccase;

import java.io.IOException;
import java.io.Serializable;

import net.sourceforge.clearcase.simple.ClearcaseUtil;
import net.sourceforge.clearcase.simple.IClearcase;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

public class StateCache implements Serializable
{
    static final long serialVersionUID = -7439899000320633901L;

    private String osPath;

    private String workspaceResourcePath;

    private transient IResource resource;

    private boolean uninitialized = true;

    private boolean hasRemote = false;

    private boolean isCheckedOut = false;

    private boolean isSnapShot = false;

    private boolean isSymbolicLink = false;

    private boolean isHijacked = false;

    private String version = "";

    StateCache(IResource resource)
    {
        if (null == resource)
                throw new IllegalArgumentException("Resource must not be null!");

        this.resource = resource;

        IPath location = resource.getLocation();
        if (location != null)
        {
            osPath = location.toOSString();
        }
        else
        {
            // resource has been invalidated in the workspace since request was
            // queued, so ignore update request.
            osPath = null;
        }
    }

    private static final String DEBUG_ID = "StateCache";

    private String symbolicLinkTarget = "";

    /**
     * @param hidden
     */
    public synchronized void updateAsync(boolean hidden)
    {
        if (!hidden)
        {
            uninitialized = true;
            ClearcasePlugin.debug(DEBUG_ID, "invalidating " + this);
        }
        RefreshStateJob job = new RefreshStateJob(this, hidden);
        job.schedule();
    }

    synchronized void doUpdate()
    {
        boolean changed = uninitialized;

        IPath location = resource.getLocation();
        if (location == null)
        {
            // resource has been invalidated in the workspace since request was
            // queued, so ignore update request.
            ClearcasePlugin.debug(DEBUG_ID, "not updating - invalid resource: "
                    + resource);
            return;
        }
        osPath = location.toOSString();

        if (resource.isAccessible())
        {
            boolean newHasRemote = ClearcasePlugin.getEngine()
                    .isElement(osPath);
            changed = changed || newHasRemote != this.hasRemote;
            this.hasRemote = newHasRemote;

            boolean newIsSymbolicLink = newHasRemote
                    && ClearcasePlugin.getEngine().isSymbolicLink(osPath);
            changed = changed || newIsSymbolicLink != this.isSymbolicLink;
            this.isSymbolicLink = newIsSymbolicLink;

            boolean newIsCheckedOut = newHasRemote
                    && ClearcasePlugin.getEngine().isCheckedOut(osPath);
            changed = changed || newIsCheckedOut != this.isCheckedOut;
            this.isCheckedOut = newIsCheckedOut;

            boolean newIsSnapShot = newHasRemote
                    && ClearcasePlugin.getEngine().isSnapShot(osPath);
            changed = changed || newIsSnapShot != this.isSnapShot;
            this.isSnapShot = newIsSnapShot;

            boolean newIsHijacked = newIsSnapShot
                    && ClearcasePlugin.getEngine().isHijacked(osPath);
            changed = changed || newIsHijacked != this.isHijacked;
            this.isHijacked = newIsHijacked;

            if (newHasRemote)
            {
                String newVersion = ClearcasePlugin.getEngine().cleartool(
                        "describe -fmt " + ClearcaseUtil.quote("%Vn") + " "
                                + ClearcaseUtil.quote(osPath)).message.trim()
                        .replace('\\', '/');
                changed = changed || !newVersion.equals(this.version);
                this.version = newVersion;
            }

            if (newIsSymbolicLink)
            {
                IClearcase.Status status = ClearcasePlugin.getEngine()
                        .getSymbolicLinkTarget(osPath);
                if (status.status)
                {
                    String newTarget = status.message;
                    changed = changed
                            || !newTarget.equals(this.symbolicLinkTarget);
                    this.symbolicLinkTarget = newTarget;
                }
            }
        }
        else
        {
            // resource does not exists
            hasRemote = false;
            isCheckedOut = false;
            isSnapShot = false;
            isHijacked = false;
            isSymbolicLink = false;
            version = "";
            symbolicLinkTarget = "";
            changed = true;
            ClearcasePlugin.debug(DEBUG_ID, "resource not accessible: "
                    + resource);
        }

        uninitialized = false;

        if (changed)
        {
            ClearcasePlugin.debug(DEBUG_ID, "updated " + this);
            StateCacheFactory.getInstance().fireStateChanged(this);
        }
    }

    /**
     * Gets the hasRemote.
     * 
     * @return Returns a boolean
     */
    public boolean hasRemote()
    {
        return hasRemote;
    }

    /**
     * Gets the isCheckedOut.
     * 
     * @return Returns a boolean
     */
    public boolean isCheckedOut()
    {
        return isCheckedOut;
    }

    /**
     * Gets the isDirty.
     * 
     * @return Returns a boolean
     */
    public boolean isDirty()
    {
        if (osPath == null) return false;

        try
        {
            return ClearcasePlugin.getEngine().isDifferent(osPath);
        }
        catch (RuntimeException ex)
        {
            ClearcasePlugin.log(IStatus.ERROR,
                    "Could not determine element dirty state of "
                            + osPath
                            + ": "
                            + (null != ex.getCause() ? ex.getCause()
                                    .getMessage() : ex.getMessage()), ex);
            return false;
        }
    }

    /**
     * Returns the osPath.
     * 
     * @return String
     */
    public String getPath()
    {
        return osPath;
    }

    /**
     * Returns the version.
     * 
     * @return String
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Returns the predecessor version.
     * 
     * @return String
     */
    public String getPredecessorVersion()
    {
        String predecessorVersion = null;

        IClearcase.Status status = (isHijacked ? ClearcasePlugin.getEngine()
                .cleartool(
                        "ls "
                                + ClearcaseUtil.quote(resource.getLocation()
                                        .toOSString())) : ClearcasePlugin
                .getEngine().cleartool(
                        "describe -fmt %PVn "
                                + ClearcaseUtil.quote(resource.getLocation()
                                        .toOSString())));
        if (status.status)
        {
            predecessorVersion = status.message.trim().replace('\\', '/');
            if (isHijacked)
            {
                int offset = predecessorVersion.indexOf("@@") + 2;
                int cutoff = predecessorVersion.indexOf("[hijacked]") - 1;
                try
                {
                    predecessorVersion = predecessorVersion.substring(offset,
                            cutoff);
                }
                catch (Exception e)
                {
                    predecessorVersion = null;
                }
            }
        }

        return predecessorVersion;
    }

    /**
     * Returns the uninitialized.
     * 
     * @return boolean
     */
    public boolean isUninitialized()
    {
        return uninitialized;
    }

    /**
     * Returns the isHijacked.
     * 
     * @return boolean
     */
    public boolean isHijacked()
    {
        return isHijacked;
    }

    /**
     * Returns the isSnapShot.
     * 
     * @return boolean
     */
    public boolean isSnapShot()
    {
        return isSnapShot;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException
    {
        // special handling for resource
        if (null != resource)
        {
            // make sure we only save states for real resources
            if (resource.isAccessible())
            {
                this.workspaceResourcePath = resource.getFullPath().toString();
            }
            else
            {
                this.workspaceResourcePath = null;
            }
        }
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException
    {
        in.defaultReadObject();

        // restore resource
        if (null != workspaceResourcePath)
        {
            // determine resource
            IPath path = new Path(workspaceResourcePath);
            resource = ResourcesPlugin.getWorkspace().getRoot()
                    .findMember(path);
            if (resource != null && resource.isAccessible())
            {
                IPath location = resource.getLocation();
                if (location != null)
                {
                    osPath = location.toOSString();
                }
                else
                {
                    // resource has been invalidated in the workspace since
                    // request was
                    // queued, so ignore update request.
                    osPath = null;
                }
            }
            else
            {
                // invalid resource
                resource = null;
                osPath = null;
                workspaceResourcePath = null;
            }
        }
        else
        {
            // invalid resource
            resource = null;
            osPath = null;
            workspaceResourcePath = null;
        }
    }

    /**
     * Returns the resource.
     * 
     * @return IResource
     */
    public IResource getResource()
    {
        return resource;
    }

    private static class RefreshStateJob extends WorkspaceJob
    {
        private StateCache cache;

        /**
         * Creates a new instance.
         * 
         * @param name
         */
        public RefreshStateJob(StateCache cache, boolean hidden)
        {
            super("Refreshing element state of "
                    + cache.getResource().getFullPath());
            this.cache = cache;
            setSystem(hidden);

            // lock as marker change (don't need to lock complete resource)
            setRule(ResourcesPlugin.getWorkspace().getRuleFactory().markerRule(
                    cache.getResource()));

            setPriority(DECORATE);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core.runtime.IProgressMonitor)
         */
        public IStatus runInWorkspace(IProgressMonitor monitor)
                throws CoreException
        {
            cache.doUpdate();
            return Status.OK_STATUS;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer toString = new StringBuffer("StateCache ");
        toString.append(resource);
        toString.append(": ");
        if (uninitialized)
        {
            toString.append("not initialized");
        }
        else if (!hasRemote)
        {
            toString.append("no clearcase element");
        }
        else if (hasRemote)
        {
            toString.append(version);

            if (isSymbolicLink)
            {
                toString.append(" [SYMBOLIC LINK (");
                toString.append(symbolicLinkTarget);
                toString.append(")]");
            }

            if (isCheckedOut) toString.append(" [CHECKED OUT]");

            if (isDirty()) toString.append(" [DIRTY]");

            if (isHijacked) toString.append(" [HIJACKED]");

            if (isSnapShot) toString.append(" [SNAPSHOT]");
        }
        else
        {
            toString.append("invalid");
        }

        return toString.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        if (null == resource) return 0;

        return resource.hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if (this == obj) return true;

        if (null == obj || StateCache.class != obj.getClass()) return false;

        if (null == resource) return null == ((StateCache) obj).resource;

        return resource.equals(((StateCache) obj).resource);
    }

    /**
     * @return
     */
    public boolean isSymbolicLink()
    {
        return isSymbolicLink;
    }

    /**
     * Returns the symbolicLinkTarget.
     * 
     * @return returns the symbolicLinkTarget
     */
    public String getSymbolicLinkTarget()
    {
        return symbolicLinkTarget;
    }

    /**
     * @return
     */
    public boolean isSymbolicLinkTargetValid()
    {
        if (osPath == null) return false;

        try
        {
            return ClearcasePlugin.getEngine()
                    .isSymbolicLinkTargetValid(osPath);
        }
        catch (RuntimeException ex)
        {
            ClearcasePlugin.log(IStatus.ERROR,
                    "Could not determine link target of "
                            + osPath
                            + ": "
                            + (null != ex.getCause() ? ex.getCause()
                                    .getMessage() : ex.getMessage()), ex);
            return false;
        }
    }
}
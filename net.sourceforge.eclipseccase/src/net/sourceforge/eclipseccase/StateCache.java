package net.sourceforge.eclipseccase;

import java.io.IOException;
import java.io.Serializable;

import net.sourceforge.clearcase.simple.ClearcaseUtil;
import net.sourceforge.clearcase.simple.IClearcase;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class StateCache implements Serializable
{
    static final long serialVersionUID = -7439899000320633901L;

    private String osPath;
    private String workspaceResourcePath;

    public static final String STATE_CHANGE_MARKER_TYPE =
        "net.sourceforge.eclipseccase.statechangedmarker";
    private transient IResource resource;
    private boolean uninitialized = true;
    private boolean hasRemote = false;
    private boolean isCheckedOut = false;
    private boolean isSnapShot = false;
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

    public synchronized void updateAsync()
    {
        updateAsync(false, false);
    }

    private static final String DEBUG_ID = "StateCache";

    public synchronized void updateAsync(boolean quick, boolean asap)
    {
        if (!quick)
        {
            uninitialized = true;
            ClearcasePlugin.debug(DEBUG_ID, "invalidating " + this);
        }
        Runnable cmd = new UpdateCacheCommand(this, quick);
        UpdateQueue queue = UpdateQueue.getInstance();
        if (asap)
            queue.addFirst(cmd);
        else
            queue.add(cmd);
    }

    private synchronized void doUpdate()
    {
        boolean changed = uninitialized;

        IPath location = resource.getLocation();
        if (location == null)
        {
            // resource has been invalidated in the workspace since request was
            // queued, so ignore update request.
            ClearcasePlugin.debug(DEBUG_ID, "not updating - invalid resource: " + resource);
            return;
        }
        osPath = location.toOSString();

        if (resource.isAccessible())
        {
            boolean hasRemote = ClearcasePlugin.getEngine().isElement(osPath);
            changed = changed || hasRemote != this.hasRemote;
            this.hasRemote = hasRemote;

            boolean isCheckedOut = hasRemote && ClearcasePlugin.getEngine().isCheckedOut(osPath);
            changed = changed || isCheckedOut != this.isCheckedOut;
            this.isCheckedOut = isCheckedOut;

            boolean isSnapShot = hasRemote && ClearcasePlugin.getEngine().isSnapShot(osPath);
            changed = changed || isSnapShot != this.isSnapShot;
            this.isSnapShot = isSnapShot;

            boolean isHijacked = isSnapShot && ClearcasePlugin.getEngine().isHijacked(osPath);
            changed = changed || isHijacked != this.isHijacked;
            this.isHijacked = isHijacked;

            if (hasRemote)
            {
                String version =
                    ClearcasePlugin
                        .getEngine()
                        .cleartool(
                            "describe -fmt "
                                + ClearcaseUtil.quote("%Vn")
                                + " "
                                + ClearcaseUtil.quote(osPath))
                        .message
                        .trim()
                        .replace('\\', '/');
                changed = changed || !version.equals(this.version);
                this.version = version;
            }
        }
        else
        {
            // resource does not exists
            hasRemote = false;
            isCheckedOut = false;
            isSnapShot = false;
            isHijacked = false;
            version = "";
            changed = true;
            ClearcasePlugin.debug(DEBUG_ID, "resource not accessible: " + resource);
        }

        uninitialized = false;

        if (changed)
        {
            ClearcasePlugin.debug(DEBUG_ID, "updated " + this);
            StateCacheFactory.getInstance().fireStateChanged(this);
        }
    }

    /**
     * Updates the cache.  If quick is false, an update is always performed.  If
     * quick is true, and update is only performed if the file's readonly status
     * differs from what it should be according to the state.
     */
    public void update(boolean quick)
    {
        if (quick && !uninitialized)
        {
            if (resource.getType() != IResource.FILE
                || (resource.isReadOnly() == (isCheckedOut || !hasRemote || isHijacked)))
            {
                doUpdate();
            }
        }
        else
        {
            doUpdate();
        }
    }

    /**
     * Gets the hasRemote.
     * @return Returns a boolean
     */
    public boolean hasRemote()
    {
        return hasRemote;
    }

    /**
     * Gets the isCheckedOut.
     * @return Returns a boolean
     */
    public boolean isCheckedOut()
    {
        return isCheckedOut;
    }

    /**
     * Gets the isDirty.
     * @return Returns a boolean
     */
    public boolean isDirty()
    {
        if (osPath == null)
            return false;

        return ClearcasePlugin.getEngine().isDifferent(osPath);
    }

    /**
     * Returns the osPath.
     * @return String
     */
    public String getPath()
    {
        return osPath;
    }

    /**
     * Returns the version.
     * @return String
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Returns the predecessor version.
     * @return String
     */
    public String getPredecessorVersion()
    {
        String version = null;

        IClearcase.Status status =
            (isHijacked
                ? ClearcasePlugin.getEngine().cleartool(
                    "ls " + ClearcaseUtil.quote(resource.getLocation().toOSString()))
                : ClearcasePlugin.getEngine().cleartool(
                    "describe -fmt %PVn "
                        + ClearcaseUtil.quote(resource.getLocation().toOSString())));
        if (status.status)
        {
            version = status.message.trim().replace('\\', '/');
            if (isHijacked)
            {
                int offset = version.indexOf("@@") + 2;
                int cutoff = version.indexOf("[hijacked]") - 1;
                try
                {
                    version = version.substring(offset, cutoff);
                }
                catch (Exception e)
                {
                    version = null;
                }
            }
        }

        return version;
    }

    /**
     * Returns the uninitialized.
     * @return boolean
     */
    public boolean isUninitialized()
    {
        return uninitialized;
    }

    /**
     * Returns the isHijacked.
     * @return boolean
     */
    public boolean isHijacked()
    {
        return isHijacked;
    }

    /**
     * Returns the isSnapShot.
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

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();

        // restore resource
        if (null != workspaceResourcePath)
        {
            // determine resource
            IPath path = new Path(workspaceResourcePath);
            resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
            if (resource != null && resource.isAccessible())
            {
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
     * @return IResource
     */
    public IResource getResource()
    {
        return resource;
    }

    private static class UpdateCacheCommand implements Runnable
    {
        StateCache cache;
        boolean quick = false;

        UpdateCacheCommand(StateCache cache, boolean quick)
        {
            this.cache = cache;
            this.quick = quick;
        }

        public void run()
        {
            cache.update(quick);
        }

        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;

            if (null == obj || obj.getClass() != UpdateCacheCommand.class)
                return false;

            return cache.equals(((UpdateCacheCommand) obj).cache);
        }
        public int hashCode()
        {
            return cache.hashCode();
        }
    }

    /* (non-Javadoc)
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

            if (isCheckedOut)
                toString.append(" [CHECKED OUT]");

            if (isDirty())
                toString.append(" [DIRTY]");

            if (isHijacked)
                toString.append(" [HIJACKED]");

            if (isSnapShot)
                toString.append(" [SNAPSHOT]");
        }
        else
        {
            toString.append("invalid");
        }

        return toString.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        if (null == resource)
            return 0;

        return resource.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;

        if (null == obj || StateCache.class != obj.getClass())
            return false;

        if (null == resource)
            return null == ((StateCache) obj).resource;

        return resource.equals(((StateCache) obj).resource);
    }

}
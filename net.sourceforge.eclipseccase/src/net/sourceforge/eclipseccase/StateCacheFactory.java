
package net.sourceforge.eclipseccase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.TeamException;

public class StateCacheFactory implements ISaveParticipant,
        IResourceChangeListener
{
    private static final String SAVE_FILE_NAME = "statecache";

    private static StateCacheFactory instance = new StateCacheFactory();

    HashMap cacheMap = new HashMap();

    private List listeners = new ArrayList();

    private StateCacheFactory()
    {}

    public static StateCacheFactory getInstance()
    {
        return instance;
    }

    public void addStateChangeListerer(StateChangeListener listener)
    {
        if (null != listener)
        {
            synchronized (listeners)
            {
                if (!listeners.contains(listener)) listeners.add(listener);
            }
        }
    }

    public boolean removeStateChangeListerer(StateChangeListener listener)
    {
        if (null != listener)
        {
            synchronized (listeners)
            {
                return listeners.remove(listener);
            }
        }
        return false;
    }

    public void fireStateChanged(StateCache stateCache)
    {
        if (null == stateCache || listeners.isEmpty()) return;

        Object[] currentListeners = null;
        synchronized (listeners)
        {
            currentListeners = listeners.toArray();
        }
        if (null != currentListeners)
        {
            for (int i = 0; i < currentListeners.length; i++)
            {
                ((StateChangeListener) currentListeners[i])
                        .stateChanged(stateCache);
            }
        }
    }

    public synchronized boolean isUnitialized(IResource resource)
    {
        StateCache cache = (StateCache) cacheMap.get(resource);
        if (cache == null) return true;
        else
            return cache.isUninitialized();
    }

    public synchronized StateCache get(IResource resource)
    {
        StateCache cache = (StateCache) cacheMap.get(resource);
        if (cache == null)
        {
            cache = new StateCache(resource);

            // sanity check for bogus cache (there is a bug with wrong states)
            if (cacheMap.containsValue(cache))
            {
                String message = MessageFormat
                        .format(
                                "A state cache for resource \"{0}\" already exists but was not found in the map!",
                                new Object[]{resource});
                IllegalStateException e = new IllegalStateException(message);
                ClearcasePlugin.log(IStatus.ERROR,
                        "Error while creating state cache!", e);
                throw e;
            }

            cache.updateAsync(true);
            cacheMap.put(resource, cache);
        }
        else if (cache.isUninitialized())
        {
            // do not update here, I found a threading issue where the same
            // resource is updated
            // again and again because the decorator thread started decoration
            // which
            // caused this methode to get called
            //cache.updateAsync();
        }
        return cache;
    }

    public synchronized void set(IResource resource, StateCache cache)
    {
        cacheMap.put(resource, cache);
    }

    public synchronized void remove(IResource resource)
    {
        try
        {
            resource.accept(new IResourceVisitor()
            {
                public boolean visit(IResource childResource)
                        throws CoreException
                {
                    switch (childResource.getType())
                    {
                        case IResource.PROJECT:
                        case IResource.FOLDER:
                            cacheMap.remove(childResource);
                            return true;

                        default:
                            cacheMap.remove(childResource);
                            return false;
                    }
                }
            });
        }
        catch (CoreException ex)
        {
            ex.printStackTrace();
        }
        cacheMap.remove(resource);
    }

    /**
     * @see org.eclipse.core.resources.ISaveParticipant#doneSaving(ISaveContext)
     */
    public void doneSaving(ISaveContext context)
    {
        int previousSaveNumber = context.getPreviousSaveNumber();
        String oldFileName = SAVE_FILE_NAME
                + Integer.toString(previousSaveNumber);
        File file = ClearcasePlugin.getDefault().getStateLocation().append(
                oldFileName).toFile();
        file.delete();
    }

    /**
     * @see org.eclipse.core.resources.ISaveParticipant#prepareToSave(ISaveContext)
     */
    public void prepareToSave(ISaveContext context) throws CoreException
    {}

    /**
     * @see org.eclipse.core.resources.ISaveParticipant#rollback(ISaveContext)
     */
    public void rollback(ISaveContext context)
    {}

    /**
     * @see org.eclipse.core.resources.ISaveParticipant#saving(ISaveContext)
     */
    public void saving(ISaveContext context) throws CoreException
    {
        switch (context.getKind())
        {
            case ISaveContext.FULL_SAVE:
                try
                {
                    int saveNumber = context.getSaveNumber();
                    String saveFileName = SAVE_FILE_NAME
                            + Integer.toString(saveNumber);
                    IPath statePath = ClearcasePlugin.getDefault()
                            .getStateLocation().append(saveFileName);
                    if (ClearcasePlugin.isPersistState())
                    {
                        ObjectOutputStream os = new ObjectOutputStream(
                                new FileOutputStream(statePath.toFile()));
                        Collection serList = new LinkedList(cacheMap.values());
                        os.writeObject(serList);
                        os.flush();
                        os.close();
                        context.map(new Path(SAVE_FILE_NAME), new Path(
                                saveFileName));
                        context.needSaveNumber();
                    }
                }
                catch (IOException ex)
                {
                    throw new CoreException(new Status(Status.WARNING,
                            ClearcasePlugin.PLUGIN_ID, TeamException.IO_FAILED,
                            "Could not persist state cache", ex));
                }
                break;
            case ISaveContext.PROJECT_SAVE:
                break;
            case ISaveContext.SNAPSHOT:
                break;
        }
    }

    void load(ISavedState context)
    {
        try
        {
            if (context != null && ClearcasePlugin.isPersistState())
            {
                String saveFileName = context.lookup(new Path(SAVE_FILE_NAME))
                        .toString();
                File stateFile = ClearcasePlugin.getDefault()
                        .getStateLocation().append(saveFileName).toFile();
                if (stateFile.exists())
                {
                    ObjectInputStream is = new ObjectInputStream(
                            new FileInputStream(stateFile));
                    Collection values = (Collection) is.readObject();
                    for (Iterator iter = values.iterator(); iter.hasNext();)
                    {
                        StateCache element = (StateCache) iter.next();
                        IResource resource = element.getResource();
                        if (resource != null && resource.isAccessible())
                        {
                            cacheMap.put(resource, element);
                        }
                        //else
                        //{
                        //	ClearcasePlugin.log(Status.WARNING, "Loaded an
                        // invalid cache entry from persistent state cache,
                        // ignoring...", null);
                        //}
                    }
                    is.close();
                }
            }
        }
        catch (Exception ex)
        {
            ClearcasePlugin
                    .log(
                            Status.WARNING,
                            "Could not load saved clearcase state cache, resetting cache",
                            ex);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
     */
    public void resourceChanged(IResourceChangeEvent event)
    {
        try
        {
            final boolean refresh = ClearcasePlugin.isRefreshOnChange();
            final List toRefresh = new ArrayList();
            event.getDelta().accept(new IResourceDeltaVisitor()
            {
                public boolean visit(IResourceDelta delta) throws CoreException
                {
                    IResource resource = delta.getResource();
                    switch (delta.getKind())
                    {
                        case IResourceDelta.REMOVED:
                            remove(resource);
                            break;

                        default:
                            if (refresh) toRefresh.add(resource);
                    }

                    switch (resource.getType())
                    {
                        case IResource.PROJECT:
                        case IResource.FOLDER:
                            return true;
                    }
                    return false;
                }
            });

            if (refresh && !toRefresh.isEmpty())
            {
                for (Iterator resources = toRefresh.iterator(); resources
                        .hasNext();)
                {
                    IResource resourceToRefresh = (IResource) resources.next();
                    StateCache cache = StateCacheFactory.getInstance().get(
                            resourceToRefresh);
                    cache.updateAsync(true);
                }
            }
        }
        catch (CoreException e)
        {
            ClearcasePlugin.log(IStatus.ERROR,
                    "Unable to do a quick update of resource", null);
        }
    }
}
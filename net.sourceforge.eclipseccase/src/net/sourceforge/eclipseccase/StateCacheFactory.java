/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Matthew Conway - initial API and implementation IBM Corporation -
 * concepts and ideas from Eclipse Gunnar Wagenknecht - new features,
 * enhancements and bug fixes
 ******************************************************************************/
package net.sourceforge.eclipseccase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
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
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.TeamException;

public class StateCacheFactory implements ISaveParticipant,
        IResourceChangeListener {

    private static final String SAVE_FILE_NAME = "statecache";

    /** the singleton instance */
    private static StateCacheFactory instance = new StateCacheFactory();

    /** maps resources to caches */
    HashMap cacheMap = new HashMap();

    /** the listeners */
    private List listeners = new ArrayList();

    /**
     * Hidden constructor.
     */
    private StateCacheFactory() {
        // super
    }

    /**
     * Returns the shared instance.
     * 
     * @return
     */
    public static StateCacheFactory getInstance() {
        return instance;
    }

    /**
     * Adds a state change listener.
     * 
     * @param listener
     */
    public void addStateChangeListerer(IResourceStateListener listener) {
        if (null != listener) {
            synchronized (listeners) {
                if (!listeners.contains(listener)) listeners.add(listener);
            }
        }
    }

    /**
     * Removes a state change listener.
     * 
     * @param listener
     * @return
     */
    public boolean removeStateChangeListerer(IResourceStateListener listener) {
        if (null != listener) {
            synchronized (listeners) {
                return listeners.remove(listener);
            }
        }
        return false;
    }

    /**
     * Fires a state change for the specified state cache.
     * 
     * @param stateCache
     */
    public void fireStateChanged(IResource resource) {

        if (operationCounter > 0) {

            // only queue if there are ongoing operations
            if (null == resource) return;
            synchronized (queuedEvents) {
                queuedEvents.addLast(resource);
            }
        } else {

            // fire event or queued events
            Object[] currentListeners = null;
            synchronized (listeners) {
                currentListeners = listeners.toArray();
            }

            if (null != currentListeners) {
                if (null == resource) {

                    // fire all pending changes
                    Object[] events = null;
                    synchronized (queuedEvents) {
                        events = queuedEvents.toArray();
                        queuedEvents.clear();
                    }
                    if (null != events) {
                        for (int i = 0; i < events.length; i++) {
                            resource = (IResource) events[i];
                            for (int j = 0; j < currentListeners.length; j++) {
                                ((IResourceStateListener) currentListeners[j])
                                        .resourceStateChanged(resource);
                            }
                        }
                    }
                } else {

                    // fire only the given change
                    for (int i = 0; i < currentListeners.length; i++) {
                        ((IResourceStateListener) currentListeners[i])
                                .resourceStateChanged(resource);
                    }
                }
            }
        }
    }

    /** a list of events queued during long running operations */
    private LinkedList queuedEvents = new LinkedList();

    /** the operation counter */
    private int operationCounter = 0;

    /**
     * Starts an operation.
     */
    public void operationBegin() {
        synchronized (this) {
            operationCounter++;
        }
    }

    /**
     * Ends an operation.
     */
    public void operationEnd() {
        boolean fire = false;
        synchronized (this) {
            if (operationCounter == 0) return;

            operationCounter--;

            fire = operationCounter == 0;
        }

        if (fire) {
            fireStateChanged(null);
        }
    }

    /**
     * Indicates if the state of the specified resource is uninitialized.
     * 
     * @param resource
     * @return <code>true</code> if uninitialized
     */
    public boolean isUnitialized(IResource resource) {
        if (!cacheMap.containsKey(resource)) return true;

        return ((StateCache) cacheMap.get(resource)).isUninitialized();
    }

    /**
     * Returns the state cache fo the specified resource.
     * 
     * @param resource
     * @return the state cache fo the specified resource
     */
    public StateCache get(IResource resource) {
        StateCache cache = (StateCache) cacheMap.get(resource);
        if (cache == null) {

            synchronized (cacheMap) {
                cache = (StateCache) cacheMap.get(resource);
                if (null == cache) {
                    cache = new StateCache(resource);
                    cacheMap.put(resource, cache);
                }
            }

            // schedule update if necessary
            if (cache.isUninitialized()) cache.updateAsync(true);
        }
        return cache;
    }

    /**
     * Removes the state cache for the specified resource including all its
     * direct and indirect members.
     * 
     * @param resource
     */
    public void remove(IResource resource) {
        if (resource.isAccessible()) {
            try {
                resource.accept(new IResourceVisitor() {

                    public boolean visit(IResource childResource)
                            throws CoreException {
                        switch (childResource.getType()) {
                        case IResource.PROJECT:
                        case IResource.FOLDER:
                            removeSingle(childResource);
                            return true;
                        default:
                            removeSingle(childResource);
                            return false;
                        }
                    }
                });
            } catch (CoreException ex) {
                // not accessible
            }
        }
        removeSingle(resource);
    }

    /**
     * Removes the state cache for the specified resource.
     * 
     * @param resource
     */
    void removeSingle(IResource resource) {
        if (cacheMap.containsKey(resource)) {
            synchronized (cacheMap) {
                cacheMap.remove(resource);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.ISaveParticipant#doneSaving(org.eclipse.core.resources.ISaveContext)
     */
    public void doneSaving(ISaveContext context) {
        int previousSaveNumber = context.getPreviousSaveNumber();
        String oldFileName = SAVE_FILE_NAME
                + Integer.toString(previousSaveNumber);
        File file = ClearcasePlugin.getInstance().getStateLocation().append(
                oldFileName).toFile();
        file.delete();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.ISaveParticipant#prepareToSave(org.eclipse.core.resources.ISaveContext)
     */
    public void prepareToSave(ISaveContext context) throws CoreException {
        // prepareToSave
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.ISaveParticipant#rollback(org.eclipse.core.resources.ISaveContext)
     */
    public void rollback(ISaveContext context) {
        // rollback
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.ISaveParticipant#saving(org.eclipse.core.resources.ISaveContext)
     */
    public void saving(ISaveContext context) throws CoreException {
        switch (context.getKind()) {

        case ISaveContext.FULL_SAVE:
            try {
                int saveNumber = context.getSaveNumber();
                String saveFileName = SAVE_FILE_NAME
                        + Integer.toString(saveNumber);
                IPath statePath = ClearcasePlugin.getInstance()
                        .getStateLocation().append(saveFileName);

                // save state cache
                ObjectOutputStream os = new ObjectOutputStream(
                        new FileOutputStream(statePath.toFile()));
                Collection serList = new LinkedList(cacheMap.values());
                os.writeObject(serList);
                os.flush();
                os.close();
                context.map(new Path(SAVE_FILE_NAME), new Path(saveFileName));
                context.needSaveNumber();
            } catch (IOException ex) {
                throw new CoreException(new Status(IStatus.WARNING,
                        ClearcasePlugin.PLUGIN_ID, TeamException.IO_FAILED,
                        "Could not persist state cache", ex));
            }
            break;

        case ISaveContext.PROJECT_SAVE:
        case ISaveContext.SNAPSHOT:
            break;
        }
    }

    /**
     * Loads the state cache from the specified context.
     * 
     * @param context
     */
    void load(ISavedState context) {
        try {
            if (context != null) {
                String saveFileName = context.lookup(new Path(SAVE_FILE_NAME))
                        .toString();
                File stateFile = ClearcasePlugin.getInstance()
                        .getStateLocation().append(saveFileName).toFile();
                if (stateFile.exists()) {
                    ObjectInputStream is = new ObjectInputStream(
                            new FileInputStream(stateFile));
                    Collection values = (Collection) is.readObject();
                    for (Iterator iter = values.iterator(); iter.hasNext();) {
                        StateCache element = (StateCache) iter.next();
                        IResource resource = element.getResource();
                        if (resource != null && resource.isAccessible()) {
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
        } catch (Exception ex) {
            ClearcasePlugin
                    .log(
                            IStatus.WARNING,
                            "Could not load saved clearcase state cache, resetting cache",
                            ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
     */
    public void resourceChanged(IResourceChangeEvent event) {

        try {
            IResourceDelta rootDelta = event.getDelta();
            if (null != rootDelta) {
                IResourceDelta[] projectDeltas = rootDelta
                        .getAffectedChildren();

                // determine resources to refresh
                final List toRefresh = new ArrayList();

                for (int i = 0; i < projectDeltas.length; i++) {
                    IResourceDelta projectDelta = projectDeltas[i];

                    // filter only shared projects
                    if (RepositoryProvider.isShared((IProject) projectDelta
                            .getResource())) {
                        if (!isAffectedBy(rootDelta)) continue;

                        projectDelta.accept(new IResourceDeltaVisitor() {

                            public boolean visit(IResourceDelta delta)
                                    throws CoreException {
                                switch (delta.getKind()) {
                                case IResourceDelta.REMOVED:
                                    // only remove cache
                                    removeSingle(delta.getResource());
                                    break;

                                default:
                                    if (needsRefresh(delta)) {
                                        // refresh cache
                                        toRefresh.add(delta.getResource());
                                    }
                                }

                                return true;
                            }
                        });
                    }
                }

                if (!toRefresh.isEmpty()) {
                    for (Iterator resources = toRefresh.iterator(); resources
                            .hasNext();) {
                        IResource resourceToRefresh = (IResource) resources
                                .next();
                        // schedule hidden update
                        StateCache cache = StateCacheFactory.getInstance().get(
                                resourceToRefresh);
                        cache.updateAsync(true);
                    }
                }
            }
        } catch (CoreException e) {
            ClearcasePlugin.log(IStatus.ERROR,
                    "Unable to do a quick update of resource", e);
        }
    }

    /**
     * Indicates if the resource delta is really interesting for a refresh.
     * 
     * @param delta
     * @return
     */
    static boolean needsRefresh(IResourceDelta delta) {
        IResource resource = delta.getResource();

        // ignore linked folders
        if (resource.isLinked()) return false;

        // check the global ignores from Team (includes derived resources)
        if (Team.isIgnoredHint(resource)) return false;

        int interestingChangeFlags = IResourceDelta.CONTENT
                | IResourceDelta.SYNC | IResourceDelta.REPLACED
                | IResourceDelta.DESCRIPTION | IResourceDelta.OPEN
                | IResourceDelta.TYPE;

        if (delta.getKind() == IResourceDelta.ADDED
                || (delta.getKind() == IResourceDelta.CHANGED && 0 != (delta
                        .getFlags() & interestingChangeFlags))) {

            //System.out
            //        .println(((org.eclipse.core.internal.events.ResourceDelta) delta)
            //                .toDebugString());

            // only refresh if current state is not checked out
            return !getInstance().isUnitialized(resource)
                    && !getInstance().get(resource).isCheckedOut();
        }

        //System.out.println("ignored: " + delta);
        return false;
    }

    /**
     * Returns whether a given delta contains some information relevant to the
     * resource state, in particular it will not consider MARKER only deltas.
     */
    static boolean isAffectedBy(IResourceDelta rootDelta) {
        //if (rootDelta == null) System.out.println("NULL DELTA");
        //long start = System.currentTimeMillis();
        if (rootDelta != null) {
            // use local exception to quickly escape from delta traversal
            class FoundRelevantDeltaException extends RuntimeException {
                // empty
            }
            try {
                rootDelta.accept(new IResourceDeltaVisitor() {

                    public boolean visit(IResourceDelta delta)
                            throws CoreException {
                        switch (delta.getKind()) {
                        case IResourceDelta.ADDED:
                        case IResourceDelta.REMOVED:
                            throw new FoundRelevantDeltaException();
                        case IResourceDelta.CHANGED:
                            // if any flag is set but MARKER, this delta should
                            // be considered
                            if (delta.getAffectedChildren().length == 0 // only
                                    // check
                                    // leaf
                                    // delta
                                    // nodes
                                    && (delta.getFlags() & ~IResourceDelta.MARKERS) != 0) { throw new FoundRelevantDeltaException(); }
                        }
                        return true;
                    }
                });
            } catch (FoundRelevantDeltaException e) {
                //System.out.println("RELEVANT DELTA detected in: "+
                // (System.currentTimeMillis() - start));
                return true;
            } catch (CoreException e) { // ignore delta if not able to traverse
            }
        }
        //System.out.println("IGNORE MARKER DELTA took: "+
        // (System.currentTimeMillis() - start));
        return false;
    }

}
/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Tobias Sodergren - initial API and implementation
 *     Achim Bursian    - complete reworked for v2.2...
 *******************************************************************************/
package net.sourceforge.eclipseccase;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseElementState;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * This class gathers view private elements in views.
 * <p>
 * The constructor takes an array of {@link IResource}s which should be
 * directories in which the search commands shall be performed. Typically, it is
 * called with one directory per used view.
 * 
 * Strategy:
 * <ol>
 * <li>For each directory, find out if it exists in a snapshot or dynamic view.</li>
 * <li>For all views, perform "cleartool lscheckout"</li>
 * <li>For all dynamic views, perform the "cleartool lsprivate" command</li>
 * <li>For all resources in a snapshot view, perform "cleartools ls -view_only"</li>
 * </ol>
 * 
 * <p>
 * Assumptions made by this class:
 * <ul>
 * <li>A project is associated with exactly one or no ClearCase view.
 * <li>A project can contain linked resources that point to elements in the same
 * view.
 * <li>A project can be associated with a dynamic or a snapshot view.
 * </ul>
 * 
 * @author Achim Bursian
 * @author Tobias Sodergren
 * 
 */
public class ViewPrivCollector {

	private static final String TRACE_ID = ViewPrivCollector.class
			.getSimpleName();

	private final Set<IResource> startupDirectories = new HashSet<IResource>();

	private Map<String, ClearCaseElementState> elementStates = new HashMap<String, ClearCaseElementState>();

	private boolean findCheckedouts = true;

	private boolean findHijacked = true;

	private boolean findOthers = true;

	public ViewPrivCollector(IResource[] resources) {
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			startupDirectories.add(resource);
		}
	}

	private class RefreshSourceData {
		private final String viewName;
		private final boolean isSnapshot;
		private final List<IResource> resources = new ArrayList<IResource>();

		public RefreshSourceData(IProject project, IResource resource,
				String viewName, boolean isSnapshot) {
			this.resources.add(resource);
			this.viewName = viewName;
			this.isSnapshot = isSnapshot;
		}

		public String getViewName() {
			return viewName;
		}

		public boolean isSnapshot() {
			return isSnapshot;
		}

		public IResource[] getResources() {
			return resources.toArray(new IResource[resources.size()]);
		}

		public void addResource(IResource resource) {
			resources.add(resource);
		}
	}

	public void collectElements(final IProgressMonitor progressMonitor) {
		IProgressMonitor monitor = progressMonitor == null ? new NullProgressMonitor()
				: progressMonitor;
		monitor.beginTask("Collecting elements", 100 * startupDirectories
				.size() + 10);
		// Find all involved projects and whether they contain dynamic or
		// snapshot views
		Map<IProject, RefreshSourceData> projects = new HashMap<IProject, RefreshSourceData>();
		Iterator<IResource> resourceIterator = startupDirectories.iterator();
		monitor.worked(5);
		while (resourceIterator.hasNext()) {
			IResource resource = resourceIterator.next();
			IProject project = resource.getProject();
			if (!projects.containsKey(project)) {
				boolean isSnapshotView;
				isSnapshotView = ClearCaseProvider.getViewType(resource)
						.equals("snapshot");
				projects.put(project,
						new RefreshSourceData(project, resource,
								ClearCaseProvider.getViewName(resource),
								isSnapshotView));
			} else {
				RefreshSourceData data = projects.get(project);
				data.addResource(resource);
			}

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

		}

		// For each project, perform the dynamic or snapshot listing strategy
		Iterator<IProject> projectIterator = projects.keySet().iterator();
		Set<String> queriedViews = new HashSet<String>();
		while (projectIterator.hasNext()) {
			IProject project = projectIterator.next();
			RefreshSourceData data = (RefreshSourceData) projects.get(project);
			if (data.isSnapshot() == true) {
				trace("Refreshing snapshot view " + data.getViewName());
				gatherSnapshotViewElements(data.getResources()[0], data
						.getViewName(), queriedViews, monitor);
			} else {
				trace("Refreshing dynamic view " + data.getViewName());
				gatherDynamicViewElements(data.getResources()[0], data
						.getViewName(), queriedViews, monitor);
			}
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

		}

		monitor.done();
	}

	private void gatherDynamicViewElements(IResource workingdir,
			String viewName, Set<String> queriedViews, IProgressMonitor monitor) {

		if (!queriedViews.contains(viewName)) {

			if (monitor.isCanceled())
				throw new OperationCanceledException();

			if (findCheckedouts) {
				addCheckedOutFiles(viewName, monitor, workingdir.getLocation());
			}
			monitor.worked(40);

			if (monitor.isCanceled())
				throw new OperationCanceledException();

			if (findOthers) {
				monitor.subTask("View private in " + viewName);
				// TODO: process getViewLSPrivateList line by line, not as array
				ClearCaseElementState[] viewLSPrivateList = ClearCasePlugin
						.getEngine().getViewLSPrivateList(
								workingdir.getLocation().toOSString(), null);
				monitor.worked(50);
				if (null == viewLSPrivateList) {
					throw new RuntimeException(
							"Could not get view private file information from view: "
									+ viewName);
				}
				updateStateCaches(viewLSPrivateList, monitor);
			} else {
				monitor.worked(50);
			}
			if (monitor.isCanceled())
				throw new OperationCanceledException();
			monitor.worked(10);
			queriedViews.add(viewName);
		} else {
			// view was already processed... (?)
			monitor.worked(100);
		}
	}

	private void gatherSnapshotViewElements(IResource workingdir,
			String viewName, Set<String> queriedViews, IProgressMonitor monitor) {

		if (!queriedViews.contains(viewName)) {

			if (monitor.isCanceled())
				throw new OperationCanceledException();

			if (findCheckedouts) {
				addCheckedOutFiles(viewName, monitor, workingdir.getLocation());
			}
			monitor.worked(40);

			if (findOthers || findHijacked) {
				// TODO: find top directory of SS view via
				// "cleartool pwv -root",
				// then switch to that dir and perform getViewLSViewOnlyList
				monitor.subTask("View private in " + viewName);
				ClearCaseElementState[] viewLSViewOnlyList = ClearCasePlugin
						.getEngine().getViewLSViewOnlyList(
								workingdir.getLocation().toOSString(), null);
				monitor.worked(50);
				updateStateCaches(viewLSViewOnlyList, monitor);
			} else {
				monitor.worked(50);
			}

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			queriedViews.add(viewName);
			monitor.worked(10);
		} else {
			// view was already processed... (?)
			monitor.worked(100);
		}
	}

	private void addCheckedOutFiles(String viewName, IProgressMonitor monitor,
			IPath path) {
		String workingdir = path.toOSString();
		monitor.subTask("Checked out in " + viewName);
		trace("addCheckedOutFiles, dir=: " + workingdir);
		// TODO: process getCheckedOutElements line by line, not as array
		ClearCaseElementState[] checkedOutState = ClearCasePlugin.getEngine()
				.getCheckedOutElements(workingdir, null);
		updateStateCaches(checkedOutState, monitor);
	}

	private void updateStateCaches(ClearCaseElementState[] elementStates,
			IProgressMonitor monitor) {
		for (int i = 0; i < elementStates.length; i++) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			ClearCaseElementState elementState = elementStates[i];
			File targetLocation = new File(elementState.element);
			IResource[] resources = null;
			if (targetLocation.isDirectory()) {
				resources = ResourcesPlugin.getWorkspace().getRoot()
						.findContainersForLocationURI(targetLocation.toURI());
			} else {
				resources = ResourcesPlugin.getWorkspace().getRoot()
						.findFilesForLocationURI(targetLocation.toURI());
			}

			// TODO: what about found resources that are not visible in
			// workspace yet? Must perform a refresh on parent...?
			for (IResource resource : resources) {
				StateCache cache = StateCacheFactory.getInstance()
						.getWithNoUpdate(resource);
				if (elementState.isCheckedOut() && !cache.isCheckedOut()) {
					trace("Found CO " + resource.getLocation());
					cache.doUpdate(elementState);
				} else if (elementState.isHijacked() && !cache.isHijacked()) {
					trace("Found Hijacked " + resource.getLocation());
					cache.doUpdate(elementState);
				} else if (cache.isUninitialized()) {
					trace("Found New " + resource.getLocation());
					cache.doUpdate(elementState);
				}
			}
		}
	}

	private List<IPath> optimizeClearCaseOperationsForLinkedResources(
			IResource[] resources) {
		Set<IPath> paths = new HashSet<IPath>();

		boolean checkParent = ClearCasePlugin.isTestLinkedParentInClearCase();

		// Use parent optimization when checking status for resources.
		for (IResource resource : resources) {
			if (checkParent) {
				IPath parent = resource.getLocation().removeLastSegments(1);
				ClearCaseElementState elementState = elementStates.get(parent
						.toOSString());
				if (elementState == null) {
					elementState = ClearCasePlugin.getEngine().getElementState(
							parent.toOSString());
					elementStates.put(parent.toOSString(), elementState);
				}
				if (elementState.isElement()) {
					paths.add(parent);
				} else {
					paths.add(resource.getLocation());
				}
			} else {
				paths.add(resource.getLocation());

			}
		}

		// Sort list to get a nicer progress output
		List<IPath> sortedPaths = new ArrayList<IPath>(paths);
		Collections.sort(sortedPaths, new Comparator<IPath>() {
			public int compare(IPath left, IPath right) {
				return left.toOSString().compareTo(right.toOSString());
			}
		});

		return sortedPaths;
	}

	public ClearCaseElementState getElementState(StateCache stateCache) {
		ClearCaseElementState state = elementStates.get(stateCache.getPath());
		if (state != null) {
			return state;
		}

		// Try the parent and see if it is marked as outside VOB
		String path = stateCache.getPath();
		String pathSeparator = "\\";
		if (!path.contains(pathSeparator)) {
			pathSeparator = "/";
		}
		String parentPath = path.substring(0, path.lastIndexOf(pathSeparator));
		ClearCaseElementState parentState = elementStates.get(parentPath);
		if (parentState != null && parentState.isOutsideVob()) {
			return new ClearCaseElementState(stateCache.getPath(),
					ClearCase.OUTSIDE_VOB);
		}

		return new ClearCaseElementState(stateCache.getPath(),
				ClearCase.CHECKED_IN | ClearCase.IS_ELEMENT);
	}

	private void trace(String message) {
		if (ClearCasePlugin.DEBUG_STATE_CACHE) {
			ClearCasePlugin.trace(TRACE_ID, message);
		}

	}

	public boolean isFindCheckedouts() {
		return findCheckedouts;
	}

	public void setFindCheckedouts(boolean findCheckedouts) {
		this.findCheckedouts = findCheckedouts;
	}

	public boolean isFindOthers() {
		return findOthers;
	}

	public void setFindOthers(boolean findOthers) {
		this.findOthers = findOthers;
	}

	public boolean isFindHijacked() {
		return findHijacked;
	}

	public void setFindHijacked(boolean findHijacked) {
		this.findHijacked = findHijacked;
	}

}

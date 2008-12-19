/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Tobias Sodergren - initial API and implementation
 *******************************************************************************/

package net.sourceforge.eclipseccase;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * A job for recursively refreshing the state of a resource.
 * 
 * @author Tobias Sodergren
 */
class RecursiveStateCacheJob extends StateCacheJob implements Comparable {

	/** the state cache to refresh */
	private final IResource resource;

	private final ClearcaseElementStatusCollector statusCollector;

	/**
	 * Creates a new job with default priority.
	 * 
	 * @param resource
	 *            The state cache to refresh.
	 * @param statusCollector
	 *            The status collector containing new status for element.
	 */
	public RecursiveStateCacheJob(IResource resource,
			ClearcaseElementStatusCollector statusCollector) {
		this(resource, statusCollector, PRIORITY_DEFAULT);
	}

	/**
	 * Creates a njob with the specified priority.
	 * 
	 * @param resource
	 * @param jobPriority
	 */
	public RecursiveStateCacheJob(IResource resource, ClearcaseElementStatusCollector statusCollector, int jobPriority) {
		super(StateCacheFactory.getInstance().get(resource), jobPriority);
		this.resource = resource;
		this.statusCollector = statusCollector;
	}

	/**
	 * Executes this job
	 * <p>
	 * This method should not be called by clients. It is called by the
	 * {@link StateCacheJobQueue}.
	 * </p>
	 * 
	 * @param monitor
	 * @throws CoreException
	 *             in case of problems
	 * @throws OperationCanceledException
	 *             if the operation was canceled
	 */
	void execute(final IProgressMonitor monitor) throws CoreException,
			OperationCanceledException {
		resource.accept(new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				StateCache cache = StateCacheFactory.getInstance().getWithNoUpdate(resource);
				cache.doUpdate(statusCollector);
				monitor.subTask("Refreshed: " + cache.getPath());
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(); 
				}
				return true;
			}});
		monitor.done();
	}

}
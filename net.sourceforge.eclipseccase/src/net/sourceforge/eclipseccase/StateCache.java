package net.sourceforge.eclipseccase;

import java.io.IOException;
import java.io.Serializable;

import net.sourceforge.clearcase.simple.ClearcaseUtil;
import net.sourceforge.clearcase.simple.IClearcase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.Team;

public class StateCache implements Serializable {

	static final long serialVersionUID = -7439899000320633901L;

	private String osPath;

	private String workspaceResourcePath;

	transient IResource resource;

	transient long updateTimeStamp = IResource.NULL_STAMP;

	int flags = 0;

	String version;

	StateCache(IResource resource) {
		if (null == resource)
			throw new IllegalArgumentException("Resource must not be null!"); //$NON-NLS-1$

		this.resource = resource;

		IPath location = resource.isAccessible() ? resource.getLocation()
				: null;
		if (location != null) {
			osPath = location.toOSString();
		} else {
			// resource has been invalidated in the workspace since request was
			// queued, so ignore update request.
			osPath = null;
		}
	}

	private static final String TRACE_ID = "StateCache"; //$NON-NLS-1$

	String symbolicLinkTarget;

	// flags

	private static final int HAS_REMOTE = 0x1;

	private static final int CHECKED_OUT = 0x2;

	private static final int SNAPSHOT = 0x4;

	private static final int HIJACKED = 0x8;

	private static final int CHECKED_OUT_OTHER_VIEW = 0x10;

	private static final int SYM_LINK = 0x20;

	private static final int SYM_LINK_TARGET_VALID = 0x40;

	private static final int INSIDE_VIEW = 0x80;

	/**
	 * Schedules a state update.
	 * 
	 * @param invalidate
	 */
	public void updateAsync(boolean invalidate) {
		updateAsync(invalidate, false);
	}

	/**
	 * Schedules a state update with a high priority.
	 * 
	 * @param invalidate
	 */
	public void updateAsyncHighPriority(boolean invalidate) {
		updateAsync(invalidate, true);
	}

	/**
	 * Schedules a state update.
	 * 
	 * @param invalidate
	 * @param useHighPriority
	 */
	private void updateAsync(boolean invalidate, boolean useHighPriority) {
		if (invalidate) {
			if (!isUninitialized()) {
				// synchronize access
				synchronized (this) {
					updateTimeStamp = IResource.NULL_STAMP;
				}
				ClearcasePlugin.trace(TRACE_ID, "invalidating " + this); //$NON-NLS-1$
				// fireing state change (the update was forced)
				StateCacheFactory.getInstance().fireStateChanged(this.resource);
			}
		}
		StateCacheJob job = new StateCacheJob(this);
		job.schedule(useHighPriority ? StateCacheJob.PRIORITY_HIGH
				: StateCacheJob.PRIORITY_DEFAULT);
	}

	/**
	 * Updates the state.
	 * 
	 * @param monitor
	 * @throws CoreException
	 * @throws OperationCanceledException
	 */
	void doUpdate(IProgressMonitor monitor) throws CoreException,
			OperationCanceledException {
		try {
			monitor
					.beginTask(
							Messages.getString("StateCache.updating") + getResource(), 10); //$NON-NLS-1$
			doUpdate();
			monitor.worked(10);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Updates the state.
	 */
	void doUpdate() {
		boolean changed = isUninitialized();

		IPath location = resource.getLocation();
		if (location == null) {
			// resource has been invalidated in the workspace since request was
			// queued, so ignore update request.
			if (ClearcasePlugin.DEBUG_STATE_CACHE) {
				ClearcasePlugin.trace(TRACE_ID,
						"not updating - invalid resource: " //$NON-NLS-1$
								+ resource);
			}
			return;
		}

		// only synchronize here
		synchronized (this) {

			osPath = location.toOSString();

			if (ClearcasePlugin.DEBUG_STATE_CACHE) {
				ClearcasePlugin.trace(TRACE_ID, "updating " + resource); //$NON-NLS-1$
			}

			if (resource.isAccessible()) {
				// can be disable through preference page [work in progress]
				if (ClearcasePlugin.isRefreshChildrenPrevented()
						&& resource.getParent() != null
						&& !(resource instanceof IProject || resource
								.getParent() instanceof IProject)
						&& !StateCacheFactory.getInstance().get(
								resource.getParent(), false).hasRemote()) {
					// RDM: resource parent is NOT in clearcase -> do not
					// process children.
					flags = 0;
					setFlag(HAS_REMOTE, false);
					version = null;
					symbolicLinkTarget = null;
					changed = true;
					if (ClearcasePlugin.DEBUG_STATE_CACHE) {
						ClearcasePlugin.trace(TRACE_ID,
								"resource parent has no remote: " //$NON-NLS-1$
										+ resource);
					}
				}
				// check the global ignores from Team (includes derived
				// resources)

				else if (!Team.isIgnoredHint(resource)) {
					boolean newHasRemote = ClearcasePlugin.getEngine()
							.isElement(osPath);
					changed |= newHasRemote != this.hasRemote();
					setFlag(HAS_REMOTE, newHasRemote);

					boolean newInsideView = checkInsideView(osPath);
					changed |= newInsideView != this.isInsideView();
					setFlag(INSIDE_VIEW, newInsideView);

					boolean newIsSymbolicLink = newHasRemote
							&& ClearcasePlugin.getEngine().isSymbolicLink(
									osPath);
					changed |= newIsSymbolicLink != this.isSymbolicLink();
					setFlag(SYM_LINK, newIsSymbolicLink);

					boolean newIsCheckedOut = newHasRemote
							&& ClearcasePlugin.getEngine().isCheckedOut(osPath,
									newIsSymbolicLink);
					changed |= newIsCheckedOut != this.isCheckedOut();
					setFlag(CHECKED_OUT, newIsCheckedOut);

					boolean newIsSnapShot = newHasRemote
							&& ClearcasePlugin.getEngine().isSnapShot(osPath,
									newIsSymbolicLink);
					changed |= newIsSnapShot != this.isSnapShot();
					setFlag(SNAPSHOT, newIsSnapShot);

					boolean newIsHijacked = newIsSnapShot
							&& ClearcasePlugin.getEngine().isHijacked(osPath,
									newIsSymbolicLink);
					changed |= newIsHijacked != this.isHijacked();
					setFlag(HIJACKED, newIsHijacked);

					boolean newIsEdited = newHasRemote
							&& !newIsCheckedOut
							&& ClearcasePlugin.getEngine()
									.isCheckedOutInAnotherView(osPath,
											newIsSymbolicLink);
					changed |= newIsEdited != this.isEdited();
					setFlag(CHECKED_OUT_OTHER_VIEW, newIsEdited);

					String newVersion = newHasRemote ? ClearcasePlugin
							.getEngine()
							.cleartool(
									"describe -fmt " + ClearcaseUtil.quote("%Vn") //$NON-NLS-1$ //$NON-NLS-2$
											+ " " + ClearcaseUtil.quote(osPath)).message //$NON-NLS-1$
							.trim().replace('\\', '/')
							: null;
					changed |= newVersion == null ? null != this.version
							: !newVersion.equals(this.version);
					this.version = newVersion;

					if (newIsSymbolicLink) {
						IClearcase.Status status = ClearcasePlugin.getEngine()
								.getSymbolicLinkTarget(osPath);
						if (null != status && status.status) {
							String newTarget = status.message;
							if (null != newTarget
									&& newTarget.trim().length() == 0)
								newTarget = null;
							changed |= null == newTarget ? null != this.symbolicLinkTarget
									: !newTarget
											.equals(this.symbolicLinkTarget);
							this.symbolicLinkTarget = newTarget;
						}

						boolean newIsTargetValid = ClearcasePlugin.getEngine()
								.isSymbolicLinkTargetValid(osPath);
						changed = changed
								|| newIsTargetValid != this
										.isSymbolicLinkTargetValid();
						setFlag(SYM_LINK_TARGET_VALID, newIsTargetValid);

					} else if (null != this.symbolicLinkTarget) {
						this.symbolicLinkTarget = null;
						setFlag(SYM_LINK_TARGET_VALID, false);
						changed = true;
					}

				} else {
					// resource is ignored by Team plug-ins
					flags = 0;
					version = null;
					symbolicLinkTarget = null;
					changed = false;
					if (ClearcasePlugin.DEBUG_STATE_CACHE) {
						ClearcasePlugin.trace(TRACE_ID,
								"resource must be ignored: " //$NON-NLS-1$
										+ resource);
					}
				}

			} else {
				// resource does not exists
				flags = 0;
				version = null;
				symbolicLinkTarget = null;
				changed = true;
				if (ClearcasePlugin.DEBUG_STATE_CACHE) {
					ClearcasePlugin.trace(TRACE_ID, "resource not accessible: " //$NON-NLS-1$
							+ resource);
				}
			}

			updateTimeStamp = resource.getModificationStamp();
		}

		// fire state change (lock must be released prior)
		if (changed) {
			if (ClearcasePlugin.DEBUG_STATE_CACHE) {
				ClearcasePlugin.trace(TRACE_ID, "updated " + this); //$NON-NLS-1$
			}
			StateCacheFactory.getInstance().fireStateChanged(this.resource);
		} else {
			// no changes
			if (ClearcasePlugin.DEBUG_STATE_CACHE) {
				ClearcasePlugin.trace(TRACE_ID, "  no changes detected"); //$NON-NLS-1$ 
			}
		}
	}

	/**
	 * Indicates if the underlying resource is a ClearCase element.
	 * 
	 * @return Returns a boolean
	 */
	public boolean hasRemote() {
		return getFlag(HAS_REMOTE);
	}

	/**
	 * Gets the isCheckedOut().
	 * 
	 * @return Returns a boolean
	 */
	public boolean isCheckedOut() {
		return getFlag(CHECKED_OUT);
	}

	/**
	 * Gets the isDirty.
	 * <p>
	 * The check is done using the timestamp only (for performance reasons). If
	 * you really want to compare using the conent you need to look at
	 * {@link #isDifferent()}.
	 * 
	 * @return Returns a boolean
	 */
	public boolean isDirty() {
		if (null == resource)
			return false;

		// performance improve: if not checked out it is not dirty
		if (!isCheckedOut())
			return false;

		return resource.getModificationStamp() != updateTimeStamp;
	}

	/**
	 * Detects if the resource is different from its predecessor.
	 * <p> The check is done using the file content.
	 * 
	 * @return Returns a boolean
	 */
	public boolean isDifferent() {

		// performance improve: if not dirty it can't be different
		if (!isDirty())
			return false;

		// this is very expensive
		try {
			return ClearcasePlugin.getEngine().isDifferent(osPath);
		} catch (RuntimeException ex) {
			ClearcasePlugin.log(IStatus.ERROR,
					"Could not determine element dirty state of "
							+ osPath
							+ ": "
							+ (null != ex.getCause() ? ex.getCause()
									.getMessage() : ex.getMessage()), ex);
		}

		// fallback to true
		return true;
	}

	/**
	 * Indicates if the resource is edited by someone else.
	 * 
	 * @return Returns a boolean
	 */
	public boolean isEdited() {
		return getFlag(CHECKED_OUT_OTHER_VIEW);
	}

	/**
	 * Returns the osPath.
	 * 
	 * @return String
	 */
	public String getPath() {
		return osPath;
	}

	/**
	 * Returns the version.
	 * 
	 * @return String
	 */
	public String getVersion() {
		return null == version ? "" : version; //$NON-NLS-1$
	}

	/**
	 * Returns the predecessor version.
	 * 
	 * @return String
	 */
	public String getPredecessorVersion() {
		String predecessorVersion = null;

		IClearcase.Status status = (isHijacked() ? ClearcasePlugin.getEngine()
				.cleartool(
						"ls " //$NON-NLS-1$
								+ ClearcaseUtil.quote(resource.getLocation()
										.toOSString())) : ClearcasePlugin
				.getEngine().cleartool(
						"describe -fmt %PVn " //$NON-NLS-1$
								+ ClearcaseUtil.quote(resource.getLocation()
										.toOSString())));
		if (status.status) {
			predecessorVersion = status.message.trim().replace('\\', '/');
			if (isHijacked()) {
				int offset = predecessorVersion.indexOf("@@") + 2; //$NON-NLS-1$
				int cutoff = predecessorVersion.indexOf("[hijacked]") - 1; //$NON-NLS-1$
				try {
					predecessorVersion = predecessorVersion.substring(offset,
							cutoff);
				} catch (Exception e) {
					predecessorVersion = null;
				}
			}
		}

		return predecessorVersion;
	}

	/**
	 * Returns the isUninitialized().
	 * 
	 * @return boolean
	 */
	public boolean isUninitialized() {
		// always ignore Team-ignore resources
		if (Team.isIgnoredHint(resource))
			return false;

		// check if we have a timestamp
		return IResource.NULL_STAMP == updateTimeStamp;
	}

	/**
	 * Returns the isHijacked().
	 * 
	 * @return boolean
	 */
	public boolean isHijacked() {
		return getFlag(HIJACKED);
	}

	/**
	 * Returns the isSnapShot().
	 * 
	 * @return boolean
	 */
	public boolean isSnapShot() {
		return getFlag(SNAPSHOT);
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		// special handling for resource
		if (null != resource) {
			// make sure we only save states for real resources
			if (resource.isAccessible()) {
				this.workspaceResourcePath = resource.getFullPath().toString();
			} else {
				this.workspaceResourcePath = null;
			}
		}
		out.defaultWriteObject();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();

		// restore resource
		if (null != workspaceResourcePath) {
			// determine resource
			IPath path = new Path(workspaceResourcePath);
			resource = ResourcesPlugin.getWorkspace().getRoot()
					.findMember(path);
			if (resource != null && resource.isAccessible()) {
				IPath location = resource.getLocation();
				if (location != null) {
					osPath = location.toOSString();
				} else {
					// resource has been invalidated in the workspace since
					// request was
					// queued, so ignore update request.
					osPath = null;
				}
			} else {
				// invalid resource
				resource = null;
				osPath = null;
				workspaceResourcePath = null;
			}
		} else {
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
	public IResource getResource() {
		return resource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer toString = new StringBuffer("StateCache "); //$NON-NLS-1$
		toString.append(resource);
		toString.append(": "); //$NON-NLS-1$
		if (isUninitialized()) {
			toString.append("not initialized"); //$NON-NLS-1$
		} else if (!hasRemote()) {
			toString.append("no clearcase element"); //$NON-NLS-1$
		} else if (hasRemote()) {
			toString.append(version);

			if (isSymbolicLink()) {
				toString.append(" [SYMBOLIC LINK ("); //$NON-NLS-1$
				toString.append(symbolicLinkTarget);
				toString.append(")]"); //$NON-NLS-1$
			}

			if (isCheckedOut())
				toString.append(" [CHECKED OUT]"); //$NON-NLS-1$

			if (isHijacked())
				toString.append(" [HIJACKED]"); //$NON-NLS-1$

			if (isSnapShot())
				toString.append(" [SNAPSHOT]"); //$NON-NLS-1$
		} else {
			toString.append("invalid"); //$NON-NLS-1$
		}

		return toString.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (null == resource)
			return 0;

		return resource.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (null == obj || StateCache.class != obj.getClass())
			return false;

		if (null == resource)
			return null == ((StateCache) obj).resource;

		return resource.equals(((StateCache) obj).resource);
	}

	/**
	 * @return
	 */
	public boolean isSymbolicLink() {
		return getFlag(SYM_LINK);
	}

	/**
	 * Returns the symbolicLinkTarget.
	 * 
	 * @return returns the symbolicLinkTarget
	 */
	public String getSymbolicLinkTarget() {
		return null == symbolicLinkTarget ? "" : symbolicLinkTarget; //$NON-NLS-1$
	}

	/**
	 * @return
	 */
	public boolean isSymbolicLinkTargetValid() {
		return getFlag(SYM_LINK_TARGET_VALID);
	}

	/**
	 * Indicates if the specified file is inside a view directory.
	 * 
	 * @param file
	 * @return <code>true</code> if the specified resource is a view directory
	 */
	static boolean checkInsideView(String file) {
		IClearcase.Status status = ClearcasePlugin.getEngine()
				.getViewRoot(file);
		return null != status && status.status;
	}

	/**
	 * Indicates if the resource is within a ClearCase view.
	 * 
	 * @return
	 */
	public boolean isInsideView() {
		return getFlag(INSIDE_VIEW);
	}

	/**
	 * Returns <code>true</code> if the specified flag is set.
	 * 
	 * @param flag
	 * @return <code>true</code> if the specified flag is set
	 */
	boolean getFlag(int flag) {
		return 0 != (flags & flag);
	}

	/**
	 * Sets the flag to the specified value.
	 * 
	 * @param flag
	 * @param value
	 */
	void setFlag(int flag, boolean value) {
		if (value)
			flags |= flag;
		else
			flags &= ~flag;
	}
}
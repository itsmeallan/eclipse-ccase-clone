/**
 * 
 */
package net.sourceforge.eclipseccase;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Analyse the output lines of a "cleartool update -print" to gather hijacked
 * elements in a snapshot view
 */
public class ViewprivOperationListenerHJ extends ViewprivOperationListener {

	protected String basedir;

	public ViewprivOperationListenerHJ(String prefix, String topDir,
			IProgressMonitor monitor) {
		super(prefix, monitor);
		basedir = topDir;
	}

	@Override
	protected void analyseLine(String line) {
		if (!line.startsWith("Keeping hijacked")) {
			return;
		}

		final String filename = basedir + "/"
				+ line.replaceFirst("^.*?\"(.*?)\".*", "$1");

		// we have a valid name now
		// System.out.println("+++ "+ filename);
		IResource[] resources = findResources(filename);

		// What about found resources that are not visible in
		// workspace yet? Two possibilities:
		// 1) If it would be visible after a manual refresh, we get a
		// valid IResource here, but resource.isAccessible() is false.
		// That is handled in cache.doUpdate() later
		// 2) If the workspace does not have a possible access path to the
		// resource, resources is empty, the for loop is not executed
		for (IResource resource : resources) {
			StateCache cache = StateCacheFactory.getInstance().getWithNoUpdate(
					resource);
			if (cache.isUninitialized()) {
				trace("Found new HJ(1) " + resource.getLocation());
				cache.updateAsync(true);
			} else if (cache.isClearCaseElement()) {
				if (cache.isHijacked()) {
					// validate that this is still a hijacked element
					cache.setVpStateVerified();
				} else {
					trace("Found new HJ(2) " + resource.getLocation());
					cache.updateAsync(true);
				}
			} else {
				// cached state is not (yet) a CC element
				trace("Found new HJ(3) " + resource.getLocation());
				cache.updateAsync(true);
			}
		}
	}

}

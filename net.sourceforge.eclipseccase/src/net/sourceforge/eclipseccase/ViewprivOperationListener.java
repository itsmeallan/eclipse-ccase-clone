package net.sourceforge.eclipseccase;

import java.io.File;

import net.sourceforge.clearcase.events.OperationListener;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

public abstract class ViewprivOperationListener implements OperationListener {

	private static final String TRACE_ID = ViewprivOperationListener.class
			.getSimpleName();

	private IProgressMonitor monitor = null;

	private int receivedLines = 0;

	private final String prefix;

	public ViewprivOperationListener(String prefix, IProgressMonitor monitor) {
		this.monitor = monitor;
		this.prefix = prefix;
		updateJobStatus();
	}

	public void finishedOperation() {
	}

	public boolean isCanceled() {
		return monitor.isCanceled();
	}

	public void ping() {
	}

	public void print(String line) {
		receivedLines++;

		if (monitor.isCanceled())
			throw new OperationCanceledException();

		if (receivedLines % 50 == 0) {
			updateJobStatus();
		}
		if (line.length() == 0) {
			// ignore empty filenames
			return;
		}
		if (line.charAt(0) == '#') {
			// ignore filenames starting with #, as these are in non-mounted
			// VOBs on PC
			return;
		}

		analyseLine(line);
	}

	protected abstract void analyseLine(String line);

	protected IResource[] findResources(String filename) {
		File targetLocation = new File(filename);
		IResource[] resources = null;
		if (targetLocation.isDirectory()) {
			resources = ResourcesPlugin.getWorkspace().getRoot()
					.findContainersForLocationURI(targetLocation.toURI());
		} else {
			resources = ResourcesPlugin.getWorkspace().getRoot()
					.findFilesForLocationURI(targetLocation.toURI());
		}
		return resources;
	}

	private void updateJobStatus() {
		monitor.subTask(prefix + ", lines received from CC: " + receivedLines);
	}

	public void printErr(String msg) {
		if (monitor.isCanceled())
			throw new OperationCanceledException();
	}

	public void printInfo(String msg) {
		if (monitor.isCanceled())
			throw new OperationCanceledException();
	}

	public void startedOperation(int amountOfWork) {
	}

	public void worked(int ticks) {
	}

	protected void trace(String message) {
		if (ClearCasePlugin.DEBUG_STATE_CACHE) {
			ClearCasePlugin.trace(TRACE_ID, message);
		}
	}
}

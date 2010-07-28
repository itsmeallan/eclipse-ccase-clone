package net.sourceforge.eclipseccase;

import net.sourceforge.clearcase.events.OperationListener;
import org.eclipse.core.runtime.IProgressMonitor;

public class ViewprivOperationListener implements OperationListener {

	private IProgressMonitor monitor = null;

	private int receivedLines = 0;

	private String prefix;

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

	public void print(String msg) {
		receivedLines++;
		if (receivedLines % 50 == 0) {
			updateJobStatus();
		}
		//System.out.println("++ "+ msg);
	}

	private void updateJobStatus() {
		monitor.subTask(prefix + ", lines received from CC: " + receivedLines);
	}

	public void printErr(String msg) {
	}

	public void printInfo(String msg) {
	}

	public void startedOperation(int amountOfWork) {
	}

	public void worked(int ticks) {
	}
}

package net.sourceforge.eclipseccase.ui.console;

import net.sourceforge.clearcase.events.OperationListener;
import org.eclipse.core.runtime.IProgressMonitor;

public class ConsoleOperationListener implements OperationListener {

	private IProgressMonitor monitor = null;

	private ClearCaseConsole console = null;

	public ConsoleOperationListener(IProgressMonitor monitor) {
		this.monitor = monitor;
		console = ClearCaseConsoleFactory.getClearCaseConsole();
		console.show();
		console.clear();
	}

	public void finishedOperation() {
	}

	public boolean isCanceled() {
		return monitor.isCanceled();
	}

	public void ping() {
	}

	public void print(String msg) {
		console.out.println(msg);
	}

	public void printErr(String msg) {
		console.err.println(msg);
	}

	public void printInfo(String msg) {
		console.info.println(msg);
	}

	public void startedOperation(int amountOfWork) {
	}

	public void worked(int ticks) {
	}
}

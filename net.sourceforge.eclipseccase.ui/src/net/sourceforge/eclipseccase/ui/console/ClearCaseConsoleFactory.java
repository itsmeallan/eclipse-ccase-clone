package net.sourceforge.eclipseccase.ui.console;

import org.eclipse.ui.console.*;

public class ClearCaseConsoleFactory implements IConsoleFactory {

	private static final String CONSOLE_NAME = "ClearCase Console";

	public void openConsole() {
		ClearCaseConsole myConsole = getClearCaseConsole();
		myConsole.show();
	}

	public static ClearCaseConsole getClearCaseConsole() {
		ClearCaseConsole myConsole = (ClearCaseConsole) findConsole(CONSOLE_NAME);

		if (myConsole == null) {
			ConsolePlugin plugin = ConsolePlugin.getDefault();
			IConsoleManager conMan = plugin.getConsoleManager();
			// no console found, so create a new one
			myConsole = new ClearCaseConsole(CONSOLE_NAME);
			conMan.addConsoles(new IConsole[] { myConsole });
		}

		return myConsole;
	}

	private static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		return null;
	}

	public static void closeConsole() {
		MessageConsole myConsole = findConsole(CONSOLE_NAME);
		if (myConsole != null) {
			ConsolePlugin plugin = ConsolePlugin.getDefault();
			IConsoleManager conMan = plugin.getConsoleManager();

			conMan.removeConsoles(new IConsole[] { myConsole });
		}
	}
}

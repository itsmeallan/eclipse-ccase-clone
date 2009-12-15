package net.sourceforge.eclipseccase.ui.console;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;

import org.eclipse.swt.widgets.Display;

import org.eclipse.ui.PlatformUI;

import org.eclipse.swt.graphics.Color;

import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.jface.util.IPropertyChangeListener;

import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.ui.console.MessageConsoleStream;

import org.eclipse.ui.console.MessageConsole;

public class ClearCaseConsole extends MessageConsole implements IPropertyChangeListener {
	public static final String CLEARCASE_CONSOLE_TYPE = "net.sourceforge.eclipseccase.ui.console.ClearCaseConsole";

	public MessageConsoleStream out;

	public MessageConsoleStream err;

	public MessageConsoleStream info;

	public ClearCaseConsole(String name) {
		super(name, null);

		super.setType(ClearCaseConsole.CLEARCASE_CONSOLE_TYPE);

		super.init();

		this.setTabWidth(2);

		this.out =  this.newMessageStream();
		this.err =  this.newMessageStream();
		this.info = this.newMessageStream();

		this.loadPreferences();

		JFaceResources.getFontRegistry().addListener(this);
	}

	/* Get ready to modify console using preferences. */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().startsWith("net.sourceforge.eclipseccase.ui.console")) {
			this.loadPreferences();
		}
	}

	protected void loadPreferences() {
		Display display = PlatformUI.getWorkbench().getDisplay();

		Color tmp = this.out.getColor();
		this.out.setColor(new Color(display, 0, 0, 0));
		if (tmp != null && !tmp.equals(this.out.getColor())) {
			tmp.dispose();
		}

		tmp = this.err.getColor();
		this.err.setColor(new Color(display, 255, 0, 0));
		if (tmp != null && !tmp.equals(this.out.getColor())) {
			tmp.dispose();
		}

		tmp = this.info.getColor();
		this.info.setColor(new Color(display, 0, 100, 255));
		if (tmp != null && !tmp.equals(this.out.getColor())) {
			tmp.dispose();
		}
	}

	public void show() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable(){
			public void run() {
				try{
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();//obtain the active page
					String id = IConsoleConstants.ID_CONSOLE_VIEW;
					IConsoleView view = (IConsoleView) page.showView(id);
					view.display(ClearCaseConsole.this);		   
				}
				catch(PartInitException e)
				{
				}
			}
		});
	}

	public void clear() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable(){
			public void run() {
				ClearCaseConsole.this.clearConsole();   
			}
		});
	}
}

package net.sourceforge.eclipseccase.ui.console;

import org.eclipse.jface.action.Action;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.*;
import org.eclipse.ui.part.IPageBookViewPage;

public class ClearCaseConsolePageParticipant implements IConsolePageParticipant {

	class ConsoleRemoveAction extends Action {

		ConsoleRemoveAction() {
			this.setText("Remove ClearCase Console"); //$NON-NLS-1$
			setToolTipText("Remove ClearCase Console"); //$NON-NLS-1$

			setImageDescriptor(TeamImages.getImageDescriptor("elcl16/participant_rem.gif")); //$NON-NLS-1$
			setDisabledImageDescriptor(TeamImages.getImageDescriptor("dlcl16/participant_rem.gif")); //$NON-NLS-1$
		}

		@Override
		public void run() {
			ClearCaseConsoleFactory.closeConsole();
		}
	}

	private ConsoleRemoveAction consoleRemoveAction;

	public void init(IPageBookViewPage page, IConsole console) {
		this.consoleRemoveAction = new ConsoleRemoveAction();
		IActionBars bars = page.getSite().getActionBars();
		bars.getToolBarManager().appendToGroup(IConsoleConstants.LAUNCH_GROUP, consoleRemoveAction);
	}

	public void dispose() {
		this.consoleRemoveAction = null;
	}

	public void activated() {
	}

	public void deactivated() {
	}

	public Object getAdapter(Class adapter) {
		return null;
	}
}
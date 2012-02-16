package net.sourceforge.eclipseccase.ui.wizards;

import java.util.ResourceBundle;
import org.eclipse.compare.*;
import org.eclipse.compare.internal.ResizableDialog;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

public class CompareDialog extends ResizableDialog {

	private static final CompareConfiguration cc = new CompareConfiguration();

	private CompareViewerSwitchingPane compareViewerPane;

	private ICompareInput myInput;

	@SuppressWarnings("restriction")
	protected CompareDialog(Shell shell, ResourceBundle bundle) {
		super(shell, bundle);
		this.setShellStyle(SWT.CLOSE);
		cc.setLeftEditable(false);
		cc.setRightEditable(false);
	}

	void compare(ICompareInput input) {

		myInput = input;
		cc.setLeftLabel(myInput.getLeft().getName());
		cc.setLeftImage(myInput.getLeft().getImage());

		cc.setRightLabel(myInput.getRight().getName());
		cc.setRightImage(myInput.getRight().getImage());

		if (compareViewerPane != null) {
			compareViewerPane.setInput(myInput);
		}

	}

	/*
	 * (non Javadoc) Creates SWT control tree.
	 */
	@Override
	protected synchronized Control createDialogArea(Composite parent) {

		Composite composite = (Composite) super.createDialogArea(parent);

		getShell().setText("Compare"); //$NON-NLS-1$
		compareViewerPane = new ViewerSwitchingPane(composite, SWT.BORDER | SWT.FLAT);
		compareViewerPane.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL));

		if (myInput != null) {
			compareViewerPane.setInput(myInput);
		}

		applyDialogFont(composite);
		return composite;
	}

	class ViewerSwitchingPane extends CompareViewerSwitchingPane {

		ViewerSwitchingPane(Composite parent, int style) {
			super(parent, style, false);
		}

		@Override
		protected Viewer getViewer(Viewer oldViewer, Object input) {
			if (input instanceof ICompareInput)
				return CompareUI.findContentViewer(oldViewer, (ICompareInput) input, this, cc);
			return null;
		}

		@Override
		public void setImage(Image image) {
			// don't show icon
		}
	}

}

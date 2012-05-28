package net.sourceforge.eclipseccase.ui.utility;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Control;

import org.eclipse.swt.graphics.FontMetrics;

/**
 * Class provides gui helper methods.
 * 
 * @author mikael petterson
 * 
 */
public class SwtHelper {

	/**
	 * Number of horizontal dialog units per character, value <code>4</code>.
	 */
	private static final int HORIZONTAL_DIALOG_UNIT_PER_CHAR = 4;

	public static final int BUTTON_WIDTH = 76;

	/**
	 * Font metrics to use for determining pixel sizes.
	 */
	private FontMetrics fontMetrics;

	public static int convertHorizontalDLUsToPixels(FontMetrics fontMetrics, int dlus) {
		// round to the nearest pixel
		return (fontMetrics.getAverageCharWidth() * dlus + HORIZONTAL_DIALOG_UNIT_PER_CHAR / 2) / HORIZONTAL_DIALOG_UNIT_PER_CHAR;
	}

	public int convertHorizontalDLUsToPixels(int dlus) {
		// test for failure to initialize for backward compatibility
		if (fontMetrics == null) {
			return 0;
		}
		return convertHorizontalDLUsToPixels(fontMetrics, dlus);
	}

	public static int convertHeightInCharsToPixels(Control control, int chars) {
		GC gc = new GC(control);
		gc.setFont(JFaceResources.getDialogFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();
		return fontMetrics.getHeight() * chars;
	}

	public static int computeButtonWidth(Button button) {
		int width = button.computeSize(SWT.DEFAULT, SWT.DEFAULT).x + 6;
		return Math.max(width, BUTTON_WIDTH);
	}

}

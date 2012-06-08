package net.sourceforge.eclipseccase.ui.operation;


import net.sourceforge.eclipseccase.ClearCasePreferences;

import net.sourceforge.eclipseccase.diff.DiffFactory;

import net.sourceforge.eclipseccase.diff.AbstractDifference;

public class ExternalCompareOperation {

	protected final String myPrivate;

	protected final String other;

	public ExternalCompareOperation(final String myPrivate, final String other) {
		this.myPrivate = myPrivate;
		this.other = other;
		// Add a factory here that can decide which launcher to use.
		AbstractDifference diff = DiffFactory.getDiffTool(ClearCasePreferences.getExtDiffTool());
		//execute
		diff.twoWayDiff(myPrivate, other);
		
	}

	

}

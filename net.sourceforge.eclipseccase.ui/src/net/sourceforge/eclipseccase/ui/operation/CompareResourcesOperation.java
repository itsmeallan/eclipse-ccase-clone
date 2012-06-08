package net.sourceforge.eclipseccase.ui.operation;

import net.sourceforge.eclipseccase.ClearCasePreferences;



public class CompareResourcesOperation {
	
	protected String myPrivate;
	protected String other;
	
	public CompareResourcesOperation(String myPrivate, String other) {
		this.myPrivate = myPrivate;
		this.other = other;
		if(ClearCasePreferences.isCompareExternal()){
			ExternalCompareOperation extCmpOp = new ExternalCompareOperation(myPrivate,other);
		}else{
			//InternalCompareOperation intCmpOp = new InternalCompareOperation();
		}
	}
	
	
	

}

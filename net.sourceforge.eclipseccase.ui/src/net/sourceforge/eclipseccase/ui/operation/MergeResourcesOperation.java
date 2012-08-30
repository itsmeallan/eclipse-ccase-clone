/*******************************************************************************
 * Copyright (c) 2012 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     eraonel - inital API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui.operation;

import net.sourceforge.eclipseccase.ClearCaseProvider;

import org.eclipse.core.resources.IResource;

import net.sourceforge.eclipseccase.ClearCasePreferences;

/**
 * @author mikael petterson
 *
 */
public class MergeResourcesOperation {
	
	private IResource resource;
	private String selectedVersion;
	private String comparableVersion;
	private String base;
	private ClearCaseProvider provider;
	
	
	public MergeResourcesOperation(IResource resource,String selectedVersion,String preVersion,String base,ClearCaseProvider provider) {
		this.resource = resource;
		this.selectedVersion = selectedVersion;
		this.comparableVersion = preVersion;
		this.base = base;
		this.provider = provider;
		
		
	}
	
	public void merge(){
		
		if(ClearCasePreferences.isMergeExternal()){
			
			ExternalMergeOperation extMergeOp = new ExternalMergeOperation(resource,comparableVersion,base);
			extMergeOp.execute();
		}else{		
			InternalMergeOperation intMergeOp = new InternalMergeOperation(resource,selectedVersion,comparableVersion,base,provider);
			intMergeOp.execute();
		}
	}

}

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
package net.sourceforge.eclipseccase.ui.utility;



import org.eclipse.core.resources.IFile;

import java.io.File;

import java.net.URLConnection;

import org.eclipse.core.resources.IResource;

import org.eclipse.core.internal.preferences.Base64;

/**
 * @author eraonel
 * 
 */
public class FileHelper {

	public static String[] decodeStringToArray(String encodedString) {
		String[] valuesArray = new String[] {};
		if (encodedString != null && encodedString.length() > 0) {
			String[] array = encodedString.split(";"); //$NON-NLS-1$
			for (int i = 0; i < array.length; i++) {
				array[i] = new String(Base64.decode(array[i].getBytes()));
			}
			// include trailing empty string
			if (encodedString.endsWith(";")) { //$NON-NLS-1$
				valuesArray = new String[array.length + 1];
				System.arraycopy(array, 0, valuesArray, 0, array.length);
				valuesArray[valuesArray.length - 1] = ""; //$NON-NLS-1$
			} else {
				valuesArray = array;
			}
		}
		return valuesArray;
	}

	public static String encodeArrayToString(String[] valuesArray) {
		String result = ""; //$NON-NLS-1$
		for (int i = 0; i < valuesArray.length; i++) {
			String str = new String(Base64.encode(valuesArray[i].getBytes()));
			result += i == 0 ? str : (";" + str); //$NON-NLS-1$
		}
		return result;
	}

	public static String getNamesListAsString(Object[] resources) {
		String resourcesNames = ""; //$NON-NLS-1$
		String name = ""; //$NON-NLS-1$
		for (int i = 0; i < resources.length; i++) {
			if (i == 4) {
				resourcesNames += "..."; //$NON-NLS-1$
				break;
			}
			
			if (resources[i] instanceof IResource) {
				name = ((IResource) resources[i]).getName();
			} else {
				name = resources[i].toString();
			}
			resourcesNames += (i == 0 ? "'" : ", '") + name + "'"; //$NON-NLS-1$ 
		}

		return resourcesNames;
	}
	
	public static String getMimeType(IFile file){
		return URLConnection.guessContentTypeFromName(file.getFullPath().toString());
	}

}

package net.sourceforge.eclipseccase.ui;

import org.eclipse.core.resources.IResource;

import java.util.Comparator;

/**
 * Compares resouces.
 * @author mikael p.
 *
 */
public class ResourceComparator implements Comparator{
	
	public int compare(Object objA, Object objB) {
		IResource resourceA = (IResource) objA;
		IResource resourceB = (IResource) objB;
		return resourceA.getFullPath().toOSString().compareTo(resourceB.getFullPath().toOSString());
	}

}

package net.sourceforge.eclipseccase.diff;


/**
 * Defines diff/merge operation all supported tools must handle.
 * 
 * @author mikael petterson
 *
 */
public abstract class AbstractDifference {
	
	public abstract void twoWayDiff(String file1,String file2);
	
	public abstract void threeWayDiff(String file1,String file2,String base);
	
	public abstract boolean twoWayMerge(String file1,String file2);
	
	public abstract boolean threeWayMerge(String file1,String file2, String base);

}

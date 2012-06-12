package net.sourceforge.eclipseccase.diff;


/**
 * Defines diff/compare operation all supported tools must handle.
 * 
 * @author eraonel
 *
 */
public abstract class AbstractDifference {
	
	public abstract void twoWayDiff(String file1,String file2);
	
	public abstract void threeWayDiff(String file1,String file2,String base);

}

package net.sourceforge.eclipseccase.diff;

import org.eclipse.core.runtime.Status;


/**
 * Defines common operations for external tools ( not executed in 
 * Eclipse).
 * 
 * @author mikael petterson
 *
 */
public abstract class AbstractExternalToolCommands {
	
	public abstract void twoWayDiff(String file1,String file2);
	
	public abstract void threeWayDiff(String file1,String file2,String base);
	
	public abstract Status twoWayMerge(String file1,String file2);
	
	public abstract Status threeWayMerge(String file1,String file2, String base);

}

/**
 * 
 */
package net.sourceforge.eclipseccase.diff;

/**
 * @author mikael petterson
 *
 */
public class MergeFactory {
	
	public static AbstractExternalToolCommands getMergeTool(String type) {
		if (Constants.TOOL_KDIFF.equals(type)) {
			return new KdiffCommands();
		} else if (Constants.TOOL_IBM.equals(type)){
			
			return new ClearCaseCommands();
		}
		
		return null;//should not happen.
	}

}

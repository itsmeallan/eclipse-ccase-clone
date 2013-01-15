/**
 * 
 */
package net.sourceforge.eclipseccase.diff;

/**
 * @author mikael petterson
 *
 */
public class DiffFactory {
			
		public static AbstractExternalToolCommands getDiffTool(String type) {
			if (Constants.TOOL_KDIFF.equals(type)) {
				return new KdiffCommands();
			} else if (Constants.TOOL_IBM.equals(type)){
				
				return new ClearCaseCommands();
			}else if(Constants.TOOL_BEYONDCOMPARE.equals(type)){
				return new BeyondCompareCommands();
			}
			
			return null;//should not happen.
		}

	

}

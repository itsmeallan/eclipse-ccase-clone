package net.sourceforge.eclipseccase.diff;

import java.util.Map;

import net.sourceforge.clearcase.commandline.CommandLauncher;
import net.sourceforge.eclipseccase.ClearCasePreferences;

public class KdiffCommands extends AbstractDifference {

	public KdiffCommands() {

	}

	// kdiff3 file1 file2
	public void twoWayDiff(String file1, String file2) {
		String[] command = new String[] { getExec(), file1, file2 };
		CommandLauncher launcher = new CommandLauncher();
		launcher.execute(command, null, null, null);
	}

	public void threeWayDiff(String file1, String file2, String base) {

	}

	public String getExec() {
		String selectedTool = ClearCasePreferences.getExtDiffTool();
		Map<String, String> toolPathMap = PreferenceHelper
				.strToMap(ClearCasePreferences.getExtDiffExecPath());
		return PreferenceHelper.getExecPath(selectedTool, toolPathMap);

	}
	
	public String getMergeExec(){
		String selectedTool = ClearCasePreferences.getExtMergeTool();
		Map<String, String> toolPathMap = PreferenceHelper
				.strToMap(ClearCasePreferences.getExtMergeExecPath());
		return PreferenceHelper.getExecPath(selectedTool, toolPathMap);
	}

	@Override
	public boolean twoWayMerge(String file1, String file2) {
		String [] command = new String [] { getMergeExec(),file1,file2,"-m"};
		CommandLauncher launcher = new CommandLauncher();
		launcher.execute(command, null, null, null);
		if(launcher.getErrorOutput() != null){
			//Show error msg.
			System.out.println("Error: "+launcher.getErrorOutput());
		}
		return true;
	}

	@Override
	public boolean threeWayMerge(String file1, String file2, String base) {
		String [] command = new String [] { getMergeExec(),file1,file2,base,"-m"};
		CommandLauncher launcher = new CommandLauncher();
		launcher.execute(command, null, null, null);
		if(launcher.getErrorOutput() != null){
			//Show error msg.
			System.out.println("Error: "+launcher.getErrorOutput());
			return false;
		}
		return true;
		
	}

}

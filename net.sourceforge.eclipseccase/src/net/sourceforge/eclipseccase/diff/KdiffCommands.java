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
		ClearCasePreferences.getExtDiffExecPath();
		String selectedTool = ClearCasePreferences.getExtDiffTool();
		Map<String, String> toolPathMap = PreferenceHelper
				.strToMap(ClearCasePreferences.getExtDiffExecPath());
		return PreferenceHelper.getExecPath(selectedTool, toolPathMap);

	}

	@Override
	public void twoWayMerge(String file1, String file2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void threeWayMerge(String file1, String file2, String base) {
		// TODO Auto-generated method stub
		
	}

}

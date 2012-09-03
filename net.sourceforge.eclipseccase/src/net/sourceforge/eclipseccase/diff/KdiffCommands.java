package net.sourceforge.eclipseccase.diff;

import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import net.sourceforge.clearcase.ClearCaseException;
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
	public Status twoWayMerge(String file1, String file2) {
		String [] errMsg = null;
		String[] command = new String[] { getExec(), file1, file2, "-m" };
		CommandLauncher launcher = new CommandLauncher();
		launcher.execute(command, null, null, null);
		//Check if command was ok.
		if(launcher.getExitValue() != 0){
			errMsg = launcher.getErrorOutput();
			if(errMsg == null){
				return new Status(IStatus.ERROR, "Plugin id here", "An unknown error occurred!");
			}else{
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < errMsg.length; i++) {
					sb.append(errMsg[i]);
					sb.append('\n');
				}
				return new Status(IStatus.ERROR, "Plugin id here",sb.toString());
			}
		}
		
		//everything was ok!
		return new Status(IStatus.OK, "Plugin id here", "Merge was ok");
		
	}

	@Override
	public Status threeWayMerge(String file1, String file2, String base) {
		String [] errMsg = null;
		String[] command = new String[] { getExec(), file1, file2,base,"-m" };
		CommandLauncher launcher = new CommandLauncher();
		try{
		launcher.execute(command, null, null, null);
		}catch (ClearCaseException cce) {
			return new Status(IStatus.ERROR,"Plugin id here,",cce.getMessage(),cce);
		}
		//Check if command was ok.
		if(launcher.getExitValue() != 0){
			errMsg = launcher.getErrorOutput();
			if(errMsg == null){
				return new Status(IStatus.ERROR, "Plugin id here", "An unknown error occurred!");
			}else{
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < errMsg.length; i++) {
					sb.append(errMsg[i]);
					sb.append('\n');
				}
				return new Status(IStatus.ERROR, "Plugin id here",sb.toString());
			}
		}
		
		//everything was ok!
		return new Status(IStatus.OK, "Plugin id here", "Merge was ok");
		
	}
	

}

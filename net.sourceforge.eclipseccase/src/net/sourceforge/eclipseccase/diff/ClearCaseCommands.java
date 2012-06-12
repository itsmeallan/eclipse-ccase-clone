package net.sourceforge.eclipseccase.diff;

import net.sourceforge.eclipseccase.ClearCasePlugin;

public class ClearCaseCommands extends AbstractDifference{

	@Override
	public void twoWayDiff(String file1, String file2) {
		ClearCasePlugin.getEngine().compareWithVersion(file1, file2);
		
	}

	@Override
	public void threeWayDiff(String file1, String file2, String base) {
		// TODO Auto-generated method stub
		
	}

}

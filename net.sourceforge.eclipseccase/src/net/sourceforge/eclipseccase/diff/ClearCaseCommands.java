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

	@Override
	public void twoWayMerge(String file1, String file2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void threeWayMerge(String file1, String file2, String base) {
		ClearCasePlugin.getEngine().merge(file1, new String []{file2}, base, 0);
		
	}
	
	

}

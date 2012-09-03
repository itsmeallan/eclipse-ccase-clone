package net.sourceforge.eclipseccase.diff;

import org.eclipse.core.runtime.Status;


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
	public Status twoWayMerge(String file1, String file2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Status threeWayMerge(String file1, String file2, String base) {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public boolean twoWayMerge(String file1, String file2) {
//		System.out.println("Here should commands for 2 way merge exist!");
//		
//		return false;
//	}
//
//	@Override
//	public boolean threeWayMerge(String file1, String file2, String base) {
//		ClearCaseElementState state = ClearCasePlugin.getEngine().merge(file1, new String []{file2}, base, 0);
//		if(state.isMerged()){
//			return true;
//		}
//		
//		return false;
//	}
	
	

}

package net.sourceforge.eclipseccase.views.historyviewer;

import java.util.Vector;

import net.sourceforge.clearcase.ElementHistory;

import java.util.ArrayList;
import java.util.List;


/***
 * 
 * @author Mattias Rundgren
 *
 */
public enum ModelProvider {
	INSTANCE;

	private List<ElementHistory> elements;

	private ModelProvider() {
		elements = new ArrayList<ElementHistory>();
	}

	public List<ElementHistory> getElements() {
		return elements;
	}

	public void setElements(Vector<ElementHistory> result) {
		elements.clear();
		for(ElementHistory eh : result) 
		{
			elements.add(eh);
		}		
	}
}


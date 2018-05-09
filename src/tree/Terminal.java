package tree;

import java.util.ArrayList;
import java.util.List;

public class Terminal extends Node {
 
	
	public Terminal(String sIdentifier) {
		
		super(sIdentifier);
	}
	
public Terminal(String sIdentifier, String sForm, int iLeft, int iRight) {
		
		super(sIdentifier);
	}
	
	public String toString() {
		return getIdentifier();
	}
	
	public List<String> getYield() {
		List<String> lst = new ArrayList<String>();
		lst.add(getIdentifier());
		return lst;
	}
	
	public Object clone()
	{
		Terminal t = new Terminal(getIdentifier());
		return t;
	}
}

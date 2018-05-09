package grammar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;


/**
 * 
 * @author rtsarfat
 *
 * CLASS: Event
 * 
 * Definition: 
 * 		Structured events 
 * Role: 
 * 		Define the form of the 
 *      left-hand-side and right-hand-side of grammar rules 
 * Responsibility: 
 * 		keep track of symbols lists, 
 * 		check event identity
 * 
 * Usage: Each event may define multiple daughters separated by space
 *   
 */

public class Event {
	
	private List<String> m_lstSymbols = new ArrayList<String>();
	
	public Event(String s) {
		StringTokenizer st = new StringTokenizer(s);
		while (st.hasMoreTokens()) {
			String sym = (String) st.nextToken();
			addSymbol(sym);
		}
	}

	private void addSymbol(String sym) {
		getSymbols().add(sym);
	}

	public boolean equals(Object o)
	{
		return toString().equals(((Event)o).toString());
	}
	
	public String toString()
	{
		// return concatenation of symbols
		StringBuffer sb = new StringBuffer();
		Iterator<String> it = getSymbols().iterator();
		while (it.hasNext()) {
			String s = (String) it.next();
			sb.append(s);
			if (it.hasNext()) 
				sb.append(" ");	
		}
		return sb.toString();
	}
	
	public int hashCode()
	{
		return toString().hashCode();
	}

	public List<String> getSymbols() 
	{
		return m_lstSymbols;
	}

	public void setSymbols(List<String> symbols) 
	{
		m_lstSymbols = symbols;
	}

	
	
	
	
}


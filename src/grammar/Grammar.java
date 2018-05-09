package grammar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.CountMap;

/**
 * 
 * @author rtsarfat
 *
 * CLASS: Grammar
 * 
 * Definition: formally <N,T,S,R> 
 * Role: holds two collection of grammatical and lexical grammar rules  
 * Responsibility: define a start symbol 
 * 
 */

public class Grammar {

	protected Set<String> m_setStartSymbols = new HashSet<String>();
	protected Set<String> m_setTerminalSymbols = new HashSet<String>();
	protected Set<String> m_setNonTerminalSymbols = new HashSet<String>();

	protected Set<Rule> m_setSyntacticRules = new HashSet<Rule>();
	protected Set<Rule> m_setLexicalRules = new HashSet<Rule>();
	protected CountMap<Rule> m_cmRuleCounts = new CountMap<Rule>();
	protected Map<String, Set<Rule>> m_lexLexicalEntries = new HashMap<String, Set<Rule>>();
		
	public Grammar() {
		super();
	}
	
	public Map<String, Set<Rule>> getLexicalEntries() {
		return m_lexLexicalEntries;
	}

	public void setLexicalEntries(Map<String, Set<Rule>> m_lexLexicalEntries) {
		this.m_lexLexicalEntries = m_lexLexicalEntries;
	}

	public CountMap<Rule> getRuleCounts() {
		return m_cmRuleCounts;
	}

	public void addRule(Rule r)
	{	
		Event eLhs = r.getLHS();
		Event eRhs = r.getRHS();
				
		if (r.isLexical())
		{
			// update the sets T, N, R
			getLexicalRules().add(r);
			getNonTerminalSymbols().addAll(eLhs.getSymbols());
			getTerminalSymbols().addAll(eRhs.getSymbols());
			
			// update the dictionary
			if (!getLexicalEntries().containsKey(eRhs.toString()) )
				getLexicalEntries().put(eRhs.toString(), new HashSet<Rule>());
			getLexicalEntries().get(eRhs.toString()).add(r);
		}
		else 
		{
			// update the sets T, N, R
			getSyntacticRules().add(r);
			getNonTerminalSymbols().addAll(eLhs.getSymbols());
			getNonTerminalSymbols().addAll(eRhs.getSymbols());
		}
		
		// update the start symbol(s)
		if (r.isTop())
			getStartSymbols().add(eLhs.toString());
		
		// update the rule counts 
		getRuleCounts().increment(r);
	}
	

	public Set<String> getNonTerminalSymbols() {
		return m_setNonTerminalSymbols;
	}

	public Set<Rule> getSyntacticRules() {
		return m_setSyntacticRules;
	}

	public void setSyntacticRules(Set<Rule> syntacticRules) {
		m_setSyntacticRules = syntacticRules;
	}

	public Set<Rule> getLexicalRules() {
		return m_setLexicalRules;
	}

	public void setLexicalRules(Set<Rule> lexicalRules) {
		m_setLexicalRules = lexicalRules;
	}

	public Set<String> getStartSymbols() {
		return m_setStartSymbols;
	}

	public void setStartSymbols(Set<String> startSymbols) {
		m_setStartSymbols = startSymbols;
	}

	public Set<String> getTerminalSymbols() {
		return m_setTerminalSymbols;
	}

	public void setTerminalSymbols(Set<String> terminalSymbols) {
		m_setTerminalSymbols = terminalSymbols;
	}

	public int getNumberOfLexicalRuleTypes()
	{
		return getLexicalRules().size();
	}
	
	public int getNumberOfSyntacticRuleTypes()
	{
		return getSyntacticRules().size();
	}
	
	public int getNumberOfStartSymbols()
	{
		return getStartSymbols().size();
	}
	
	public int getNumberOfTerminalSymbols()
	{
		return getTerminalSymbols().size();
	}
	
	public void addStartSymbol(String string) {
		getStartSymbols().add(string);
	}

	public void removeStartSymbol(String string) {
		getStartSymbols().remove(string);
	}

	public void addAll(List<Rule> theRules) {
		for (int i = 0; i < theRules.size(); i++) {
			addRule(theRules.get(i));
		}
	}

}

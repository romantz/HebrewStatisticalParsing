package grammar;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 
 * @author rtsarfat
 *
 * CLASS: lexical entry
 * 
 * Definition: The set of analyses for a terminal of a language as listed in its lexicon
 * Role: Map the lexical entry onto a set of possible grammatical contexts (symbols) that can generate it
 * Responsibility: Maintain consistency with the set(s) of grammar terminal and pre-terminal categories
 * 
 */
public class LexicalEntry {

	protected String m_strLexeme = null;
	protected Set<Rule> m_setLexicalRules = new HashSet<Rule>();
	
	
	public LexicalEntry(String s) {
		setLexeme(s);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public boolean addRule(Rule rule)
	{
		return getLexicalRules().add(rule);
	}
	
	public Rule getRule(Rule rule)
	{
		Iterator<Rule> itr = getLexicalRules().iterator();
		while (itr.hasNext()) {
			Rule rule2 = (Rule) itr.next();
			if (rule2.equals(rule))
				return rule2;
		}
		return null;
	}
	
	public boolean removeRule(Rule rule)
	{
		return getLexicalRules().remove(rule);
	}
		
	public String getLexeme() {
		return m_strLexeme;
	}

	private void setLexeme(String lexeme) {
		m_strLexeme = lexeme;
	}

	public Set<Rule> getLexicalRules() {
		return m_setLexicalRules;
	}

	protected void setLexicalRules(Set<Rule> lexicalRules) {
		m_setLexicalRules = lexicalRules;
	}
	
	public int size()
	{
		return getLexicalRules().size();
	}

}

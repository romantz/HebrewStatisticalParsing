package decode;

import grammar.Grammar;
import grammar.Rule;

import java.util.*;
import java.util.Map.Entry;

import tree.Node;
import tree.Terminal;
import tree.Tree;

public class Decode {

	public static Set<Rule> m_setGrammarRules = null;
	public static Map<String, Set<Rule>> m_mapLexicalRules = null;
	
    /**
     * Implementation of a singleton pattern
     * Avoids redundant instances in memory 
     */
	public static Decode m_singDecoder = null;
	    
	public static Decode getInstance(Grammar g)
	{
		if (m_singDecoder == null)
		{
			m_singDecoder = new Decode();
			m_setGrammarRules = g.getSyntacticRules();
			m_mapLexicalRules = g.getLexicalEntries();			
		}
		return m_singDecoder;
	}


	public void addUnaryRules(ChartNode n){
		boolean foundNew;
		Map<String, Double> newTransitions = new HashMap<String, Double>();
		do {
			foundNew = false;
			for (Entry<String, ChartTransition> e: n.getTransitions()) {
				Set<Rule> ruleSet = m_mapLexicalRules.get(e.getKey());
				if (ruleSet != null) {
					for (Rule r : ruleSet) {
						String lhs = r.getLHS().toString();
						if (!newTransitions.containsKey(lhs)) {
							newTransitions.put(lhs, r.getMinusLogProb());
							foundNew = true;
						}
					}
				}
			}
		} while(foundNew);

		for(Entry<String, Double> e: newTransitions.entrySet()) {
			ChartTransition transition = new UnaryChartTransition(n, e.getValue(), true);
			n.addTransition(e.getKey(), transition);
		}
	}

	public Tree decode(List<String> input){
		
		// Done: Baseline Decoder
		//       Returns a flat tree with NN labels on all leaves 
		
		Tree t = new Tree(new Node("TOP"));
		Iterator<String> theInput = input.iterator();
		while (theInput.hasNext()) {
			String theWord = (String) theInput.next();
			Node preTerminal = new Node("NN");
			Terminal terminal = new Terminal(theWord);
			preTerminal.addDaughter(terminal);
			t.getRoot().addDaughter(preTerminal);
		}
		
		// TODO: CYK decoder
		//       if CYK fails, 
		//       use the baseline outcome

		for(Entry e: m_mapLexicalRules.entrySet()){
			System.out.println(e.getKey() + ", " + e.getValue());
		}
		System.out.println("---------");
		for(Rule r: m_setGrammarRules){
			System.out.println(r);
		}

		ChartNode[][] chart = new ChartNode[input.size() + 1][input.size() + 1];

		for(int i = 1; i <= input.size(); i++) {
			System.out.println(input.get(i - 1));
			chart[i - 1][i] = new ChartNode();
			for(Rule r: m_mapLexicalRules.get(input.get(i - 1))) {
				ChartTransition transition = new UnaryChartTransition(null, r.getMinusLogProb(), false);
				String parent = r.getLHS().toString();
				chart[i - 1][i].addTransition(parent, transition);
			}

			addUnaryRules(chart[i - 1][i]);

			for(int j = i - 2; j >= 0; j--){
				chart[j][i] = new ChartNode();
				for(int k = j + 1; k < i; k++){
					if(chart[j][k] != null && chart[k][i] != null) {
						for (Entry<String, ChartTransition> e1 : chart[j][k].getTransitions()) {
							for (Entry<String, ChartTransition> e2 : chart[k][i].getTransitions()) {
								String key = e1.getKey() + " " + e2.getKey();
								if (m_mapLexicalRules.containsKey(key)) {
									Set<Rule> ruleSet = m_mapLexicalRules.get(key);
									for (Rule r : ruleSet) {
										ChartTransition transition = new BinaryChartTransition(
												chart[j][k],
												chart[k][i],
												r.getMinusLogProb(),
												false
										);
										chart[j][i].addTransition(r.getLHS().toString(), transition);
									}
								}
							}
						}
					}
					addUnaryRules(chart[j][i]);
				}
			}
		}

		for(int i = 0; i <= input.size(); i++) {
			for(int j = 0; j <= input.size(); j++) {
				System.out.println("(" + j + ", " + i + "): " + chart[j][i]);
			}
		}

		return t;
		
	}

	private abstract class ChartTransition {
		boolean selfReference = false;
		ChartNode n1;
		double probability;

		public ChartTransition(ChartNode n1, double probability, boolean sr) {
			this.n1 = n1;
			this.probability = probability;
			this.selfReference = sr;
		}

		public ChartNode getN1() { return n1; }
		public double getProbability() { return probability; }
	}

	private class BinaryChartTransition extends ChartTransition {
		ChartNode n2;
		public BinaryChartTransition(ChartNode n1, ChartNode n2, double probability, boolean sr) {
			super(n1, probability, sr);
			this.n2 = n2;
		}
		public ChartNode getP2() { return n2; }

		public String toString(){
			return "(" + n1.toString() +", " + n2.toString() + ")" + ", " + probability;
		}
	}

	private class UnaryChartTransition extends ChartTransition {
		public UnaryChartTransition(ChartNode n1, double probability, boolean sr) {
			super(n1, probability, sr);
		}

		public String toString(){
			if(selfReference)
				return "this, " + probability;
			else if(n1 != null)
				return n1.toString() + ", " + probability;
			else
				return "null, " + probability;
		}
	}

//	private class TerminalTransition extends ChartTransition {
//		public TerminalTransition
//	}

	private class ChartNode{
		private List<Entry<String, ChartTransition>> nodeTransitions;

		public ChartNode(){
			nodeTransitions = new LinkedList<Entry<String, ChartTransition>>();
		}

		public void addTransition(String var, ChartTransition t){
			nodeTransitions.add(new AbstractMap.SimpleEntry<String, ChartTransition>(var, t));
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			for(Entry e: nodeTransitions) {
				sb.append(e.getKey() + " - ");
				//if(e.getValue() == this)
				//	sb.append(e.getKey() + ": this\n");
				//else
				//	sb.append(e.getKey() + ": " + e.getValue() +"\n");
			}
			return sb.toString();
		}

		public List<Entry<String, ChartTransition>> getTransitions(){
			return nodeTransitions;
		}
	}
	
	
}

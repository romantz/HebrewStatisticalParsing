package decode;

import grammar.Grammar;
import grammar.Rule;

import java.util.*;

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

		for(java.util.Map.Entry e: m_mapLexicalRules.entrySet()){
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
				ChartTransition transition = new UnaryChartTransition(null, r.getMinusLogProb());
				chart[i - 1][i].addTransition(r.getLHS().toString(), transition);
			}
			for(int j = i - 2; j >= 0; j--){
				for(int k = j + 1; k < i; k++){
					chart[j][i] = new ChartNode();
					if(chart[j][k] != null && chart[k][i] != null) {
						for (java.util.Map.Entry<String, ChartTransition> e1 : chart[j][k].getTransitions().entrySet()) {
							for (java.util.Map.Entry<String, ChartTransition> e2 : chart[k][i].getTransitions().entrySet()) {
								String key = e1.getKey() + " " + e2.getKey();
								if (m_mapLexicalRules.containsKey(key)) {
									Set<Rule> ruleSet = m_mapLexicalRules.get(key);
									for (Rule r : ruleSet) {
										ChartTransition transition = new BinaryChartTransition(
												chart[j][k],
												chart[k][i],
												r.getMinusLogProb()
										);
										chart[j][i].addTransition(r.getLHS().toString(), transition);
									}
								}
							}
						}
					}
				}
			}
		}

		for(int i = 0; i < input.size(); i++) {
			for(int j = 0; j <= input.size(); j++) {
				System.out.println("(" + i + ", " + j + "): " + chart[i][j]);
			}
		}

		return t;
		
	}

	private abstract class ChartTransition {
		ChartNode n1;
		double probability;

		public ChartTransition(ChartNode n1, double probability) {
			this.n1 = n1;
			this.probability = probability;
		}

		public ChartNode getN1() { return n1; }
		public double getProbability() { return probability; }
	}

	private class BinaryChartTransition extends ChartTransition {
		ChartNode n2;
		public BinaryChartTransition(ChartNode n1, ChartNode n2, double probability) {
			super(n1, probability);
			this.n2 = n2;
		}
		public ChartNode getP2() { return n2; }

		public String toString(){
			return "(" + n1.toString() +", " + n2.toString() + ")" + ", " + probability;
		}
	}

	private class UnaryChartTransition extends ChartTransition {
		public UnaryChartTransition(ChartNode n1, double probability) {
			super(n1, probability);
		}

		public String toString(){
			if(n1 != null)
				return n1.toString() + ", " + probability;
			else
				return "null, " + probability;
		}
	}

//	private class TerminalTransition extends ChartTransition {
//		public TerminalTransition
//	}

	private class ChartNode{
		private HashMap<String, ChartTransition> nodeTransitions;

		public ChartNode(){
			nodeTransitions = new HashMap<String, ChartTransition>();
		}

		public void addTransition(String var, ChartTransition t){
			nodeTransitions.put(var, t);
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			for(java.util.Map.Entry e: nodeTransitions.entrySet()) {
				sb.append(e.getKey() + ": " + e.getValue() +"\n");
			}
			return sb.toString();
		}

		public HashMap<String, ChartTransition> getTransitions(){
			return nodeTransitions;
		}
	}
	
	
}

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

	private final double MAX_PROBABILITY = 0.0;
	private final String START_VARIABLE = "S";

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
		Set<Rule> newAppliedRules = new HashSet<Rule>();
		List<ChartTransition> allNewTransitions = new LinkedList<ChartTransition>();
		List<ChartTransition> currentNewTransitions = new LinkedList<ChartTransition>();
		List<ChartTransition> previousNewTransitions = new LinkedList<ChartTransition>();

		for (ChartTransition t: n.getTransitions()) {
			for (Rule r : m_setGrammarRules) {
				if(r.getRHS().toString().equals(t.getVar()) && !r.getLHS().toString().equals(r.getRHS().toString())){
					ChartTransition t2 = new UnaryChartTransition(
							t,
							r.getMinusLogProb() + t.getProbability(),
							r.getLHS().toString());
					currentNewTransitions.add(t2);
					newAppliedRules.add(r);
				}
			}
		}

		allNewTransitions.addAll(currentNewTransitions);
		while(!currentNewTransitions.isEmpty()) {
			previousNewTransitions = currentNewTransitions;
			currentNewTransitions = new LinkedList<ChartTransition>();
			for (ChartTransition t: previousNewTransitions) {
				for (Rule r : m_setGrammarRules) {
					if(r.getRHS().toString().equals(t.getVar()) &&
							!r.getLHS().toString().equals(r.getRHS().toString()) &&
							!newAppliedRules.contains(r)
							){
						ChartTransition t2 = new UnaryChartTransition(
								t,
								r.getMinusLogProb() + t.getProbability(),
								r.getLHS().toString());
						currentNewTransitions.add(t2);
						newAppliedRules.add(r);
					}
				}
			}
			allNewTransitions.addAll(currentNewTransitions);
		}

		for(ChartTransition t: allNewTransitions)
			n.addTransition(t);
	}

	public Node constructNodeFromTransition(ChartTransition transition){
		Node n = new Node(transition.getVar());
		for(ChartTransition t: transition.getTransitions()) {
			n.addDaughter(constructNodeFromTransition(t));
		}
		return n;
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

		for(Rule r: m_setGrammarRules){
			System.out.println(r);
		}
		for(Entry e: m_mapLexicalRules.entrySet()){
			System.out.println(e.getKey()+" , " + e.getValue());
		}

		ChartNode[][] chart = new ChartNode[input.size() + 1][input.size() + 1];

		for(int i = 1; i <= input.size(); i++) {
			System.out.println(i);
			chart[i - 1][i] = new ChartNode();
			TerminalTransition terminal = new TerminalTransition(input.get(i - 1));
			if(m_mapLexicalRules.containsKey(input.get(i - 1))) {
				for (Rule r : m_mapLexicalRules.get(input.get(i - 1))) {
					ChartTransition transition = new UnaryChartTransition(
							terminal,
							r.getMinusLogProb(),
							r.getLHS().toString());
					chart[i - 1][i].addTransition(transition);
				}
			}
			else {
				ChartTransition transition = new UnaryChartTransition(
						terminal,
						MAX_PROBABILITY,
						"NN");
				chart[i - 1][i].addTransition(transition);
			}

			addUnaryRules(chart[i - 1][i]);

			for(int j = i - 2; j >= 0; j--){
				chart[j][i] = new ChartNode();
				for(int k = j + 1; k < i; k++){
					if(chart[j][k] != null && chart[k][i] != null) {
						for (ChartTransition t1: chart[j][k].getTransitions()) {
							for (ChartTransition t2: chart[k][i].getTransitions()) {
								String key = t1.getVar() + " " + t2.getVar();
								for(Rule r: m_setGrammarRules) {
									if(r.getRHS().toString().equals(key)){
										ChartTransition transition = new BinaryChartTransition(
												t1,
												t2,
												r.getMinusLogProb() + t1.getProbability() + t2.getProbability(),
												r.getLHS().toString()
										);
										chart[j][i].addTransition(transition);
									}
								}
							}
						}
					}
				}
				addUnaryRules(chart[j][i]);
			}
		}

		double minProb = Double.MAX_VALUE;
		ChartTransition bestTransition = null;
		if(chart[0][input.size()] != null) {
			for (ChartTransition transition : chart[0][input.size()].getTransitions()) {
				if (transition.variable.equals(START_VARIABLE) && transition.getProbability() < minProb) {
					minProb = transition.getProbability();
					bestTransition = transition;
				}
			}
		}

		System.out.println("-----");
		for(int i = 0; i < chart.length; i++){
			for(int j = 0; j < chart[i].length; j++){
				System.out.println("(" + j +", " + i +")" + chart[j][i]);
			}
		}


		if(bestTransition == null)
			return t;

		Tree t2 = new Tree(new Node("TOP"));
		t2.getRoot().addDaughter(constructNodeFromTransition(bestTransition));
		return t2;
		
	}

	private abstract class ChartTransition {
		ChartTransition t1;
		double probability;
		String variable;

		public ChartTransition(ChartTransition t1, double probability, String var) {
			this.t1 = t1;
			this.probability = probability;
			this.variable = var;
		}

		public abstract List<ChartTransition> getTransitions();

		public ChartTransition getT1() { return t1; }
		public double getProbability() { return probability; }
		public String getVar() { return variable; }
	}

	private class BinaryChartTransition extends ChartTransition {
		ChartTransition t2;
		public BinaryChartTransition(
				ChartTransition t1,
				ChartTransition t2,
				double probability,
				String var) {
			super(t1, probability, var);
			this.t2 = t2;
		}
		public ChartTransition getT2() { return t2; }

		public List<ChartTransition> getTransitions(){
			List<ChartTransition> transitions = new LinkedList<ChartTransition>();
			transitions.add(t1);
			transitions.add(t2);
			return transitions;
		}

		public String toString(){
			return "(" + variable + " (" + t1.toString() +" " + t2.toString() + "))";
		}
	}

	private class UnaryChartTransition extends ChartTransition {
		public UnaryChartTransition(ChartTransition t1, double probability, String var) {
			super(t1, probability, var);
		}

		public String toString(){
			return "(" + variable + " " + t1.toString() + ")";
		}

		public List<ChartTransition> getTransitions(){
			List<ChartTransition> transitions = new LinkedList<ChartTransition>();
			transitions.add(t1);
			return transitions;
		}
	}

	private class TerminalTransition extends ChartTransition {
		public TerminalTransition(String var){
			super(null, MAX_PROBABILITY, var);
		}
		public String toString(){
			return variable;
		}

		public List<ChartTransition> getTransitions(){
			List<ChartTransition> transitions = new LinkedList<ChartTransition>();
			return transitions;
		}
	}

	private class ChartNode{
		private List<ChartTransition> nodeTransitions;

		public ChartNode(){
			nodeTransitions = new LinkedList<ChartTransition>();
		}

		public void addTransition(ChartTransition t){
			nodeTransitions.add(t);
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			for(ChartTransition t: nodeTransitions) {
				sb.append(t.toString() + "\n");
			}
			return sb.toString();
		}

		public List<ChartTransition> getTransitions(){
			return nodeTransitions;
		}
	}
	
	
}

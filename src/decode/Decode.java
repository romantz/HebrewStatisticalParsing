package decode;

import grammar.Grammar;
import grammar.Rule;

import java.util.*;

import train.Pair;
import tree.Node;
import tree.Terminal;
import tree.Tree;

public class Decode {

	public static Set<Rule> m_setGrammarRules = null;
	public static Map<String, Set<Rule>> m_mapLexicalRules = null;

	public static Map<String, Set<Rule>> m_mapGrammarRules = null;
	public static Map<String, Set<Rule>> m_mapUnaryRules = null;
	public static Map<String, Set<Rule>> m_mapLeftSymbolBinaryRules = null;
	public static Map<String, Set<Rule>> m_mapRightSymbolBinaryRules = null;

	public static int ruleCount = 0;
	public static Map<Rule, Integer> m_mapRuleToNum = null;
	public static Map<Integer, Rule> m_mapNumToRule = null;


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

			m_mapUnaryRules = new HashMap<String, Set<Rule>>();
			m_mapLeftSymbolBinaryRules = new HashMap<String, Set<Rule>>();
			m_mapRightSymbolBinaryRules = new HashMap<String, Set<Rule>>();
			m_mapGrammarRules = new HashMap<String, Set<Rule>>();

			m_mapRuleToNum = new HashMap<Rule, Integer>();
			m_mapNumToRule = new HashMap<Integer, Rule>();

			for(Rule r: m_setGrammarRules){
				m_mapRuleToNum.put(r, ruleCount);
				m_mapNumToRule.put(ruleCount, r);

				if(r.getRHS().getSymbols().size() == 2){
					String firstSymbol = r.getRHS().getSymbols().get(0);
					Set s = m_mapLeftSymbolBinaryRules.get(firstSymbol);
					if(s == null) {
						Set<Rule> ruleSet = new HashSet<Rule>();
						ruleSet.add(r);
						m_mapLeftSymbolBinaryRules.put(firstSymbol, ruleSet);
					} else {
						s.add(r);
					}

					String secondSymbol = r.getRHS().getSymbols().get(1);
					s = m_mapRightSymbolBinaryRules.get(secondSymbol);
					if(s == null) {
						Set<Rule> ruleSet = new HashSet<Rule>();
						ruleSet.add(r);
						m_mapRightSymbolBinaryRules.put(secondSymbol, ruleSet);
					} else {
						s.add(r);
					}
				}

				Set s = m_mapGrammarRules.get(r.getRHS().toString());
				if(s == null) {
					Set<Rule> ruleSet = new HashSet<Rule>();
					ruleSet.add(r);
					m_mapGrammarRules.put(r.getRHS().toString(), ruleSet);
					if(r.getRHS().getSymbols().size() == 1)
						m_mapUnaryRules.put(r.getRHS().toString(), ruleSet);
				} else{
					s.add(r);
					if(r.getRHS().getSymbols().size() == 1)
						m_mapUnaryRules.get(r.getRHS().toString()).add(r);
				}

				ruleCount++;
			}

//			for(Map.Entry e: m_mapRightSymbolBinaryRules.entrySet()){
//				System.out.println(e.getKey()+" , " + e.getValue());
//			}
		}

		return m_singDecoder;
	}


	public void addUnaryRules(ChartNode n){
		Set<Rule> newAppliedRules = new HashSet<Rule>();
		List<ChartTransition> currentNewTransitions = new ArrayList<ChartTransition>();
		List<ChartTransition> previousNewTransitions = new ArrayList<ChartTransition>();

		for (ChartTransition t: n.getTransitions().values()) {
			Set<Rule> ruleSet = m_mapUnaryRules.get(t.getVar());
			if(ruleSet != null) {
				for (Rule r : ruleSet) {
					ChartTransition t2 = new UnaryChartTransition(
							t,
							r.getMinusLogProb() + t.getProbability(),
							r.getLHS().toString());
					currentNewTransitions.add(t2);
					newAppliedRules.add(r);
				}
			}
		}

		for(ChartTransition t: currentNewTransitions)
			n.addTransition(t);

		while(!currentNewTransitions.isEmpty()) {
			previousNewTransitions = currentNewTransitions;
			currentNewTransitions = new LinkedList<ChartTransition>();
			for (ChartTransition t: previousNewTransitions) {
				Set<Rule> ruleSet = m_mapUnaryRules.get(t.getVar());
				if(ruleSet != null) {
					for (Rule r : ruleSet) {
						if (!newAppliedRules.contains(r)) {
							ChartTransition t2 = new UnaryChartTransition(
									t,
									r.getMinusLogProb() + t.getProbability(),
									r.getLHS().toString());
							currentNewTransitions.add(t2);
							n.addTransition(t2);
							newAppliedRules.add(r);
						}
					}
				}
			}
		}
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

		/*System.out.println("---------");
		for(Rule r: m_setGrammarRules){
			System.out.println(r);
		}
		System.out.println("---------");
		for(Entry e: m_mapLexicalRules.entrySet()){
			System.out.println(e.getKey()+" , " + e.getValue());
		}
		System.out.println("---------");
		for(Entry e: m_mapUnaryRules.entrySet()){
			System.out.println("xxx "+e.getKey()+" , " + e.getValue());
		}
		System.out.println("---------");
		for(Entry e: m_mapGrammarRules.entrySet()){
			System.out.println("yyy "+e.getKey()+" , " + e.getValue());
		}
		System.out.println("---------");*/

		ChartNode[][] chart = new ChartNode[input.size() + 1][input.size() + 1];

		for(int i = 1; i <= input.size(); i++) {
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
			chart[i - 1][i].calculateRuleArrays();
			for (ChartTransition tr : chart[i - 1][i].getTransitions().values()) {
				System.out.println("----- " + tr);
			}

			for(int j = i - 2; j >= 0; j--){
				chart[j][i] = new ChartNode();
				for(int k = j + 1; k < i; k++){
					if(chart[j][k] != null && chart[k][i] != null) {
						HashMap<Integer, ChartTransition> leftRules = chart[j][k].getLeftRules();
						HashMap<Integer, ChartTransition> rightRules = chart[k][i].getRightRules();

						HashMap<Integer, Pair<ChartTransition, ChartTransition>> intersection =
								intersectHashMaps(leftRules, rightRules);
						for(Map.Entry<Integer, Pair<ChartTransition, ChartTransition>> e: intersection.entrySet()) {
							Rule r = m_mapNumToRule.get(e.getKey());

							ChartTransition newTransition = new BinaryChartTransition(
									e.getValue().x,
									e.getValue().y,
									r.getMinusLogProb() +
											e.getValue().x.getProbability() +
											e.getValue().y.getProbability(),
									r.getLHS().toString()
							);
							chart[j][i].addTransition(newTransition);
						}
					}
				}
				addUnaryRules(chart[j][i]);
				chart[j][i].calculateRuleArrays();
				System.out.println(chart[j][i].getTransitions().size());
			}
		}

//		for(int i = 0; i < chart[0].length; i++) {
//			for (int j = 0; j < chart.length; j++) {
//				System.out.println(i + ", " + j);
//				if(chart[j][i] != null)
//				for (ChartTransition tr : chart[j][i].getTransitions().values()) {
//					System.out.println("----- " + tr);
//				}
//			}
//		}

		double minProb = Double.MAX_VALUE;
		ChartTransition bestTransition = null;
		if(chart[0][input.size()] != null) {
			for (ChartTransition transition : chart[0][input.size()].getTransitions().values()) {
				if (transition.variable.equals(START_VARIABLE) && transition.getProbability() < minProb) {
					minProb = transition.getProbability();
					bestTransition = transition;
				}
			}
		}

		if(bestTransition == null)
			return t;

		Tree t2 = new Tree(new Node("TOP"));
		t2.getRoot().addDaughter(constructNodeFromTransition(bestTransition));
		return t2;
		
	}

	public static HashMap<Integer, Pair<ChartTransition, ChartTransition>> intersectHashMaps(
			HashMap<Integer, ChartTransition> left,
			HashMap<Integer, ChartTransition> right){

		HashMap<Integer, Pair<ChartTransition, ChartTransition>> intersection =
				new HashMap<Integer, Pair<ChartTransition, ChartTransition>>();

		if(left.size() <= right.size()) {
			for (Map.Entry<Integer, ChartTransition> e : left.entrySet()) {
				ChartTransition t = right.get(e.getKey());
				if (t != null)
					intersection.put(e.getKey(), new Pair(e.getValue(), t));
			}
		} else {
			for (Map.Entry<Integer, ChartTransition> e : right.entrySet()) {
				ChartTransition t = left.get(e.getKey());
				if (t != null)
					intersection.put(e.getKey(), new Pair(t, e.getValue()));
			}
		}
		return intersection;
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
		private Map<String, ChartTransition> nodeTransitions;
		private HashMap<Integer, ChartTransition> rightRules;
		private HashMap<Integer, ChartTransition> leftRules;


		public ChartNode(){
			nodeTransitions = new HashMap<String, ChartTransition>();
		}

		public void calculateRuleArrays(){
			leftRules = new HashMap<Integer, ChartTransition>();
			rightRules = new HashMap<Integer, ChartTransition>();

			for(ChartTransition t: nodeTransitions.values()) {
				Set<Rule> currentLeftRules = m_mapLeftSymbolBinaryRules.get(t.getVar());
				if(currentLeftRules != null)
					for(Rule r: currentLeftRules){
						leftRules.put(m_mapRuleToNum.get(r), t);
					}

				Set<Rule> currentRightRules = m_mapRightSymbolBinaryRules.get(t.getVar());
				if(currentRightRules != null)
					for(Rule r: currentRightRules){
						rightRules.put(m_mapRuleToNum.get(r), t);
					}
			}
		}

		public HashMap<Integer, ChartTransition> getRightRules(){ return rightRules; }
		public HashMap<Integer, ChartTransition> getLeftRules(){ return leftRules; }

		public void addTransition(ChartTransition t){
			String var = t.variable;
			ChartTransition oldTransition = nodeTransitions.get(var);
			if(oldTransition == null || t.getProbability() < oldTransition.getProbability()){
				nodeTransitions.put(var, t);
			}
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			for(ChartTransition t: nodeTransitions.values()) {
				sb.append(t.toString() + "," + t.getProbability() + "\n");
			}
			return sb.toString();
		}

		public Map<String, ChartTransition> getTransitions(){
			return nodeTransitions;
		}
	}
}

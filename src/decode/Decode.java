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

	// A mapping from a string to a set of rules in which the rhs is equal to the string
	public static Map<String, Set<Rule>> m_mapGrammarRules = null;

	// A mapping from a string to a set of unary rules in which the rhs is equal to the string
	public static Map<String, Set<Rule>> m_mapUnaryRules = null;

	// A mapping from a string to a set of binary rules in which the rhs' 1st symbol is equal to the string
	public static Map<String, Set<Rule>> m_mapLeftSymbolBinaryRules = null;

	// A mapping from a string to a set of binary rules in which the rhs' 2nd symbol is equal to the string
	public static Map<String, Set<Rule>> m_mapRightSymbolBinaryRules = null;

	// Counter for the number of rules
	public static int ruleCount = 0;

	// The following two maps are used to give every rule a unique number
	// this way, string comparison is avoided in order to improve performance
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

			// Populate all the HashMaps that will be used in processing
			for(Rule r: m_setGrammarRules){
				m_mapRuleToNum.put(r, ruleCount);
				m_mapNumToRule.put(ruleCount, r);

				// Check if this is a binary rule
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
		}

		return m_singDecoder;
	}

	/**
	 * Add all relevant unary rule after all the binary rules for the chart node have been calculated
	 * @param cn the ChartNode for which unary rules are calculated
	 */
	public void addUnaryRules(ChartNode cn){
		Set<Rule> newAppliedRules = new HashSet<Rule>();
		List<ChartTransition> currentNewTransitions = new ArrayList<ChartTransition>();
		List<ChartTransition> previousNewTransitions = new ArrayList<ChartTransition>();

		for (ChartTransition t: cn.getTransitions().values()) {
			Set<Rule> ruleSet = m_mapUnaryRules.get(t.getVar());
			if(ruleSet != null) {
				for (Rule r : ruleSet) {
					// Add current unary rule to chart node
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
			cn.addTransition(t);

		// Keep adding unary transition to the chart node as long as new unary
		// transitions have been introduced in the previous iteration
		while(!currentNewTransitions.isEmpty()) {
			previousNewTransitions = currentNewTransitions;
			currentNewTransitions = new LinkedList<ChartTransition>();
			for (ChartTransition t: previousNewTransitions) {
				Set<Rule> ruleSet = m_mapUnaryRules.get(t.getVar());
				if(ruleSet != null) {
					for (Rule r : ruleSet) {
						// Check if this rule was already processed. This is used
						// to avoid entering an infinite loop
						if (!newAppliedRules.contains(r)) {
							ChartTransition t2 = new UnaryChartTransition(
									t,
									r.getMinusLogProb() + t.getProbability(),
									r.getLHS().toString());
							currentNewTransitions.add(t2);
							cn.addTransition(t2);
							newAppliedRules.add(r);
						}
					}
				}
			}
		}
	}

	/**
	 * Used to construct the parse tree
	 * @param transition the current transition in the chart
	 * @return a node constructed from the given transition
	 */
	public Node constructNodeFromTransition(ChartTransition transition){
		Node n = new Node(transition.getVar());
		for(ChartTransition t: transition.getTransitions()) {
			n.addDaughter(constructNodeFromTransition(t));
		}
		return n;
	}

	/**
	 * A heuristic to tag terminal values
	 * @param cn the current ChartNode to process
	 * @param word the given word in the given sentence
	 */
	public void terminalTaggingHeuristic(ChartNode cn, String word){
		TerminalTransition terminal = new TerminalTransition(word);
		String wordSearched = "";
		boolean foundWord = false;

		// Check if the lexical rules contain the given word in the rhs of a rule.
		// If not, check if the word ends in "IM" or "WT" (hebrew plural), and if so, look for the
		// word without the two last letters in the lexical rules
		if(m_mapLexicalRules.containsKey(word)) {
			wordSearched = word;
			foundWord = true;
		} else if(word.length() > 2 &&
				(word.substring(word.length() - 2).equals("IM") || word.substring(word.length() - 2).equals("WT")) &&
				m_mapLexicalRules.containsKey(word.substring(0, word.length() - 2))){
			wordSearched = word.substring(0, word.length() - 2);
			foundWord = true;
		}

		// If such a word was found, add the unary transition to it
		if(foundWord) {
			for (Rule r : m_mapLexicalRules.get(wordSearched)) {
				ChartTransition transition = new UnaryChartTransition(
						terminal,
						r.getMinusLogProb(),
						r.getLHS().toString());
				cn.addTransition(transition);
			}
		}
		// Otherwise, the word has not been found, so we employ smoothing and give it all
		// the possible tags with their respective probabilities
		else{
			boolean mightBeVerb = false;

			// If the word is unseen and its first letter is an "AITN" letter, there is a high probability
			// that this is a verb
			if(word.charAt(0) == 'A' || word.charAt(0) == 'I' || word.charAt(0) == 'T' || word.charAt(0) == 'N' ||
					word.charAt(0) == 'M') {
				mightBeVerb = true;
			}

			// Get all rules of the form POS-->UNKNOWN as a smoothing technique
			for(Rule r: m_mapLexicalRules.get("UNKNOWN")) {
				double prob = r.getMinusLogProb();
				String lhs = r.getLHS().toString();

				// Judging by the given train and gold sets, unseen words are often adjectives,
				// so if the current rule is JJ, or VB and the word starts with a "AITN" letter,
				// it's probability is then divided by 2
				if(lhs.equals("JJ") || (lhs.equals("VB") && mightBeVerb))
					prob /= 2;

				ChartTransition transition = new UnaryChartTransition(
						terminal,
						prob,
						r.getLHS().toString());
				cn.addTransition(transition);
			}
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


		//CKY implementation
		ChartNode[][] chart = new ChartNode[input.size() + 1][input.size() + 1];

		for(int i = 1; i <= input.size(); i++) {
			chart[i - 1][i] = new ChartNode();
			terminalTaggingHeuristic(chart[i - 1][i], input.get(i - 1));

			addUnaryRules(chart[i - 1][i]);
			chart[i - 1][i].calculateRuleMaps();

			for(int j = i - 2; j >= 0; j--){
				chart[j][i] = new ChartNode();
				for(int k = j + 1; k < i; k++){
					if(chart[j][k] != null && chart[k][i] != null) {
						// Get map of all rules which have the 1st symbol in the rhs of the rule
						// as a transition in chart[j][k]
						HashMap<Integer, ChartTransition> leftRules = chart[j][k].getLeftRules();

						// Get map of all rules which have the 2nd symbol in the rhs of the rule
						// as a transition in chart[k][i]
						HashMap<Integer, ChartTransition> rightRules = chart[k][i].getRightRules();

						// Intersect them to get all rules that can be derived from chart[j][k] and chart[k][i]
						HashMap<Integer, Pair<ChartTransition, ChartTransition>> intersection =
								intersectHashMaps(leftRules, rightRules);

						// Add every rule in the intersection to chart[j][i]
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
				// Add all the possible unary rules that can be derived from chart[j][i]
				addUnaryRules(chart[j][i]);

				// Calculate all rules that can be derived from chart[j][i] by having their 1st of 2nd
				// symbol as a transition in chart[j][i]
				chart[j][i].calculateRuleMaps();
			}
		}

		// Look for the start symbol in chart[0][input.size()] which gives the minimum probability transition
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

		// If CKY returned no valid parse, return the result of the dummy parser
		if(bestTransition == null)
			return t;

		// Construct the parse tree from the start symbol transition with smallest probability
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

		public void calculateRuleMaps(){
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

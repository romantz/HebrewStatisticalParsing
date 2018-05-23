package train;

import grammar.Event;
import grammar.Grammar;
import grammar.Rule;

import java.util.*;

import tree.Node;
import tree.Tree;
import treebank.Treebank;
import utils.CountMap;


/**
 * 
 * @author Reut Tsarfaty
 * 
 * CLASS: Train
 * 
 * Definition: a learning component
 * Role: reads off a grammar from a treebank
 * Responsibility: keeps track of rule counts
 * 
 */

public class Train {

	public static final String MARKOVIZATION_SYMBOL = "@";
	int h = -1;

    /**
     * Implementation of a singleton pattern
     * Avoids redundant instances in memory 
     */
	public static Train m_singTrainer = null;
	    
	public static Train getInstance()
	{
		if (m_singTrainer == null)
		{
			m_singTrainer = new Train();
		}
		return m_singTrainer;
	}
	
	public static void main(String[] args) {

	}
	
	public Grammar train(Treebank myTreebank)
	{
		Grammar myGrammar = new Grammar();
		Map<String, Integer> unknownCount = new HashMap<String, Integer>();
		HashMap<Rule, List<Rule>> binarizationMap = new HashMap<Rule, List<Rule>>();
		for (int i = 0; i < myTreebank.size(); i++) {
			Tree myTree = myTreebank.getAnalyses().get(i);
			List<Rule> theRules = getRules(myTree);
			List<Rule> actualRules = new ArrayList<Rule>();

			for(Rule r: theRules) {
				List<String> rhsSymbols = r.getRHS().getSymbols();
				String newNonTerminal = "";
				if(rhsSymbols.size() > 2){
					if(binarizationMap.containsKey(r)){
						actualRules.addAll(binarizationMap.get(r));
					} else {
						List<Rule> currentBinarization = new ArrayList<Rule>();
						ArrayList<String> lastHSymbols = new ArrayList<String>();
						String parent = r.getLHS().toString();

						for (int k = 0; k < h - 1; k++){
							lastHSymbols.add("-");
						}
						if(h > 0 || h == -1)
							lastHSymbols.add(rhsSymbols.get(0));

						Event eLHS = new Event(parent);
						newNonTerminal = Arrays.toString(lastHSymbols.toArray()).replace(" ", "")
								+ MARKOVIZATION_SYMBOL + eLHS.toString();
						Event eRHS = new Event(rhsSymbols.get(0) + " " + newNonTerminal);
						Rule r1 = new Rule(eLHS, eRHS);
						currentBinarization.add(r1);
						r1.setTop(r.isTop());
						for (int j = 1; j < rhsSymbols.size() - 1; j++) {
							if(h > 0){
								for (int k = 1; k < h; k++){
									lastHSymbols.set(k - 1, lastHSymbols.get(k));
								}
								lastHSymbols.set(h - 1, rhsSymbols.get(j));
							}
							if(h == -1)
								lastHSymbols.add(rhsSymbols.get(j));
							eLHS = new Event(newNonTerminal);
							newNonTerminal = Arrays.toString(lastHSymbols.toArray()).replace(" ", "")
									+ MARKOVIZATION_SYMBOL + parent;
							eRHS = new Event(rhsSymbols.get(j) + " " + newNonTerminal);
							Rule r2 = new Rule(eLHS, eRHS);
							currentBinarization.add(r2);
						}
						eLHS = new Event(newNonTerminal);
						eRHS = new Event(rhsSymbols.get(rhsSymbols.size() - 1));
						Rule r3 = new Rule(eLHS, eRHS);
						currentBinarization.add(r3);
						actualRules.addAll(currentBinarization);
						binarizationMap.put(r, currentBinarization);
					}
				} else {
					actualRules.add(r);
				}
			}

			myGrammar.addAll(actualRules);
		}

		CountMap<Event> lhsCounts = new CountMap<Event>();

		for(java.util.Map.Entry e: myGrammar.getRuleCounts().entrySet()){
			Event lhs = ((Rule)e.getKey()).getLHS();
			if(!lhsCounts.containsKey(lhs))
				lhsCounts.put(lhs, (Integer)e.getValue());
			else
				lhsCounts.put(lhs, lhsCounts.get(lhs) + (Integer)e.getValue());
		}

		int totalUnknownCount = 0;
		for(Rule r: myGrammar.getLexicalRules()){
			List<String> rhsSymbols = r.getRHS().getSymbols();
			if(rhsSymbols.size() == 1){
				String lhs = r.getLHS().toString();
				Integer count = unknownCount.get(lhs);
				totalUnknownCount++;
				if(count == null){
					unknownCount.put(lhs, 1);
				}
				else {
					unknownCount.put(lhs, count + 1);
				}
			}
		}

		for(java.util.Map.Entry e: myGrammar.getRuleCounts().entrySet()){
			//
			// System.out.println("========== " + e.getKey()+", "+e.getValue());
			Rule r = (Rule)e.getKey();
			Event lhs = r.getLHS();
			r.setMinusLogProb(
					(-1) * Math.log((Integer)e.getValue() / ((Integer)lhsCounts.get(lhs)).doubleValue()));
		}

		for(java.util.Map.Entry e: unknownCount.entrySet()){
			Rule r = new Rule((String)e.getKey(), "UNKNOWN", true);
			r.setMinusLogProb(
					(-1) * Math.log((Integer)e.getValue() / ((Integer)totalUnknownCount).doubleValue()));
			myGrammar.addRule(r);
		}

		return myGrammar;
	}

	public List<Rule> getRules(Tree myTree)
	{
		List<Rule> theRules = new ArrayList<Rule>();
		
		List<Node> myNodes = myTree.getNodes();
		for (int j = 0; j < myNodes.size(); j++) {
			Node myNode = myNodes.get(j);
			if (myNode.isInternal())
			{
				Event eLHS = new Event(myNode.getIdentifier());
				Iterator<Node> theDaughters = myNode.getDaughters().iterator();
				StringBuffer sb = new StringBuffer();
				while (theDaughters.hasNext()) {
					Node n = (Node) theDaughters.next();
					sb.append(n.getIdentifier());
					if (theDaughters.hasNext())
						sb.append(" ");
				}
				Event eRHS = new Event (sb.toString());
				Rule theRule = new Rule(eLHS, eRHS);
				if (myNode.isPreTerminal())
					theRule.setLexical(true);
				if (myNode.isRoot())
					theRule.setTop(true);
				theRules.add(theRule);
			}	
		}
		return theRules;
	}
	
}

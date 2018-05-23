package parse;

import grammar.Grammar;
import grammar.Rule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import bracketimport.TreebankReader;

import decode.Decode;
import train.Train;

import tree.Node;
import tree.Tree;
import treebank.Treebank;

import utils.LineWriter;

public class Parse {

	/**
	 *
	 * @author Reut Tsarfaty
	 * @date 27 April 2013
	 * 
	 * @param train-set 
	 * @param test-set 
	 * @param exp-name
	 * 
	 */
	
	public static void main(String[] args) {

		//**************************//
		//*      NLP@IDC PA2       *//
		//*   Statistical Parsing  *//
		//*     Point-of-Entry     *//
		//**************************//

		if (args.length < 3)
		{
			System.out.println("Usage: Parse <goldset> <trainset> <experiment-identifier-string>");
			return;
		}

		// 1. read input
		Treebank myGoldTreebank = TreebankReader.getInstance().read(true, args[0]);
		Treebank myTrainTreebank = TreebankReader.getInstance().read(true, args[1]);

		// 2. transform trees
		// TODO

		// 3. train
		Grammar myGrammar = Train.getInstance().train(myTrainTreebank);
//		for(Rule r: myGrammar.getLexicalRules()) System.out.println(r);
//		for(Rule r: myGrammar.getSyntacticRules()) System.out.println(r);
//		for(java.util.Map.Entry e: myGrammar.getRuleCounts().entrySet()) {
//			Rule r = (Rule) e.getKey();
//			System.out.println(r.getMinusLogProb() +", " + r.isTop() + ", " + e.getKey() + ", " + e.getValue());
//		}

		// 4. decode

		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(8);

		Decode decodeInstance = Decode.getInstance(myGrammar);

		Long startTime = System.currentTimeMillis();
		List<Tree> myParseTrees = new ArrayList<Tree>();
		Task[] tasks = new Task[myGoldTreebank.size()];
		for (int i = 0; i < myGoldTreebank.size(); i++) {
			Long currentTime = System.currentTimeMillis();
			List<String> mySentence = myGoldTreebank.getAnalyses().get(i).getYield();
			tasks[i] = new Task(mySentence, decodeInstance);
			executor.execute(tasks[i]);
//			Tree myParseTree = Decode.getInstance(myGrammar).decode(mySentence);
//			myParseTrees.add(myParseTree);
//			if(i % 10 == 0)
//				System.out.println("Finished processing " + i + " sentences");
			//System.out.println((System.currentTimeMillis() - currentTime) + ", " + myParseTree);
			//System.exit(0);
		}
		try {
			executor.shutdown();
			while (!executor.awaitTermination(1, TimeUnit.SECONDS)) {}
		} catch (InterruptedException e){
			System.out.println(e.getStackTrace());
		}
		System.out.println(System.currentTimeMillis() - startTime);

		// 5. de-transform trees
		List<Tree> myDeTransformedTrees = new ArrayList<Tree>();
		for(Task t: tasks){
			Tree t2 = new Tree(deTransformTree(t.getTree().getRoot()));
			myDeTransformedTrees.add(t2);
		}
//		List<Tree> myDeTransformedTrees = new ArrayList<Tree>();
//		for(Tree t: myParseTrees){
//			Tree t2 = new Tree(deTransformTree(t.getRoot()));
//			myDeTransformedTrees.add(t2);
//		//	System.out.println(t2);
//		}
		
		// 6. write output
		writeOutput(args[2], myGrammar, myDeTransformedTrees);
	}
	
	/**
	 * Writes output to files:
	 * = the trees are written into a .parsed file
	 * = the grammar rules are written into a .gram file
	 * = the lexicon entries are written into a .lex file
	 */
	private static void writeOutput(
			String sExperimentName, 
			Grammar myGrammar,
			List<Tree> myTrees) {
		
		writeParseTrees(sExperimentName, myTrees);
		writeGrammarRules(sExperimentName, myGrammar);
		writeLexicalEntries(sExperimentName, myGrammar);
	}

	/**
	 * Writes the parsed trees into a file.
	 */
	private static void writeParseTrees(String sExperimentName,
			List<Tree> myTrees) {
		LineWriter writer = new LineWriter(sExperimentName+".parsed");
		for (int i = 0; i < myTrees.size(); i++) {
			writer.writeLine(myTrees.get(i).toString());
		}
		writer.close();
	}
	
	/**
	 * Writes the grammar rules into a file.
	 */
	private static void writeGrammarRules(String sExperimentName,
			Grammar myGrammar) {
		LineWriter writer;
		writer = new LineWriter(sExperimentName+".gram");
		Set<Rule> myRules = myGrammar.getSyntacticRules();
		Iterator<Rule> myItrRules = myRules.iterator();
		while (myItrRules.hasNext()) {
			Rule r = (Rule) myItrRules.next();
			writer.writeLine(r.getMinusLogProb()+"\t"+r.getLHS()+"\t"+r.getRHS()); 
		}
		writer.close();
	}
	
	/**
	 * Writes the lexical entries into a file.
	 */
	private static void writeLexicalEntries(String sExperimentName, Grammar myGrammar) {
		LineWriter writer;
		Iterator<Rule> myItrRules;
		writer = new LineWriter(sExperimentName+".lex");
		Set<String> myEntries = myGrammar.getLexicalEntries().keySet();
		Iterator<String> myItrEntries = myEntries.iterator();
		while (myItrEntries.hasNext()) {
			String myLexEntry = myItrEntries.next();
			StringBuffer sb = new StringBuffer();
			sb.append(myLexEntry);
			sb.append("\t");
			Set<Rule> myLexRules =   myGrammar.getLexicalEntries().get(myLexEntry);
			myItrRules = myLexRules.iterator();
			while (myItrRules.hasNext()) {
				Rule r = (Rule) myItrRules.next();
				sb.append(r.getLHS().toString());
				sb.append(" ");
				sb.append(r.getMinusLogProb());
				sb.append(" ");
			}
			writer.writeLine(sb.toString());
		}
	}

	public static Node deTransformTree(Node node){
		if(node.isLeaf())
			return new Node(node.getLabel());

		Node newNode = new Node(node.getLabel());
		for(Node daughter: node.getDaughters()){
			Node newDaughter = deTransformTree(daughter);
			if(daughter.isLeaf() || !daughter.getLabel().contains(Train.MARKOVIZATION_SYMBOL)){
				newNode.addDaughter(newDaughter);
			} else {
				for(Node daughter2: newDaughter.getDaughters()){
					newNode.addDaughter(daughter2);
				}
			}

		}
		return newNode;
	}

	


}

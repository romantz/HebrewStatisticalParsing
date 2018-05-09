package treebank;

import java.util.ArrayList;
import java.util.List;

import tree.Tree;


/**
 * 
 * @author rtsarfat
 *
 * CLASS: Treebank
 * 
 * Definition: A collection of hand-annotated corpus of NL utterances with syntactic parse trees
 * Role: Know the kind of utterance/analysis to store
 * Responsibility: Handle tree-transfrorms over the treebank, cross-compare treebanks size/trees
 */
public class Treebank {

	protected List<Tree> m_lstAnalyses = new ArrayList<Tree>();
	
	public Treebank() {
		super();
	}

	public void add(Tree pt){
		getAnalyses().add(pt);		
	}
	
	public int size()
	{
		return m_lstAnalyses.size();
	}

	public List<Tree> getAnalyses() {
		return m_lstAnalyses;
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < getAnalyses().size(); i++) 
		{
			sb.append((getAnalyses().get(i)).toString());
			if (i+1< getAnalyses().size()) sb.append("\n");
		}
		
		return sb.toString();
	}
	
	
}

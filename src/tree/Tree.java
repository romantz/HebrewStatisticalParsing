package tree;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @author Reut Tsarfaty
 * 
 * CLASS: Tree
 * 
 * Definition: a linearily ordered recursive structure
 * Role: define ID/LP relations between nodes
 * Responsibility: provide the entry-point for navigating inside subtrees
 * 
 * 
 */

public class Tree {
	
	// a designated root node
	private Node m_nodeRoot = null;
	
	/**
	 * C'tor
	 * @param mNodeRoot
	 */
	public Tree() {
		super();
		m_nodeRoot = new Node();
		m_nodeRoot.setRoot(true);
	}
	
	public Tree(Node root) {
		super();
		m_nodeRoot = root;
		m_nodeRoot.setRoot(true);
	}
	
	
	public Node getRoot() {
		return m_nodeRoot;
	}

	public String toString()
	{
		return getRoot().toStringSubtree();
	}

	public List<Node> getNodes() 
	{
		List<Node> lst = new ArrayList<Node>();
		return getRoot().getNodes(lst);
	}
	
	public List<Terminal> getTerminals() 
	{
		return getRoot().getTerminals();
	}

	public List<String> getYield() 
	{
		return getRoot().getYield();		
	}
	
	 public Object clone()
	{
		Tree t = new Tree();
		t.setRoot((Node)getRoot().clone());
		return t;
	}

	private void setRoot(Node n) {
		m_nodeRoot = n;
	}
}

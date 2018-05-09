/*
 * Created on Feb 28, 2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package bracketimport;

import tree.Node;
import tree.Terminal;
import tree.Tree;
import java.util.ArrayList;
import java.util.Stack;

/**
 * @author Reut Tsarfaty
 * @author Evelina Andersson
 * t 1
 * @date created Feb 28, 2007
 * @date modified May 12, 2011
 * 
 * 
 * Definition: Reads an input string and creates a tree object based on it
 * Role: creates a hierarchical structure based on bracketed constituents
 * Responsibility: ignore redundant spaces, record string labels
 * 
 * */

public class TreeReader {

    
    /**
     * Implementation of a singlton pattern
     * Avoids redundant instances in memory 
     */
    public static TreeReader m_singConfigurator = null;
    
    public static TreeReader getInstance()
    {
        if (m_singConfigurator == null)
        {
            m_singConfigurator = new TreeReader();
        }
        return m_singConfigurator;
    }
    /**
     * 
     */
    public TreeReader() {
        super();
    }

    public static void main(String[] args) 
    {    
        
    	String sentence = "(S (NP (N  John))(VP (V loves)(NP (N  Mary) ) ) )";
	
    	Tree cc = (Tree)TreeReader.getInstance().read(sentence);
                    
        System.out.println(cc);
        
    }

    public Object read(String sInput) 
    {
	String[] chars = sInput.split("");
	StringBuffer sb = new StringBuffer();
	ArrayList<String> tokens = new ArrayList<String>();

	for(int i = 0; i < chars.length; i++)
	{
	    if(chars[i].equals(" "))
	    {
		if(sb.toString().length() > 0)
		    tokens.add(sb.toString());
		sb = new StringBuffer();
	    }
	    else if(chars[i].equals("(") && sb.toString().length() == 0)
		 {
		    sb.append(chars[i]);
		 }
		else if(!chars[i].equals(")"))
		     {
			 sb.append(chars[i]);
		     }
		     else
		     {
			 sb.append(chars[i]);
			 tokens.add(sb.toString());
			 sb = new StringBuffer();
		     }
	    
	}
		
	Stack traverse = new Stack();

	for(int i = 0; i < tokens.size(); i++) {
	    String token = tokens.get(i);
	    
	    if(traverse.empty()) 
	    {
		Node node = new Node(token.substring(1, token.length()));
		traverse.push(node);
	    } 
	    else 
	    {
		Node top = (Node) traverse.peek();
		if(token.startsWith("(")) 
	        {
		    Node node = new Node(token.substring(1, token.length()));
		    top = (Node) traverse.pop(); 
		    top.addDaughter(node);
		    traverse.push(top);
		    traverse.push(node);
		}
		else if(!token.equals(")")) 
		     {
			 if(token.endsWith(")"))
			 {
			     //Add terminal
			     Terminal nChild = new Terminal(token.substring(0, token.length()-1));
			     
			     top.addDaughter(nChild);
			     traverse.pop();
			     if(traverse.empty())
			     {
				 traverse.push(top);
			     }
			     else 
			     {
				 Node top2 = (Node) traverse.pop(); 
				 traverse.push(top2);
			     }
			 }
			 else 
			 {
			       //Add terminal
			     Terminal nChild = new Terminal(token);
			     top.addDaughter(nChild);
			     traverse.pop();
			     traverse.push(top);
			 }
		     }
		else if(token.equals(")"))
		     {
			 if(traverse.empty())
			 {
			     traverse.push(top);
			 }
			 else 
			 {
			     traverse.pop();
			     if(traverse.empty())
			     {
				 traverse.push(top);
			     }
			     else
			     {
				 Node top2 = (Node) traverse.pop();
				 traverse.push(top2);
			     }
			 }
		     }
	    }
	}

	return new Tree((Node) traverse.pop());
    }
}

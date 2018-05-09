package bracketimport;
import tree.Node;
import tree.Terminal;


/**
 * @author Reut Tsarfaty
 * 
 * @date created Feb 28, 2007
 * @date modified May 12, 2011
 * 
 * Definition: reads the node label from input
 * Role: create a coresponding node with the label as an identifier
 * Responsibility: distinguish terminal from non-terminal nodes
 * 
 * */

public class LabelReader  {
      
    /**
     * Implementation of a singleton pattern
     * Avoids redundant instances in memory 
     */
    public static LabelReader m_singReader = null;
    
    public static LabelReader getInstance()
    {
        if (m_singReader == null)
        {
        	m_singReader = new LabelReader();
        }
        return m_singReader;
    }
 
    /**
     * C'tor
     */
    public LabelReader() {
        super();
    }

    public static void main(String[] args) {
        
        LabelReader cc = LabelReader.getInstance();       
        Node c = (Node)cc.read("NN-ZY-H-NY3",false);
        
        System.out.println(c.getIdentifier());
        System.out.println(c.getYield());
        
    }
    

    /**
     * 
     * @param sInput
     * @param bTerminal
     * @return Object
     * 
     */
    public Object read(String sInput, boolean bTerminal) {
        
        if (!bTerminal)
        {
        	Node cNewNode = new Node(sInput);
        	return cNewNode;
        }
        else 
        {
        	Terminal cNewTerminal = new Terminal(sInput);
        	return cNewTerminal;
        }
    }

    
}


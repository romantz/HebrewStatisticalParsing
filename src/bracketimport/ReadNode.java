/*
 * Created on Mar 1, 2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package bracketimport;

import tree.Node;
 

/**
 * @author Reut Tsarfaty
 * @date created Mar 1, 2007
 * @date modified May 12, 2011
 * 
 * Definition: Keeps a node which was already read and created
 * Role: Know the input string span that was consumed by the dominated subtree
 * Responsibility: Keep the original string span that was consumed by the dominated subtree
 * 
 * */

public class ReadNode {

    private int m_iStartIndex = 0;
    private int m_iProcessed = 0;
    private String m_sConfigurationString = null;
    private Node m_cConfiguredNode = null;
    
    /**
     * 
     */
    public ReadNode() {
        super();
    }

    public static void main(String[] args) {
    }
    
    /**
     * @return Returns the m_cConfigured.
     */
    public Node getNode() {
        return m_cConfiguredNode;
    }
    /**
     * @param configured The m_cConfigured to set.
     */
    public void setNode(Node configured) {
        m_cConfiguredNode = configured;
    }
    /**
     * @return Returns the m_iEndIndex.
     */
    public int getProcessed() {
        return m_iProcessed;
    }
    /**
     * @param endIndex The m_iEndIndex to set.
     */
    public void setProcessed(int endIndex) {
        m_iProcessed = endIndex;
    }
    /**
     * @return Returns the m_iStartIndex.
     */
    public int getStartIndex() {
        return m_iStartIndex;
    }
    /**
     * @param startIndex The m_iStartIndex to set.
     */
    public void setStartIndex(int startIndex) {
        m_iStartIndex = startIndex;
    }
    /**
     * @return Returns the m_sConfigurationString.
     */
    public String getConfigurationString() {
        return m_sConfigurationString;
    }
    /**
     * @param configurationString The m_sConfigurationString to set.
     */
    public void setConfigurationString(String configurationString) {
        m_sConfigurationString = configurationString;
    }
}

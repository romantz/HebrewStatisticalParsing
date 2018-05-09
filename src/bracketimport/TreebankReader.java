package bracketimport;
import utils.LineWriter;
import tree.Tree;
import treebank.Treebank;
import utils.LineReader;
import java.io.*;
import java.util.*;

public class TreebankReader   {

	    /**
	     * Implementation of a singleton pattern
	     * Avoids redundant instances in memory 
	     */
	 public static TreebankReader m_singConfigurator = null;
   	    
	 public static TreebankReader getInstance()
	 {
	    if (m_singConfigurator == null)
	    {
	        m_singConfigurator = new TreebankReader();
	    }
	    return m_singConfigurator;
	 }
	    

    public static void main(String[] args) 
    {
	boolean linebreak = false;
	String path = null;
	String file = "";
	String dir = "";
	int startDir = -1;
	int endDir = -1;

	for(int i = 0; i < args.length; i++) 
	{
	    if(args[i].equals("-linebreak"))
		linebreak = true;
	
	    if(args[i].equals("-path")) 
	    {
		path = args[i+1];
		i++;
	    }

	    if(args[i].equals("-file")) 
	    {
		file = args[i+1];
		i++;
	    }
	    
	    if(args[i].equals("-dir")) 
	    {
		String[] directories = args[i+1].split("-");
		
		if(directories.length == 2)
		{
		    try 
		    {
			startDir = Integer.parseInt(directories[0]);
			endDir = Integer.parseInt(directories[1]);
			
			i++;
		    }
		    catch (Exception e)
		    {
			dir = args[i+1];
			i++;
		    }
		}
		else
	        {
		    dir = args[i+1];
		    i++;
		}
	    }
	}
	
	Treebank tb = (Treebank) TreebankReader.getInstance().read(linebreak, path, file, dir, startDir, endDir);
	    
	LineWriter lw = new LineWriter("test.ftree");
	
	for (int i = 0; i < tb.size(); i++) {
	    lw.writeLine((tb.getAnalyses().get(i)).toString());	
	}
	lw.close();
    }




    private ArrayList<String> getDirectoryFiles(String path, String directory)
    {
	ArrayList<String> files = new ArrayList<String>();

	try 
	{
	    File subpath = new File(path + "/" + directory);
	    String[] filesAndDirectories = subpath.list();
	    
	    Arrays.sort(filesAndDirectories);
	    
	    for(int i = 0; i < filesAndDirectories.length; i++)
	    {
		File dirOrFile = new File(path + "/" + directory + "/" + filesAndDirectories[i]);
		    
		if(dirOrFile != null && !dirOrFile.isDirectory())
		{
		    files.add(path + "/" + directory + "/" + filesAndDirectories[i]);
		}
		else 
		{
		    if(!filesAndDirectories[i].endsWith(".svn")) 
		    {
			files.addAll(getDirectoryFiles(path + "/" + directory, filesAndDirectories[i]));
		    }
		}
	    }
	}
	
	catch (Exception e)
	      {
		  e.printStackTrace();
	      }
	
	return files;
    }

    public Treebank read(boolean lineBreak, String pathAndFile)
    {
	String[] splits = pathAndFile.split("/");
	
	String myPath = splits[0];
	
	for(int i = 0; i < splits.length - 1; i++)
	{
	    myPath += "/" + splits[i];
	}
	
	if (myPath.equals(pathAndFile))
	{
	    myPath = "./";
	}
		
	return read(lineBreak, myPath, splits[splits.length-1], "", -1,-1);
    } 

    public Treebank read(boolean lineBreak, String path, String file, String dir, int startDir, int endDir) 
    {
	Treebank tb = new Treebank();
	String input = "";
	int iLine = 1;
	int iProcessed = 0;
	ArrayList<String> files = new ArrayList<String>();
	
	if(file.length() > 0)
        { 
	    input = path + "/" + file;
	    files.add(input);
	}
	
	else if(dir.length() > 0)
	    files = getDirectoryFiles(path, dir);
	else if (startDir != -1 && endDir != -1)
             {
		for(int d = startDir; d <= endDir; d++)
		{
		    String subDir = "";
		    
		    if(d < 10)
			subDir = "0" + d;
		    else 
			subDir = d + "";
		    
		    files.addAll(getDirectoryFiles(path, "" + subDir));
		}
	     }

	//	System.out.print("Reading off the treebank trees ");
	
	for(int f = 0; f < files.size(); f++)
	{
	    LineReader lrTreebank =  new LineReader(files.get(f));
	    String sLine = lrTreebank.readLine();
	  
	    StringBuffer sb = new StringBuffer();
	    
	    while (sLine != null)
	    {
	
		try
		{ 
		  
		    Tree pt = null;
		    
		    if(!lineBreak)
		    {
			if(sLine != null && sLine.equals(""))
			{
			    sLine = lrTreebank.readLine();
			}
			
			if (sLine != null && sLine.startsWith("("))
			{
			    sb.append(sLine);
			    sLine = lrTreebank.readLine();
			}
		     
			while( sLine != null && !sLine.equals("") && !sLine.startsWith("("))
			{
			    sb.append(sLine);
			    sLine = lrTreebank.readLine();
			}
		    
			if(sb.length() > 0)
			{
			    if(sb.toString().startsWith("("))
			    {
				pt = (Tree) TreeReader.getInstance().read(sb.toString());
			    }
			    
			    sb = new StringBuffer();
			    
			    tb.add(pt);
			    iProcessed++;
			    //System.out.print(".");
			}
		    }
		    		
		    if(pt == null && sLine != null)
		    {
			if(sLine.startsWith("("))
			{
			    pt = (Tree) TreeReader.getInstance().read(sLine);
			}
			
			 tb.add(pt);
			 iProcessed++;
			 //System.out.print(".");
			
			sLine = lrTreebank.readLine();
		    }
		
		   
		}
	
		catch (Exception e)
		{
		    System.out.print("Error... Tree " + iLine + " failed to upload.");
		    System.out.print(e.getMessage());
		    e.printStackTrace();
		    System.out.print(sLine);
		}
		iLine++;
	    }
	    iLine--;
	}   
	
	// -- End and Report 
	if (iProcessed != iLine)
	{
	    System.out.print("Finished!\nRead off " + iProcessed + " trees out of " + iLine+".\n");
            
	}
	System.out.print("Finished!\nRead off " + iLine + " trees.\n"); 
	
	return tb;
    }
}

package utils;


import java.io.*;

/**
 * Simple class to write lines into from a file. 
 * Create a LineWriter object,
 * then send it the writeLine() message as many times as desired. 
 * When you finish using it you should close the file ??
 */
public class LineWriter 
{   
	public static void main(String[] args) 
	{
		System.out.println("START");
		String sfile = "out.txt"	;
		LineWriter lw = new LineWriter(sfile);
		String sMsg = "this is a test";
		lw.writeLine(sMsg);
		sMsg = "this is another one";
		lw.writeLine(sMsg);
		lw.close();
		System.out.println("FINISH");
	}
	
    
    BufferedWriter bufferedWriter;
    
    /**
     * Creates a LineWriter to write strings as line into a file.
     */
    public LineWriter(String message) 
    {
        // create and display a file dialog
        //FileDialog dialog = new FileDialog(new Frame(), message, FileDialog.LOAD);
        //dialog.setVisible(true);
        // get the directory and name of the selected file
        //String dir = dialog.getDirectory();
        //String file = dialog.getFile();
        // make sure we got a file
        //if (dir == null || file == null) 
        //{
        //    System.err.println("No file selected.");
        //    return;
        //}
        // construct the full path name of the file
        //String fileName = dir + file;
    	
    	
    	
    	
        String fileName = message;
        Writer fileWriter = null;
        try {
            fileWriter = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8");
        }
        catch (IOException e) {
            System.err.println ("LineWriter can't find/create input file: " + fileName);
			System.err.println ("Need to create it myself :(");
            e.printStackTrace ();
        }
        // create and save a reader for the file
        bufferedWriter = new BufferedWriter (fileWriter);
    }
    
    /**
     * This constructor is used if you forget to supply a filename
     */
    LineWriter() 
    {
        this("Write lines to what file?");
    }
    
    /**
     * Once you have created a LineReader for a file, each call to readLine()
     * will return another line from that file. After the last line in read,
     * readLine() will return null instead of a String.
     */
    public void writeLine (String sLine) 
    {
        if (bufferedWriter == null) {
            System.err.println("writeLine() called without a valid file.");
        }
        try { 
        	bufferedWriter.write(sLine+"\n") ;
        }
        catch (IOException e) { e.printStackTrace();}
    } 

    /**
     * Closes the file used 
     */
     public void close () 
     {
        try { bufferedWriter.close(); }
        catch (IOException e) { }
    } 
    
	/**
	 * check if the file exist
	 */
	 boolean exists () 
	 {        
	 	if (bufferedWriter == null) 
	 	{
			System.err.println("writeLine() called without a valid input file.");
			return false;
	 	}
	 	else
	 	{
		 	return true;
	 	}
	}
}

package planning_lab;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

/** A class for providing simple reading and writing services for text files.
 * 
 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
 *
 */
public class DataHandler {

	/**
	 * Read the contents of a textfile into a string
	 * 
	 * @param textFile the path of the textfile to be read
	 * @return the contents of the textfile in a string
	 */
	public static String readFile2String(String textFile) {
		
		String ret = "";
			
		try {
				
			FileInputStream fis = new FileInputStream(textFile);
			int x = fis.available();
			byte[] b = new byte[x];
			fis.read(b);
			fis.close();
			ret = new String(b);
			
		} catch (IOException e) {
			
			System.out.println(e.toString());
		
		}
		
		return ret;
			
	} // End of DataHandler.readFile2String method

	/**
	 * Write a string into a given textfile
	 * 
	 * @param target the path of the textfile where the text content should be put (if it doesn't exist, it will be created, else overwritten)
	 * @param text the contents which shall be put into the given textfile
	 */
	public static void writeString2File(String target, String text) {

	    try {
	    	
	        BufferedWriter out = new BufferedWriter(new FileWriter(target));
	        out.write(text);
	        out.close();
	        
	    } catch (IOException e) {
	    
	    	System.out.println(e.toString());
	    	
	    }
    			
	} // End of DataHandler.writeString2File method
		
} // End of DataHandler class
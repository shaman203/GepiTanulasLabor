package planning_lab;

import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;



import au.com.bytecode.opencsv.CSVReader;

/** The contents of a CSV file with some extra functionality
 * 
 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
 *
 */
public class CSVtable implements Serializable {
	
	private static final long serialVersionUID = -631938223283670237L;
	/** The file path of the CSV file */
	private String filePath;
	/** The complete table corresponding to the CSV file (all the rows in a list) */
	private List<String[]> csvTable;

	/** Create a csvTable based upon a CSV file
	 * 
	 * @param filePath  The file path of the CSV file
	 *  
	 */
	public CSVtable(String filePath) {
		
		try {
			
			this.filePath		= filePath;
			CSVReader reader	= new CSVReader(new FileReader(filePath), ';');
			this.csvTable	 	= reader.readAll();
		
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	} // End of CSVtable(String) constructor
	
	/**
	 * 
	 * Get the filePath
	 * 
	 * @return the filePath
	 */
	public String getFilePath() {
		
		return this.filePath;
		
	} // End of CSVtable.getFilePath method
	
	/**
	 * 
	 * Get the csvTable
	 * 
	 * @return the csvTable object
	 */
	public List<String[]> getCSVtable() {
		
		return this.csvTable;
		
	} // End of CSVtable.getCSVtable method
	
	/**
	 * Find the first row, where given columns are of a given value (primary/unique key recommended),
	 * and return the value of its given column
	 * 
	 * @param inputidx the column indices of the appropriate columns to be compared
	 * @param input the values to which the inputidx columns' value must be compared to
	 * @param outputidx the index of the output column
	 * 
	 * @return the appropriate (outputidx-th) element of the row where the input was right
	 */
	public String translate(int[] inputidx, String[] input, int outputidx) throws Exception {

		String out = "";

		Iterator<String[]> csvi = this.csvTable.iterator();
		
		if (	csvi.hasNext() &&					// If there is a header in the CSV file, and...
				inputidx.length == input.length &&	// the column index array's length equals the length of the input, and...
				input.length > 0 &&					// that length is greater than zero, and...
				outputidx >= 0 &&					// the output's column index is greater than zero, and...
				outputidx < csvi.next().length) {	// the output's column index is not greater than the number of columns...

			while (csvi.hasNext()) {							// Iterate through rows...
				  
				  String[] row = csvi.next();
				  
				  for (int i = 0; i < inputidx.length; i++) {					// Iterate through columns...
					  
					  if (inputidx[i] >= 0 && inputidx[i] < row.length) {		// If input index is in range and until now every column was a hit...
						  
						  if (!row[inputidx[i]].equalsIgnoreCase(input[i])) {
							  
							  break;
							  
						  } else if (i == inputidx.length-1) {	// So we reached the end with a hit...!
							  
							  return row[outputidx];
							  
						  }
						  
					  } else {
						  
						  throw new Exception("Error in the " + i + "-th input index.");
						  
					  }
					  
				  }

			}
			
		}
		
		return out;
		
	} // End of CSVtable.translate() method
	
	/**
	 * Convert a CSV table into an appropriate String. Given are column indices in a given order.
	 * These columns are extracted from the table row by row, and then wrapped in a String array.
	 * This means, that the elements of the String array are used as pre/in/postfix "glue" for the
	 * appropriate elements extracted from the CSV table row-by-row.
	 * 
	 * @param csvTable the CSV table (in List<String[]> object) to be converted
	 * @param inputidx a String array consisting of int's that are column indices of the CSV table
	 * @param wrapping a variable length String array with elements to wrap the table elements extracted from the table (first is prefix, last is postfix, others are infix)
	 * 
	 * @return a String with the appropriate values of the CSV table wrapped in the given String array (row by row)
	 */
	public static String convert(List<String[]> csvTable, int[] inputidx, String[] wrapping) throws Exception {

		String out = "";

		Iterator<String[]> csvi = csvTable.iterator();
		
		if (csvi.hasNext()) {
			
			int columnnum	= csvi.next().length;
			int wrapnum		= wrapping.length;

			while (csvi.hasNext()) {										// Iterate through the rows of the table...
				  
				  out 			+= wrapping[0];
				  String[] row	=  csvi.next();
				  
				  for (int i = 0; i < inputidx.length; i++) {				// Iterate through columns...
					  
					  if (inputidx[i] >= 0 && inputidx[i] < columnnum) {	// If input index is in range and until now every column was a hit...

						  out += row[inputidx[i]];
						  if (i+1 < wrapnum) out += wrapping[i+1];
						  if (i == inputidx.length-1) out += "\n";
						  
					  } else {
						  
						  throw new Exception("ERROR: the " + i + "-th input index exceeds the number of columns in the table.");
						  
					  }
					  
				  } // for

			} // while
			
		} // if
		
		return out;
		
	} // End of CSVtable.convert() method
	
	/**
	 * Convert an array of strings into an array of int's
	 * 
	 * @param sarray a String array having int elements
	 * @return an array of int's
	 * @throws Exception
	 * 
	 */
	public static int[] convertStringArraytoIntArray(String[] sarray) throws Exception {
		
		if (sarray != null) {
			
			int intarray[] = new int[sarray.length];	
			
			for (int i = 0; i < sarray.length; i++) intarray[i] = Integer.parseInt(sarray[i]);
			
			return intarray;
		
		}
		
		return null;
		
	}  // End of CSVtable.convertStringArraytoIntArray() method
	
	/**
	 * Convert a CSV file to a special TXT file
	 * 
	 * 	 
	 * @param args[0]	Input CSV filepath
	 * @param args[1]	Output TXT filepath
	 * @param args[2]	Input column indexes (whitespace delimited, element values starting from zero to columns-1)
	 * @param args[3+]	Wrapping strings
	 * 
	 * @example For example the arguments of CSVTable in case of trying to produce some initial price declarations in a PDDL problem description could be the following (given the data.csv file used for the accompanying laboratory):
	 * \jade\src\msclab01\planning_lab\csv\data.csv \jade\src\msclab01\planning_lab\csv\problem_price_data.pddl "2 0 5" "(= (price " " " ") " ")"
	 */
	public static void main(String[] args) {

		if (args.length > 3) {

			try {

				System.out.println("Beginning conversion...");
				
				// Prepare the input arguments for further processing...
				String inputFilepath	= args[0];
				String outputFilepath	= args[1];
				int[] inputidx			= convertStringArraytoIntArray(args[2].split(" "));
				String[] wrapping		= Arrays.copyOfRange(args, 3, args.length);

				// Get the CSV table
				List<String[]> csvTable	= new CSVReader(new FileReader(inputFilepath), ';').readAll();

				// Write the appropriate conversion of the CSV table into a text file
				DataHandler.writeString2File(outputFilepath, CSVtable.convert(csvTable, inputidx, wrapping));

				System.out.println("Conversion finished successfully...");
				
			} catch (Exception e) {

				e.printStackTrace();

			}

		} else {

			System.out.println(	"Not enough input arguments (at least 4 needed):\n\n" +
								"(0)  Input CSV filepath\n" +
								"(1)  Output filepath\n" +
								"(2)  Input column indices (whitespace delimited, element values starting from zero to columns-1, e.g. \"3 0 2 1\")\n" +
								"(3+) Wrapping strings (before, between, and after the extracted table elements");

		}

	}  // End of CSVtable.main() method

} // End of CSVtable class
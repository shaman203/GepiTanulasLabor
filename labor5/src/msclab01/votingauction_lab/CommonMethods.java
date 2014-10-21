package msclab01.votingauction_lab;

import java.util.*;

/** A class for providing simple methods for the agents of this excercise.
 * 
 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
 *
 */
public class CommonMethods {

	/**
	 * Inverse of String.split()
	 * 
	 * @param s a string array to be joined
	 * @param delimiter a string to delimit the joined elements of string array s
	 * @return a string of the joined elements of string array s
	 */
	public static String join(String[] s, String delimiter) {
		
		String out = s[0];
	    for (int i = 1; i < s.length; i++) out += delimiter + s[i];
	    return out;
	    
	} // End of CommonMethods.join() static method
		
	/**
	 * Find the index of the (first) maximal element in an array of integers
	 * 
	 * @param t an array of integers
	 * @return the index of the (first) maximal element in t
	 */
	public static int maxIdx(int[] t) {
		
	    int imax = 0;
	    
	    for (int i = 1; i < t.length; i++) {
	    	
	        if (t[i] > t[imax]) {
	        	
	        	imax = i;
	            
	        }
	        
	    }
	    
	    return imax;
	    
	} // End of CommonMethods.maxIdx() static method

	/**
	 * Return the number of maximal elements in an array of integers
	 * 
	 * @param t an array of integers
	 * @return the number of maximal elements in t
	 */
	public static int maxNum(int[] t) {
		
		// The initial value of the number of maximums in t...
		int maxnum = 0;
		
		// Get the value of the maximum...
	    int max = t[maxIdx(t)];
	    
	    for (int i = 0; i < t.length; i++) {
	    	
	        if (t[i] == max) maxnum++;
	        
	    }
	    
	    return maxnum;
	    
	} // End of CommonMethods.maxNum() static method
	
	/**
	 * Return the indices of all the maximal elements in an array of integers
	 * 
	 * @param t an array of integers
	 * @return a vector of all the indices of the maximal elements in t
	 */
	public static Vector<Integer> allMaxes(int[] t) {

		Vector<Integer> out = new Vector<Integer>();
		
		// Get the value of the maximum...
	    int max = t[maxIdx(t)];
	    
	    for (int i = 0; i < t.length; i++) {
	    	
	        if (t[i] == max) out.add(new Integer(i));
	        
	    }
	    
	    return out;
	    
	} // End of CommonMethods.allMaxes() static method
	
} // End of CommonMethods class
package org.bme.mit.iir;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Util {

	public static String readFileAsString(String fname) throws IOException {
		return join(readLinesIntoList(fname), "\n");
	}
	
	public static List<String> readLinesIntoList(String fname) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fname));
		ArrayList<String> lines = new ArrayList<String>();
		String line;
		while ((line = br.readLine()) != null) {
			lines.add(line);
		}
		return lines;
	}
	
	public static <T>
	String join(final Iterable<T> objs, final String delimiter) {
	    Iterator<T> iter = objs.iterator();
	    if (!iter.hasNext())
	        return "";
	    StringBuilder sb = new StringBuilder(String.valueOf(iter.next()));
	    while (iter.hasNext())
	        sb.append(delimiter).append(String.valueOf(iter.next()));
	    return sb.toString();
	}
}

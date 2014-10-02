package org.bme.mit.iir;

import java.io.IOException;
import java.util.Map;

public class Feladat1 {

	public static void main(String[] args) throws IOException {
		
		Indexer indexer = new Indexer();
		
		Map<String,Map<String,Integer>> map;
		map = indexer.indexFolder("data/corpus/");
		//indexer.writeToFileAsClearText(map, "indexfile.txt");
		indexer.writeToFileSerialized(map, "indexfile.ser");
		
	}

}

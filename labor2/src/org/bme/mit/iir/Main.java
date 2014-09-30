package org.bme.mit.iir;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

	public static void main(String[] args) throws IOException {
		
		Indexer indexer = new Indexer();
		Searcher searcher = new Searcher();
		
		Map<String,Map<String,Integer>> map = indexer.indexFolder("data/IndexTest/");
		//Map<String,List<SzoEloford>> map = indexer.indexFolder("E:\\labor 2\\workspace\\labor2\\data\\corpus\\");
		indexer.writeToFile(map, "indexfile.txt");
		
		System.out.println("Beolvastam "+map.size());
		System.out.println(map.get("Linux"));
		
		List<String> terms = new ArrayList<String>();
		terms.add("Linux");
		System.out.println(searcher.searchForTerms(terms, map));
	}

}

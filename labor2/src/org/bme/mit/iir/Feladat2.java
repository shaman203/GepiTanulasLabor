package org.bme.mit.iir;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Feladat2 {

	public static void main(String[] args) throws IOException {
		Indexer indexer = new Indexer();
		Searcher searcher = new Searcher();
		
		
		Map<String,Map<String,Integer>> map;

		map = indexer.readFromFileSerialized("indexfile.ser");

		List<String> terms = new ArrayList<String>();
		terms.add("processzor");
		terms.add("Intel");
		System.out.println(searcher.searchForTerms(terms, map).size());

	}

}

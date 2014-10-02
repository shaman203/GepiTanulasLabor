package org.bme.mit.iir;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

	public static void main(String[] args) throws IOException {
		
		Indexer indexer = new Indexer();
		Searcher searcher = new Searcher();
		PelletConceptExpander expander = new PelletConceptExpander("pc_shop_0.owl", true, true);
		Map<String,Map<String,Integer>> map;
		//map = indexer.indexFolder("data/corpus/");
		//indexer.writeToFileAsClearText(map, "indexfile.txt");
		//indexer.writeToFileSerialized(map, "indexfile.ser");
		map = indexer.readFromFileSerialized("indexfile.ser");
		//System.out.println("Beolvastam "+map.size());
		//System.out.println(map.get("Linux"));
		
		List<String> terms = new ArrayList<String>();
		terms.add("processzor");
		System.out.println(searcher.searchForTerms(terms, map).size());
		System.out.println(searcher.searchForExpandedTerms(terms, map, expander).size());
		
	}

}

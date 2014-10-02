package org.bme.mit.iir;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Feladat3 {

	public static void main(String[] args) throws IOException {

		Indexer indexer = new Indexer();
		Searcher searcher = new Searcher();
		PelletConceptExpander expander = new PelletConceptExpander("pc_shop_0.owl", true,false);
		Map<String,Map<String,Integer>> map;
		map = indexer.readFromFileSerialized("indexfile.ser");
		List<String> terms = new ArrayList<String>();
		terms.add("processzor");
		System.out.println(searcher.searchForTerms(terms, map).size());
		System.out.println(searcher.searchForExpandedTerms(terms, map, expander).size());

	}

}

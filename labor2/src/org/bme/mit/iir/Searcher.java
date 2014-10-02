package org.bme.mit.iir;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 *  A feladat: K�sz�tsen az 1. feladatban k�sz�tett index f�jlra egy keres� programot. A program bemen� param�terk�nt szavakat kap, eredm�nyk�nt ki�rja azon dokumentumok nev�t, amelyekben mindegyik sz� szerepel.
 *  Leadand�: a keres� program, r�vid magyar�zat, fut�si p�lda.
 *  Felhaszn�lhat� adatok: az 1. feladatban k�sz�tett index f�jl.
 *  Tan�csok: T�bb sz�b�l �ll� keres�si minta eset�n a dokumentumokat lehet szavank�nt is list�zni, b�r egy j� keres� rangsorolja az eredm�nyt aszerint, hogy egy-egy dokumentumban h�ny sz�t tal�lt meg (illetve melyik sz� h�nyszor szerepelt). Ez a k�s�bbi feladatok szempontj�b�l is hasznos lehet.
 *  Referencia id�ig�ny: 30 perc
 */

public class Searcher {

	public List<String> searchForTerms(List<String> terms,
			Map<String, Map<String,Integer>> map) {
		return searchForExpandedTerms(terms, map, null);
	}

	private Set<String> getTermDocs(String term, Map<String, Map<String,Integer>>  map, PelletConceptExpander expander) {

		if(expander == null)
			return map.get(term).keySet();
		
		Set<String> similarTerms = expander.expandConcept(term);
		similarTerms.add(term);
		
		Set<String> docs = new HashSet<String>();
		for(String simterm : similarTerms)
		{
			if(map.containsKey(simterm))
			docs.addAll(map.get(simterm).keySet());
		}
		
		return docs;
	}

	private List<String> findCommon(List<Set<String>> filteredStuff) {

		List<String> docList = new ArrayList<String>();

		if (filteredStuff.size() > 0) {
			Set<String> first = filteredStuff.get(0);
			for (String doc : first) {
				if (presentInAll(doc, filteredStuff)) {
					docList.add(doc);
				}
			}
		}
		return docList;
	}

	private boolean presentInAll(String doc,
			List<Set<String>> filteredStuff) {

		for (Set<String> szo : filteredStuff) {

			boolean megvolt = false;
			for (String elo : szo)
				if (elo.equals(doc)) {
					megvolt = true;
					break;
				}
			if (!megvolt)
				return false;

		}

		return true;
	}

	public List<String>  searchForExpandedTerms(
			List<String> terms, Map<String, Map<String, Integer>> map, PelletConceptExpander expander) {
		
		List<String> docList = new ArrayList<String>();
		List<Set<String>> filteredStuff = new ArrayList<Set<String>>();
		for (String term : terms) {
			if (map.containsKey(term)) {
				
				filteredStuff.add(getTermDocs(term, map, expander));
			}
		}

		docList = findCommon(filteredStuff);

		return docList;
	}

}

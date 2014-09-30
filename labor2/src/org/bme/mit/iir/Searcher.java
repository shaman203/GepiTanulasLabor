package org.bme.mit.iir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
		List<String> docList = new ArrayList<String>();

		List<Map<String,Integer>> filteredStuff = new ArrayList<Map<String,Integer>>();
		for (String term : terms) {
			if (map.containsKey(term)) {
				filteredStuff.add(map.get(term));
			}
		}

		docList = findCommon(filteredStuff);

		return docList;
	}

	private List<String> findCommon(List<Map<String,Integer>> filteredStuff) {

		List<String> docList = new ArrayList<String>();

		if (filteredStuff.size() > 0) {
			Map<String,Integer> first = filteredStuff.get(0);
			for (String doc : first.keySet()) {
				if (presentInAll(doc, filteredStuff)) {
					docList.add(doc);
				}
			}
		}
		return docList;
	}

	private boolean presentInAll(String doc,
			List<Map<String,Integer>> filteredStuff) {

		for (Map<String,Integer> szo : filteredStuff) {

			boolean megvolt = false;
			for (String elo : szo.keySet())
				if (elo.equals(doc)) {
					megvolt = true;
					break;
				}
			if (!megvolt)
				return false;

		}

		return true;
	}

}

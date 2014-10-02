package org.bme.mit.iir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;



/*
 * 1. feladat: egyszer� indexel�s
 *  A feladat: tervezzen �s val�s�tson meg egy egyszer� inverz dokumentum el��ll�t� programot! A program a param�terk�nt megadott k�nyvt�rban tal�lhat� sz�veges dokumentumokat elemzi, majd egy kimeneti index f�jlban elt�rolja, hogy melyik sz� mely dokumentumokban tal�lhat� meg.
 *  Leadand�: az indexel� program �s r�vid le�r�sa.
 *  Felhaszn�lhat� adatok: a korpusz.
 *  Eszk�z�k: A korpusz beolvas�s�ban Java nyelven �rt seg�df�ggv�nyek seg�tenek, l�sd a f�ggel�ket.
 *  Tan�csok: Egy egyszer� statisztik�t kell k�sz�teni. Az inverz dokumentum (avagy dokumentum index) l�nyege az, hogy ha megadunk egy sz�t, akkor kikereshet� legyen, hogy az mely dokumentumokban szerepelt. Azt is c�lszer� elt�rolni, hogy egy sz� h�nyszor szerepelt egy adott dokumentumban. Az index f�jl szerkezete szabadon kialak�that�, de feleljen meg a tov�bbi feladatoknak. Az index kialak�t�sakor ne m�trixos form�ban t�rolja a sz�-dokumentum p�rokat, hanem t�m�ritve, azaz csak azok a sz�-dokumentum p�rok szerepeljenek, melyek el�fordul�si gyakoris�ga nem nulla.
 *  Referencia id�ig�ny: 30 perc
 */

public class Indexer 
{
	private TermRecognizer recog;
	private int docCount;


	public Indexer() throws IOException
	{
		recog = new TermRecognizer();
	}

	public Map<String,Map<String,Integer>> indexFolder(String folderName)
	{
		HashMap<String,Map<String,Integer>> map = new HashMap<String,Map<String,Integer>>();
		final File folder = new File(folderName);
		this.docCount = 0;
		indexFilesInFolder(folder,map);
		System.out.println("Indexed doc count = "+this.docCount);
		return map;
	}

	public void indexFilesInFolder(final File folder, Map<String,Map<String,Integer>> map) {
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				indexFilesInFolder(fileEntry,map);
			} else {
				indexFile(fileEntry.getPath(),map);
			}
		}	
	}

	private void indexFile(String docName, Map<String, Map<String,Integer>> map) {

		try {
			this.docCount++;

			for(String text :  Util.readLinesIntoList(docName))
			{
				Map<String,Integer> words = recog.termFrequency(text);
				for(Entry<String,Integer> entry : words.entrySet())
				{
					if(!map.containsKey(entry.getKey()))
					{
						map.put(entry.getKey(), new HashMap<String,Integer>());
					}
					Map<String,Integer> something = map.get(entry.getKey());
					if(!something.containsKey(docName))
					{
						something.put(docName,new Integer(0));
					}
					something.put(docName, something.get(docName)+ entry.getValue());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void writeToFileAsClearText(Map<String,Map<String,Integer>> map, String filename)
	{

		BufferedWriter writer = null;
		try {
			//create a temporary file
			File logFile = new File(filename);
			writer = new BufferedWriter(new FileWriter(logFile));

			for(Entry<String, Map<String,Integer>> szo : map.entrySet())
			{
				writer.write(szo.getKey()+" ");
				for(Entry<String,Integer> entry : szo.getValue().entrySet())
				{
					writer.write(entry.getKey()+" "+entry.getValue().toString()+", ");
				}
				writer.write("\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// Close the writer regardless of what happens...
				writer.close();
			} catch (Exception e) {
			}
		}
	}

	public void writeToFileSerialized(Map<String,Map<String,Integer>> map, String filename)
	{

		BufferedWriter writer = null;
		try {
			//create a temporary file
			File logFile = new File(filename);
			writer = new BufferedWriter(new FileWriter(logFile));

			FileOutputStream fileOut =
					new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(map);
			out.close();
			fileOut.close();


		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// Close the writer regardless of what happens...
				writer.close();
			} catch (Exception e) {
			}
		}
	}
	
	public Map<String,Map<String,Integer>> readFromFileSerialized(String filename)
	{
		Map<String,Map<String,Integer>> map = null;
		 try
	      {
	         FileInputStream fileIn = new FileInputStream(filename);
	         ObjectInputStream in = new ObjectInputStream(fileIn);
	         map = (Map<String,Map<String,Integer>>) in.readObject();
	         in.close();
	         fileIn.close();
	         return map;
	      }catch(IOException i)
	      {
	         i.printStackTrace();
	         return null;
	      }catch(ClassNotFoundException c)
	      {
	         System.out.println("Employee class not found");
	         c.printStackTrace();
	         return null;
	      }
	}
}




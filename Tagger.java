package nil;

import java.util.HashMap;
import java.util.Iterator;
//import org.apache.commons.math3.*;


public class Tagger {
	
	private final static int clusters = 8;
	
	public static void brownCluster() throws Exception{
		
		long startTime = System.nanoTime();
		
		//Retrieve taggable vocab and initialize appropriately
			//Average distance from X most frequent words
		//WARNING: we compress entire vocabulary into a hashmap -- could be dangerous for large vocabularies
		startTime = System.nanoTime();
		HashMap<String, String> tagVocab = Librarian.returnTaggableVocab();
		//Taggable Vocab is as follows <Cluster, Parseable String of Words in Cluster>
		System.out.println("Intialized taggable vocabulary in " + Commander.convertTime(System.nanoTime() - startTime) + ".");
		
		startTime = System.nanoTime();
		final int V = tagVocab.size();
		Librarian.returnProbabilityMap();
		System.out.println("Performed Brown cluster initialization for " + V + " words in " + Commander.convertTime(System.nanoTime() - startTime) + ".");
		
		//Brown Clustering
		startTime = System.nanoTime();
		System.out.println("Performed non-prototypical Brown cluster for " + V + " words in " + Commander.convertTime(System.nanoTime() - startTime) + ".");
		
		//Prototype and perform second cluster
		startTime = System.nanoTime();
		System.out.println("Performed prototypical Brown cluster for " + V + " words in " + Commander.convertTime(System.nanoTime() - startTime) + ".");
		
		//Update POS in database
		startTime = System.nanoTime();
		System.out.println("Updated cluster for " + V + " words in " + Commander.convertTime(System.nanoTime() - startTime) + ".");
		
	}
	
}

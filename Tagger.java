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
		HashMap<String, HashMap<String, Double>> probabilityMap = Librarian.returnProbabilityMap();
		HashMap<String, Double> sMap = makeSMap(probabilityMap);
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
	
	private static double qCalculation(HashMap<String, HashMap<String, Double>> probabilityMap, String l, String m){
		
		double p = probabilityMap.get(l).get(m);
		double leftP = 1.0;
		HashMap<String, Double> part = probabilityMap.get(l);
		Iterator<Double> partIterator = part.values().iterator();
		while(partIterator.hasNext())
			leftP += partIterator.next();
		double rightP = 1.0;
		Iterator<HashMap<String, Double>> fullIterator = probabilityMap.values().iterator();
		while(fullIterator.hasNext()){
			
			part = fullIterator.next();
			if(part.containsKey(m))
				rightP += part.get(m);
			
		}
		
		return (p * Math.log(p / (leftP * rightP)));
		
	}
	
	private static HashMap<String, Double> makeSMap(HashMap<String, HashMap<String, Double>> probabilityMap){
		
		HashMap<String, Double> sMap = new HashMap<String, Double>();
		String[] keyArray = probabilityMap.keySet().toArray(new String[0]);
		for(int i = 0; i < keyArray.length; i++){
			
			double s = 0.0;
			
			for(int j = 0; j < keyArray.length; j++){
				
				if(probabilityMap.get(keyArray[i]).containsKey(keyArray[j]))
					s += qCalculation(probabilityMap, keyArray[i], keyArray[j]);
				
				if(probabilityMap.get(keyArray[j]).containsKey(keyArray[i]))
					s += qCalculation(probabilityMap, keyArray[j], keyArray[i]);
				
			}
			
			if(probabilityMap.get(keyArray[i]).containsKey(keyArray[i]))
				s -= qCalculation(probabilityMap, keyArray[i], keyArray[i]);
			
			sMap.put(keyArray[i], s);
			
		}
		return sMap;
		
	}
	
}

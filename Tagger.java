package nil;

import java.util.ArrayList;
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
		HashMap<String, ArrayList<String>> tagVocab = Librarian.returnTaggableVocab();
		//Taggable Vocab is as follows <Cluster, Parseable String of Words in Cluster>
		System.out.println("Intialized taggable vocabulary in " + Commander.convertTime(System.nanoTime() - startTime) + ".");
		
		//Initialize tables for faster Brown clustering
		startTime = System.nanoTime();
		final int V = tagVocab.size();
		HashMap<String, HashMap<String, Double>> probabilityMap = Librarian.returnProbabilityMap();
		HashMap<String, Double> sMap = makeSMap(probabilityMap);
		System.out.println("Performed Brown cluster initialization for " + V + " words in " + Commander.convertTime(System.nanoTime() - startTime) + ".");
		
		//Brown Clustering
		startTime = System.nanoTime();
		while(tagVocab.size() >  clusters)
			brownMerge(tagVocab, probabilityMap, sMap);
		System.out.println("Performed non-prototypical Brown cluster for " + V + " words in " + Commander.convertTime(System.nanoTime() - startTime) + ".");
		
		//Reinitialize tables for faster Brown clustering and prototype
		startTime = System.nanoTime();
		System.out.println("Performed prototypical Brown cluster reinitialization for " + V + " words in " + Commander.convertTime(System.nanoTime() - startTime) + ".");
		
		//Perform second cluster
		startTime = System.nanoTime();
		System.out.println("Performed prototypical Brown cluster for " + V + " words in " + Commander.convertTime(System.nanoTime() - startTime) + ".");
		
		//Update POS in database
		startTime = System.nanoTime();
		System.out.println("Updated cluster for " + V + " words in " + Commander.convertTime(System.nanoTime() - startTime) + ".");
		
	}
	
	private static double qCalculation(HashMap<String, HashMap<String, Double>> probabilityMap, String l, String m){
		
		double p = probabilityMap.get(l).get(m);
		/*double leftP = 1.0;
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
			
		}*/
		double leftP = 0.0;
		double rightP = 0.0;
		HashMap<String, Double> part = probabilityMap.get(l);
		Iterator<Double> partIterator = part.values().iterator();
		while(partIterator.hasNext()){
			Double next = partIterator.next();
			//Positive nexts are forward bigrams where the current word is left
			if(next > 0.0)
				leftP += next;
			//Negative nexts are reverse bigrams where the current word is right
			//else
			//	rightP -= next;
		}
		part = probabilityMap.get(m);
		partIterator = part.values().iterator();
		while(partIterator.hasNext()){
			Double next = partIterator.next();
			//Negative nexts are reverse bigrams where the current word is right
			if(next < 0.0)
				rightP -= next;
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
	
	private static void brownMerge(HashMap<String, ArrayList<String>> tagVocab, HashMap<String, HashMap<String, Double>> probabilityMap, HashMap<String, Double> sMap){
		
		String cluster1 = "";
		String cluster2 = "";
		double changeInformation = Double.NEGATIVE_INFINITY;
		
		String[] keys = probabilityMap.keySet().toArray(new String[0]);
		for(int i = 0; i < keys.length; i++){
			
			for(int j = i+1; j < keys.length; j++){
				
				double changeInformationTemp = -1.0*sMap.get(keys[i]) - sMap.get(keys[j]) + qCalculation(probabilityMap, keys[i], keys[j]) + qCalculation(probabilityMap, keys[j], keys[i]);
				//p_k(i+j, i+j)
				double p = 0.0;
				if(probabilityMap.get(keys[i]).containsKey(keys[i]))
					p += probabilityMap.get(keys[i]).get(keys[i]);
				if(probabilityMap.get(keys[i]).containsKey(keys[j]))
					p += probabilityMap.get(keys[i]).get(keys[j]);
				if(probabilityMap.get(keys[j]).containsKey(keys[j]))
					p += probabilityMap.get(keys[j]).get(keys[j]);
				if(probabilityMap.get(keys[j]).containsKey(keys[i]))
					p += probabilityMap.get(keys[j]).get(keys[i]);
				//Left and Right p for q_k(i+j, i+j)
				double leftP = 0.0;
				double rightP = 0.0;
				HashMap<String, Double> part = probabilityMap.get(keys[i]);
				Iterator<Double> partIterator = part.values().iterator();
				while(partIterator.hasNext()){
					Double next = partIterator.next();
					//Positive nexts are forward bigrams where the current word is left
					if(next > 0.0)
						leftP += next;
					//Negative nexts are reverse bigrams where the current word is right
					else
						rightP -= next;
				}
				part = probabilityMap.get(keys[j]);
				partIterator = part.values().iterator();
				while(partIterator.hasNext()){
					Double next = partIterator.next();
					//Negative nexts are reverse bigrams where the current word is right
					if(next > 0.0)
						rightP += next;
					else
						rightP -= next;
				}
				double q = 0.0;
				if(p > 0.0)
					q =  (p * Math.log(p / (leftP * rightP)));
				changeInformationTemp += q;
				//q_l
				q = 0.0;
				for(int k = 0; k < keys.length; k++){
					
					if(i != k && j != k){
						
						p = 0.0;
						if(probabilityMap.get(keys[k]).containsKey(keys[i]))
							p += probabilityMap.get(keys[k]).get(keys[i]);
						if(probabilityMap.get(keys[k]).containsKey(keys[j]))
							p += probabilityMap.get(keys[k]).get(keys[j]);
						
						if(p > 0.0){
							
							double leftPTemp = 0.0;
							part = probabilityMap.get(keys[k]);
							partIterator = part.values().iterator();
							while(partIterator.hasNext()){
								Double next = partIterator.next();
								//Positive nexts are forward bigrams where the current word is left
								if(next > 0.0)
									leftP += next;
							}
							q += (p * Math.log(p / (leftPTemp * rightP)));
							
						}
						
						p = 0.0;
						if(probabilityMap.get(keys[i]).containsKey(keys[k]))
							p += probabilityMap.get(keys[i]).get(keys[k]);
						if(probabilityMap.get(keys[j]).containsKey(keys[k]))
							p += probabilityMap.get(keys[j]).get(keys[k]);
						
						if(p > 0.0){
							
							double rightPTemp = 0.0;
							part = probabilityMap.get(keys[k]);
							partIterator = part.values().iterator();
							while(partIterator.hasNext()){
								Double next = partIterator.next();
								//Negative nexts are reverse bigrams where the current word is right
								if(next < 0.0)
									rightP -= next;
							}
							q += (p * Math.log(p / (leftP * rightPTemp)));
							
						}
						
					}
					
				}
				
				changeInformationTemp += q;
				
				if(changeInformationTemp > changeInformation){
					
					changeInformation = changeInformationTemp;
					cluster1 = keys[i];
					cluster2 = keys[j];
					
				}
				
			}
		
		}
		
		//merge clusters
		
	}
	
}

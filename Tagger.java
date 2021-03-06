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
		HashMap<String, HashMap<String, Double>> lProbabilityMap = Librarian.returnProbabilityMap();
		//Make right-bigram probability map from left-bigram probability map
		HashMap<String, HashMap<String, Double>> rProbabilityMap = new HashMap<String, HashMap<String, Double>>();
		String[] words = tagVocab.keySet().toArray(new String[0]);
		for(int i = 0; i < words.length; i++){
			
			rProbabilityMap.put(words[i], new HashMap<String, Double>());
			for(int j = 0; j < words.length; j++){
				
				if(lProbabilityMap.containsKey(words[j]) && lProbabilityMap.get(words[j]).containsKey(words[i]))
					rProbabilityMap.get(words[i]).put(words[j], lProbabilityMap.get(words[j]).get(words[i]));
				
			}
			
		}
		HashMap<String, Double> sMap = makeSMap(tagVocab, lProbabilityMap, rProbabilityMap);
		System.out.println("Performed Brown cluster initialization for " + V + " words in " + Commander.convertTime(System.nanoTime() - startTime) + ".");
		
		//Brown Clustering
		startTime = System.nanoTime();
		while(tagVocab.size() >  clusters)
			brownMerge(tagVocab, lProbabilityMap, rProbabilityMap, sMap);
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
	
	private static double qCalculation(HashMap<String, HashMap<String, Double>> lProbabilityMap, HashMap<String, HashMap<String, Double>> rProbabilityMap, String l, String m){
		
		if(!lProbabilityMap.containsKey(l) || !lProbabilityMap.get(l).containsKey(m))
			return 0.0;
		
		if(!rProbabilityMap.containsKey(m) || !rProbabilityMap.get(m).containsKey(l))
			return 0.0;
		
		double p = lProbabilityMap.get(l).get(m);
		double leftP = 0.0;
		double rightP = 0.0;
		HashMap<String, Double> part = lProbabilityMap.get(l);
		Iterator<Double> partIterator = part.values().iterator();
		while(partIterator.hasNext())
			leftP += partIterator.next();
		part = rProbabilityMap.get(m);
		partIterator = part.values().iterator();
		while(partIterator.hasNext())
			rightP += partIterator.next();
		
		return (p * Math.log(p / (leftP * rightP)));
		
	}
	
	private static HashMap<String, Double> makeSMap(HashMap<String, ArrayList<String>> tagVocab, HashMap<String, HashMap<String, Double>> lProbabilityMap, HashMap<String, HashMap<String, Double>> rProbabilityMap){
		
		HashMap<String, Double> sMap = new HashMap<String, Double>();
		String[] keyArray = tagVocab.keySet().toArray(new String[0]);
		for(int i = 0; i < keyArray.length; i++){
			
			double s = 0.0;
			
			for(int j = 0; j < keyArray.length; j++){
				
				if(lProbabilityMap.containsKey(keyArray[i]) && lProbabilityMap.get(keyArray[i]).containsKey(keyArray[j]))
					s += qCalculation(lProbabilityMap, rProbabilityMap, keyArray[i], keyArray[j]);
				
				if(lProbabilityMap.containsKey(keyArray[j]) && lProbabilityMap.get(keyArray[j]).containsKey(keyArray[i]))
					s += qCalculation(lProbabilityMap, rProbabilityMap, keyArray[j], keyArray[i]);
				
			}
			
			if(lProbabilityMap.containsKey(keyArray[i]) && lProbabilityMap.get(keyArray[i]).containsKey(keyArray[i]))
				s -= qCalculation(lProbabilityMap, rProbabilityMap, keyArray[i], keyArray[i]);
			
			sMap.put(keyArray[i], s);
			
		}
		return sMap;
		
	}
	
	private static void brownMerge(HashMap<String, ArrayList<String>> tagVocab, HashMap<String, HashMap<String, Double>> lProbabilityMap, HashMap<String, HashMap<String, Double>> rProbabilityMap, HashMap<String, Double> sMap){
		
		//Determine which clusters to merge
		String cluster1 = "";
		String cluster2 = "";
		double changeInformation = Double.NEGATIVE_INFINITY;
		
		String[] keys = tagVocab.keySet().toArray(new String[0]);
		for(int i = 0; i < keys.length; i++){
			
			for(int j = i+1; j < keys.length; j++){
				
				double changeInformationTemp = -1.0*sMap.get(keys[i]) - sMap.get(keys[j]) + qCalculation(lProbabilityMap, rProbabilityMap, keys[i], keys[j]) + qCalculation(lProbabilityMap, rProbabilityMap, keys[j], keys[i]);
				//p_k(i+j, i+j)
				double p = 0.0;
				if(lProbabilityMap.containsKey(keys[i]) && lProbabilityMap.get(keys[i]).containsKey(keys[i]))
					p += lProbabilityMap.get(keys[i]).get(keys[i]);
				if(lProbabilityMap.containsKey(keys[i]) && lProbabilityMap.get(keys[i]).containsKey(keys[j]))
					p += lProbabilityMap.get(keys[i]).get(keys[j]);
				if(lProbabilityMap.containsKey(keys[j]) && lProbabilityMap.get(keys[j]).containsKey(keys[j]))
					p += lProbabilityMap.get(keys[j]).get(keys[j]);
				if(lProbabilityMap.containsKey(keys[j]) && lProbabilityMap.get(keys[j]).containsKey(keys[i]))
					p += lProbabilityMap.get(keys[j]).get(keys[i]);
				//Left and Right p for q_k(i+j, i+j)
				double leftP = 0.0;
				double rightP = 0.0;
				if(lProbabilityMap.containsKey(keys[i])){
					HashMap<String, Double> part = lProbabilityMap.get(keys[i]);
					Iterator<Double> partIterator = part.values().iterator();
					while(partIterator.hasNext())
						leftP += partIterator.next();
				}
				
				if(rProbabilityMap.containsKey(keys[i])){
					HashMap<String, Double> part = rProbabilityMap.get(keys[i]);
					Iterator<Double> partIterator = part.values().iterator();
					while(partIterator.hasNext())
						rightP += partIterator.next();
				}
				
				if(lProbabilityMap.containsKey(keys[j])){
					HashMap<String, Double> part = lProbabilityMap.get(keys[j]);
					Iterator<Double> partIterator = part.values().iterator();
					while(partIterator.hasNext())
						leftP += partIterator.next();
				}
				
				if(rProbabilityMap.containsKey(keys[j])){
					HashMap<String, Double> part = rProbabilityMap.get(keys[j]);
					Iterator<Double> partIterator = part.values().iterator();
					while(partIterator.hasNext())
						rightP += partIterator.next();
				}
				
				double q = 0.0;
				if(p > 0.0)
					q =  (p * Math.log(p / (leftP * rightP)));
				else{
					
					leftP = 1.0;
					rightP = 1.0;
					
				}
				changeInformationTemp += q;
				//q_l
				q = 0.0;
				for(int k = 0; k < keys.length; k++){
					
					if(i != k && j != k){
						
						p = 0.0;
						if(lProbabilityMap.containsKey(keys[k]) && lProbabilityMap.get(keys[k]).containsKey(keys[i]))
							p += lProbabilityMap.get(keys[k]).get(keys[i]);
						if(lProbabilityMap.containsKey(keys[k]) && lProbabilityMap.get(keys[k]).containsKey(keys[j]))
							p += lProbabilityMap.get(keys[k]).get(keys[j]);
						
						if(p > 0.0){
							
							double leftPTemp = 0.0;
							HashMap<String, Double> part = lProbabilityMap.get(keys[k]);
							Iterator<Double> partIterator = part.values().iterator();
							while(partIterator.hasNext())
								leftPTemp += partIterator.next();
							q += (p * Math.log(p / (leftPTemp * rightP)));
							
						}
						
						p = 0.0;
						if(lProbabilityMap.containsKey(keys[i]) && lProbabilityMap.get(keys[i]).containsKey(keys[k]))
							p += lProbabilityMap.get(keys[i]).get(keys[k]);
						if(lProbabilityMap.containsKey(keys[j]) && lProbabilityMap.get(keys[j]).containsKey(keys[k]))
							p += lProbabilityMap.get(keys[j]).get(keys[k]);
						
						if(p > 0.0){
							
							double rightPTemp = 0.0;
							HashMap<String, Double> part = rProbabilityMap.get(keys[k]);
							Iterator<Double> partIterator = part.values().iterator();
							while(partIterator.hasNext())
								rightPTemp += partIterator.next();
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
		//update tagVocab
		tagVocab.get(cluster1).addAll(tagVocab.get(cluster2));
		tagVocab.remove(cluster2);
		//update probabilityMap
		HashMap<String, Double> lProbabilityCluster1 = lProbabilityMap.get(cluster1);
		lProbabilityMap.remove(cluster1);
		HashMap<String, Double> rProbabilityCluster1 = rProbabilityMap.get(cluster1);
		rProbabilityMap.remove(cluster1);
		HashMap<String, Double> lProbabilityCluster2 = lProbabilityMap.get(cluster2);
		lProbabilityMap.remove(cluster2);
		HashMap<String, Double> rProbabilityCluster2 = rProbabilityMap.get(cluster2);
		rProbabilityMap.remove(cluster2);
		
		lProbabilityMap.put(cluster1, new HashMap<String, Double>());
		rProbabilityMap.put(cluster1, new HashMap<String, Double>());
		
		Iterator<String> iterator = tagVocab.keySet().iterator();
		while(iterator.hasNext()){
			
			String word = iterator.next();
			if(!word.equals(cluster1) && !word.equals(cluster2)){
				
				//left-bigram
				if(lProbabilityMap.containsKey(word) && lProbabilityMap.get(word).containsKey(cluster1) && lProbabilityMap.get(word).containsKey(cluster2)){
					double p = lProbabilityMap.get(word).get(cluster1);
					p += lProbabilityMap.get(word).get(cluster2);
					lProbabilityMap.get(word).put(cluster1, p);
					lProbabilityMap.get(word).remove(cluster2);
					rProbabilityMap.get(cluster1).put(word, p);
				}
				else if(lProbabilityMap.containsKey(word) && lProbabilityMap.get(word).containsKey(cluster2)){
					double p = lProbabilityMap.get(word).get(cluster2);
					lProbabilityMap.get(word).put(cluster1, p);
					lProbabilityMap.get(word).remove(cluster2);
					rProbabilityMap.get(cluster1).put(word, p);
				}
				else if(lProbabilityMap.containsKey(word) && lProbabilityMap.get(word).containsKey(cluster1))
					rProbabilityMap.get(cluster1).put(word, lProbabilityMap.get(word).get(cluster1));
				
				//right-bigram
				if(rProbabilityMap.containsKey(word) && rProbabilityMap.get(word).containsKey(cluster1) && rProbabilityMap.get(word).containsKey(cluster2)){
					double p = rProbabilityMap.get(word).get(cluster1);
					p += rProbabilityMap.get(word).get(cluster2);
					rProbabilityMap.get(word).put(cluster1, p);
					rProbabilityMap.get(word).remove(cluster2);
				}
				else if(rProbabilityMap.containsKey(word) && rProbabilityMap.get(word).containsKey(cluster2)){
					rProbabilityMap.get(word).put(cluster1, lProbabilityMap.get(word).get(cluster2));
					rProbabilityMap.get(word).remove(cluster2);
				}
				else if(rProbabilityMap.containsKey(word) && rProbabilityMap.get(word).containsKey(cluster1))
					lProbabilityMap.get(cluster1).put(word, rProbabilityMap.get(word).get(cluster1));
				
			}
			
		}
		double p = 0.0;
		if(lProbabilityCluster1.containsKey(cluster1))
			p += lProbabilityCluster1.get(cluster1);
		if(lProbabilityCluster1.containsKey(cluster2))
			p += lProbabilityCluster1.get(cluster2);
		if(lProbabilityCluster2.containsKey(cluster2))
			p += lProbabilityCluster2.get(cluster2);
		if(lProbabilityCluster2.containsKey(cluster1))
			p += lProbabilityCluster2.get(cluster1);
		if(p > 0.0){
			
			lProbabilityMap.get(cluster1).put(cluster1, p);
			rProbabilityMap.get(cluster1).put(cluster1, p);
			
		}
		//update sMap
		HashMap<String, Double> newSMap = new HashMap<String, Double>();
		iterator = sMap.keySet().iterator();
		while(iterator.hasNext()){
			
			String cluster = iterator.next();
			if(!cluster.equals(cluster2)){
				
				double s = 0.0;
				if(!cluster.equals(cluster1)){
					
					s = sMap.get(cluster) + qCalculation(lProbabilityMap, rProbabilityMap, cluster, cluster1) + qCalculation(lProbabilityMap, rProbabilityMap, cluster1, cluster);
					
					double leftPCluster = 0.0;
					double rightPCluster = 0.0;
					HashMap<String, Double> part = lProbabilityMap.get(cluster);
					Iterator<Double> partIterator = part.values().iterator();
					while(partIterator.hasNext())
						leftPCluster += partIterator.next();
					part = rProbabilityMap.get(cluster);
					partIterator = part.values().iterator();
					while(partIterator.hasNext())
						rightPCluster += partIterator.next();
					
					//cluster - cluster1
					p = rProbabilityCluster1.get(cluster);
					double rightP = 0.0;
					partIterator = rProbabilityCluster1.values().iterator();
					while(partIterator.hasNext())
						rightP += partIterator.next();
					s -= (p * Math.log(p / (leftPCluster * rightP)));
					//cluster1 - cluster
					p = lProbabilityCluster1.get(cluster);
					double leftP = 0.0;
					partIterator = lProbabilityCluster1.values().iterator();
					while(partIterator.hasNext())
						leftP += partIterator.next();
					s -= (p * Math.log(p / (leftP * rightPCluster)));
					//cluster - cluster2
					p = rProbabilityCluster2.get(cluster);
					rightP = 0.0;
					partIterator = rProbabilityCluster2.values().iterator();
					while(partIterator.hasNext())
						rightP += partIterator.next();
					s -= (p * Math.log(p / (leftPCluster * rightP)));
					//cluster2 - cluster
					p = lProbabilityCluster2.get(cluster);
					leftP = 0.0;
					partIterator = lProbabilityCluster2.values().iterator();
					while(partIterator.hasNext())
						leftP += partIterator.next();
					s -= (p * Math.log(p / (leftP * rightPCluster)));
					
					newSMap.put(cluster, s);
					
				}
				
			}
			
		}
		
		double s = 0.0;
		iterator = newSMap.keySet().iterator();
		while(iterator.hasNext()){
			
			String word = iterator.next();
			
			if(lProbabilityMap.get(cluster1).containsKey(word))
				s += qCalculation(lProbabilityMap, rProbabilityMap, cluster1, word);
			
			if(lProbabilityMap.get(word).containsKey(cluster1))
				s += qCalculation(lProbabilityMap, rProbabilityMap, word, cluster1);
			
		}
		
		if(lProbabilityMap.get(cluster1).containsKey(cluster1))
			s -= qCalculation(lProbabilityMap, rProbabilityMap, cluster1, cluster1);
		newSMap.put(cluster1, s);
		
		sMap = newSMap;
		
		System.out.println(cluster1 + " " +  cluster2);
		
	}
	
}

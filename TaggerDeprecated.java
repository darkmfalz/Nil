package nil;

import java.util.HashSet;
import java.util.List;
import java.util.Arrays;
import java.util.TreeSet;
//import org.apache.commons.math3.*;


public class TaggerDeprecated {
	
	private final static int clusters = 8;
	
	public static void hierachicalCluster() throws Exception{
		
		//Initialize all words to individual cluster
		/*String[] words = corpus.split("[\\s]+");
		for(int i = 0; i < words.length; i++){
			
			if(Dictionary.noWordPOS(words[i].toLowerCase()))
				Dictionary.updateWordPOS(words[i].toLowerCase(), words[i].toLowerCase());
			
		}*/
		
		long startTime = System.nanoTime();
		
		//Extract X most frequent words
		int size = 500;
		String[] mostFreq = Librarian.mostFreqWordsInit(size);
		System.out.println("Extracted 500 most frequent words in " + Commander.convertTime(System.nanoTime() - startTime) + ".");
		//Construct context vectors
			//Average distance from X most frequent words
		//WARNING: we compress entire vocabulary into an array -- could be dangerous for large vocabularies
		startTime = System.nanoTime();
		String[][] tagVocab = Librarian.returnTaggableVocab();
		String[] cluster = new String[tagVocab.length];
		double[] clusterSize = new double[tagVocab.length];
		double[][] clusterMean = new double[tagVocab.length][500];
		for(int i = 0; i < tagVocab.length; i++){
			
			double[] context = contextVector(tagVocab[i][0], tagVocab[i][2].split(" [\\.] "), mostFreq);
			//Reinitialize individual clusters
			tagVocab[i][1] = tagVocab[i][0];
			//Update cluster arrays
			cluster[i] = tagVocab[i][1];
			clusterSize[i] = 1.0;
			clusterMean[i] = context;
			
		}
		System.out.println("Constructed context vectors for all words in " + Commander.convertTime(System.nanoTime() - startTime) + ".");
		//Hierarchical Agglomerative Clustering
		startTime = System.nanoTime();
		int clusters = clusterSize.length;
		while(clusters > TaggerDeprecated.clusters){
			
			mergeWard(cluster, clusterSize, clusterMean, tagVocab);
			clusters--;
			//System.out.println(clusters + " : " + new HashSet<String>(Arrays.asList(cluster)).size() + " : " + Arrays.stream(cluster).distinct().toArray().length);
			//In case of de-sync, investigate why and how
			if(clusters != new HashSet<String>(Arrays.asList(cluster)).size() || clusters != Arrays.stream(cluster).distinct().toArray().length){
				//for(int i = 0; i < tagVocab.length; i++)
				//	System.out.println(tagVocab[i][0] + " : " + cluster[i] + " : " + clusterSize[i] + " : " + Arrays.toString(clusterMean[i]));
				break;
			}
			//Cluster re-sync
			if(clusters <= TaggerDeprecated.clusters){
				clusters = Arrays.stream(cluster).distinct().toArray().length;
				if(clusters <= TaggerDeprecated.clusters)
					break;
			}
			
		}
		System.out.println("Performed first cluster for all words in " + Commander.convertTime(System.nanoTime() - startTime) + ".");
		//Re-distribute clusters appropriately
		
		//Prototype and recluster
		
		//Update POS in database
		for(int i = 0; i < tagVocab.length; i++)
			Librarian.updateWordPOS(tagVocab[i][0], cluster[i]);
		
	}
	
	private static double[] contextVector(String word, String[] sentences, String[] mostFreq){
		
		//Re-do context vector
		double[] context = new double[mostFreq.length];
		//Not appearing with a word gives it a very high value;
		for(int i = 0; i < context.length; i++)
			context[i] = Double.MAX_VALUE / 3.0;
		int[] occurrences = new int[mostFreq.length];
		for(int i = 0; i < sentences.length; i++){
			
			List<String> sentence = Arrays.asList(sentences[i].split("[\\s]+"));
			int indexW = sentence.indexOf(word);
			//For each of the most frequent words
			for(int j = 0; j < mostFreq.length; j++){
				
				int indexF = sentence.indexOf(mostFreq[j]);
				if(indexF >= 0){
					
					occurrences[j]++;
					if(occurrences[j] <= 1)
						context[j] =  indexW - indexF;
					else
						context[j] =  context[j] * (occurrences[j] - 1) / occurrences[j] + (indexW - indexF) / occurrences[j];
					
				}
				
			}
			
		}
		
		return context;
		
	}

	private static void mergeWard(String[] cluster, double[] clusterSize, double[][] clusterMean, String[][] tagVocab){
		
		TreeSet<String> iterated = new TreeSet<String>();
		double[] minimumCostMerge = new double[3];
		minimumCostMerge[0] = Double.MAX_VALUE;
		
		theCutie:
		for(int i = 0; i < cluster.length; i++){
			
			if(!iterated.contains(cluster[i])){
				
				for(int j = i + 1; j < cluster.length; j++){
					
					if(!iterated.contains(cluster[j])){
						
						double cost = (clusterSize[i] * clusterSize[j]) / (clusterSize[i] + clusterSize[j]) * dotSum(clusterMean[i], clusterMean[j]);
						if(cost < minimumCostMerge[0] && !cluster[i].equals(cluster[j])){
							
							//Update minimumCostMerge
							minimumCostMerge[0] = cost;
							minimumCostMerge[1] = i;
							minimumCostMerge[2] = j;
							
							if(cost <= 0)
								break theCutie;
							
						}
						
					}
					
				}
				iterated.add(cluster[i]);
				
			}
			
		}
		
		//Merge clusters
		int i = (int) minimumCostMerge[1];
		int j = (int) minimumCostMerge[2];
		for(int k = 0; k < cluster.length; k++){
			
			if(cluster[k].equals(cluster[i])){
				System.out.println(tagVocab[k][0] + " : " + cluster[k] + " : " + clusterSize[k]);
				if(cluster[k].equals("section"))
					System.out.println(Arrays.toString(clusterMean[k]));
			}
			else if(cluster[k].equals(cluster[j])){
				System.out.println(tagVocab[k][0] + " : " + cluster[k] + " : " +  clusterSize[k]);
				if(cluster[k].equals("section"))
					System.out.println(Arrays.toString(clusterMean[k]));
			}
			
		}
		System.out.println("Merged \"" + cluster[i] + "\" and \"" + cluster[j] + "\" at cost " + minimumCostMerge[0] + ".");
		for(int k = 0; k < clusterMean[i].length; k++){
			
			clusterMean[i][k] = clusterMean[i][k] * clusterSize[i] / (clusterSize[i] + clusterSize[j]) + clusterMean[j][k] * clusterSize[j] / (clusterSize[i] + clusterSize[j]);
			clusterMean[j][k] = clusterMean[i][k];
			
		}
		clusterSize[i] += clusterSize[j];
		clusterSize[j] = clusterSize[i];
		String clusterJ = cluster[j];
		//Update all pertinent words
		for(int k = 0; k < cluster.length; k++){
			
			if(cluster[k].equals(cluster[i])){
				
				clusterSize[k] = clusterSize[i];
				clusterMean[k] = clusterMean[i];
				System.out.println(tagVocab[k][0] + " : " + cluster[k] + " : " +  clusterSize[k]);
				if(cluster[k].equals("section"))
					System.out.println(Arrays.toString(clusterMean[k]));
				
			}
			else if(cluster[k].equals(clusterJ)){
				
				cluster[k] = cluster[i];
				clusterSize[k] = clusterSize[i];
				clusterMean[k] = clusterMean[i];
				System.out.println(tagVocab[k][0] + " : " + cluster[k] + " : " +  clusterSize[k]);
				if(cluster[k].equals("section"))
					System.out.println(Arrays.toString(clusterMean[k]));
				
			}
			
		}
		System.out.println();
		
	}

	private static double dotSum(double[] clusterMean1, double[] clusterMean2) {
		
		double sum = 0;
		
		for(int i = 0; i < clusterMean1.length; i++)
			sum += Math.pow(clusterMean1[i] - clusterMean2[i], 2.0);
		
		return sum;
		
	}
	
}

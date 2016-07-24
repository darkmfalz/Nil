package nil;

import java.util.List;
import java.util.Arrays;
//import org.apache.commons.math3.*;


public class Tagger {
	
	private final static int clusters = 8;
	
	public static void brownCluster() throws Exception{
		
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
		System.out.println(Arrays.toString(mostFreq));
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
			
			double[] context = contextVector(tagVocab[i][0], tagVocab[i][2].split(" . "), mostFreq);
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
		while(clusters > Tagger.clusters){
			
			mergeWard(cluster, clusterSize, clusterMean);
			clusters--;
			//Cluster re-sync
			if(clusters <= Tagger.clusters){
				clusters = Arrays.stream(cluster).distinct().toArray().length;
				if(clusters <= Tagger.clusters)
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
		
		double[] context = new double[mostFreq.length];
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
					allZeros = false;
					
				}
				
			}
			
		}
		
		return context;
		
	}

	private static void mergeWard(String[] cluster, double[] clusterSize, double[][] clusterMean){
		
		double[] minimumCostMerge = new double[3];
		minimumCostMerge[0] = Double.MAX_VALUE;
		
		theCutie:
		for(int i = 0; i < cluster.length; i++){
			
			for(int j = i + 1; j < cluster.length; j++){
				
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
		
		//Merge clusters
		int i = (int) minimumCostMerge[1];
		int j = (int) minimumCostMerge[2];
		//System.out.println("Merged \"" + cluster[i] + "\" and \"" + cluster[j] + "\" at cost " + minimumCostMerge[0] + ".");
		cluster[j] = cluster[i];
		for(int k = 0; k < clusterMean[i].length; k++){
			
			clusterMean[i][k] = clusterMean[i][k] * clusterSize[i] / (clusterSize[i] + clusterSize[j]) + clusterMean[j][k] * clusterSize[j] / (clusterSize[i] + clusterSize[j]);
			clusterMean[j][k] = clusterMean[i][k];
			
		}
		clusterSize[i] += clusterSize[j];
		clusterSize[j] = clusterSize[i];
		
	}

	private static double dotSum(double[] clusterMean1, double[] clusterMean2) {
		
		double sum = 0;
		
		for(int i = 0; i < clusterMean1.length; i++)
			sum += Math.pow(clusterMean1[i] - clusterMean2[i], 2.0);
		
		return sum;
		
	}
	
}

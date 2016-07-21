package nil;

import java.util.List;
import java.util.Arrays;
import org.apache.commons.math3.*;


public class Tagger {
	
	//private final static int clusters = 8;
	
	public static void brownCluster() throws Exception{
		
		//Initialize all words to individual cluster
		/*String[] words = corpus.split("[\\s]+");
		for(int i = 0; i < words.length; i++){
			
			if(Dictionary.noWordPOS(words[i].toLowerCase()))
				Dictionary.updateWordPOS(words[i].toLowerCase(), words[i].toLowerCase());
			
		}*/
		
		//Extract X most frequent words
		int size = 500;
		String[] mostFreq = Dictionary.mostFreqWordsInit(size);
		//Construct context vectors
			//Average distance from X most frequent words
		String[][] tagVocab = Dictionary.returnTaggableVocab();
		for(int i = 0; i < tagVocab.length; i++){
			
			double[] context = contextVector(tagVocab[i][0], tagVocab[i][2].split(" . "), mostFreq);
			Dictionary.updateWordContext(tagVocab[i][0], context);
			System.out.println(Arrays.toString(context));
			
		}
		//Hierarchical Agglomerative Clustering
		//Re-distribute clusters appropriately
		
	}
	
	private static double[] contextVector(String word, String[] sentences, String[] mostFreq){
		
		double[] context = new double[mostFreq.length];
		int[] occurrences = new int[mostFreq.length];
		for(int i = 0; i < sentences.length; i++){
			
			List<String> sentence = Arrays.asList(sentences[i].split("[//s]+"));
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

}

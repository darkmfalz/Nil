package nil;

import java.sql.SQLException;

import org.apache.commons.math3.*;

public class Tagger {
	
	//private final static int clusters = 8;
	
	public static void brownCluster(String corpus) throws Exception{
		
		//Initialize all words to individual cluster
		/*String[] words = corpus.split("[\\s]+");
		for(int i = 0; i < words.length; i++){
			
			if(Dictionary.noWordPOS(words[i].toLowerCase()))
				Dictionary.updateWordPOS(words[i].toLowerCase(), words[i].toLowerCase());
			
		}*/
		
		//Extract X most frequent words
		String[] mostFreq = Dictionary.mostFreqWordsInit(500);
		//Construct context vectors
			//Average distance from X most frequent words?
			//That's probably the best approach
		//Hierarchical Agglomerative Clustering
		//Re-distribute clusters appropriately
		
	}

}
